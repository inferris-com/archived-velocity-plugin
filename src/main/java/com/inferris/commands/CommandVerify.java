package com.inferris.commands;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.inferris.Inferris;
import com.inferris.database.DatabasePool;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.util.CodeGenerator;
import com.inferris.util.ContentTypes;
import com.inferris.util.DatabaseUtils;
import com.inferris.util.RestClientManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class CommandVerify extends Command implements TabExecutor {

    private static final String API_BASE_URL = "https://inferris.com/community/api/";
    String API_KEY = Inferris.getProperties().getProperty("xf.api.key");
    private static final String TITLE = "Verification request";
    private static final int EXPIRATION_TIME = 15; // minutes
    private String recommendationName = null;
    private String code = null;
    private String recipient_id = null;

    public CommandVerify(String name) {
        super(name);
    }


    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            if (args.length == 0) {
                sendInstructions(player);

            } else if (args.length == 2 && args[0].equalsIgnoreCase("key")) {
                String verificationCode = args[1];
                String storedCode; // Initialize the variable outside the block

                try (Connection connection = DatabasePool.getConnection();
                     ResultSet resultSet = DatabaseUtils.queryData(connection, "verification_sessions", new String[]{"verification_key", "expiration_time"}, "uuid = '" + player.getUniqueId() + "'")) {
                    if (resultSet.next()) {
                        storedCode = resultSet.getString("verification_key");
                        Timestamp expirationTime = resultSet.getTimestamp("expiration_time");
                        if (verificationCode.equals(storedCode)) {
                            String whereClause = "uuid = '" + player.getUniqueId() + "'";

                            // TODO: Add verification process here

                            long currentTime = System.currentTimeMillis();
                            if (!isVerificationCodeExpired(expirationTime)) {
                                verify(player);
                            } else {
                                DatabaseUtils.removeData(connection, "verification_sessions", whereClause);
                                player.sendMessage(new TextComponent(ChatColor.YELLOW + "The verification code has " + ChatColor.RED +
                                        "expired" + ChatColor.YELLOW + ". Generate a new key by re-verifying yourself."));
                            }
                        } else {
                            player.sendMessage(new TextComponent(ChatColor.RED + "The verification code you entered is invalid."));
                        }
                    } else {
                        player.sendMessage(new TextComponent(ChatColor.RED + "You are not currently in the verification process."));
                    }
                } catch (SQLException e) {
                    Inferris.getInstance().getLogger().severe(e.getMessage());
                }

                    /*
                    Verification request
                     */

            } else {
                String forumUsername = String.join(" ", Arrays.copyOfRange(args, 0, args.length));
                String forumData = getForumData(forumUsername, "username");

                if (forumData != null) {
                    if (recommendationName != null) {
                        player.sendMessage(new TextComponent(ChatColor.YELLOW + "Did you mean " + recommendationName + "?"));
                        recommendationName = null;
                    } else {
                        boolean isVerified = false;
                        boolean isSessionVerified = false;
                        String condition = "uuid = '" + player.getUniqueId() + "'";

                        try (Connection connection = DatabasePool.getConnection();
                             ResultSet verificationResultSet = DatabaseUtils.queryData(connection, "verification", new String[]{"*"}, condition);
                             ResultSet sessionResultSet = DatabaseUtils.queryData(connection, "verification_sessions", new String[]{"*"}, condition)) {

                            if (verificationResultSet.next()) {
                                isVerified = true;
                            }
                            if (sessionResultSet.next()) {
                                isSessionVerified = true;
                            }
                        } catch (SQLException e) {
                            Inferris.getInstance().getLogger().severe(e.getMessage());
                        }

                        if (!isVerified) {

                            if (!isSessionVerified) {
                                player.sendMessage(new TextComponent(ChatColor.GREEN + "Verification message sent to forum account: " + forumData));
                                player.sendMessage(new TextComponent(ChatColor.GRAY + "Please check your forum inbox for further instructions. This verification request will expire in " + EXPIRATION_TIME + " minutes."));
                                String[] columnNames = {"uuid", "mc_username", "verification_key", "expiration_time", "recipient_id"};
                                code = CodeGenerator.generateCode(10);
                                sendMessage();
                                Object[] values = {String.valueOf(player.getUniqueId()), player.getName(), code, new Timestamp(System.currentTimeMillis() + (EXPIRATION_TIME * 60 * 1000)), recipient_id};

                                try (Connection connection = DatabasePool.getConnection()) {
                                    DatabaseUtils.insertData(connection, "verification_sessions", columnNames, values);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                player.sendMessage(new TextComponent(ChatColor.RED + "You already have a verification session in progress. Requests expire in 15 minutes from the time of use."));
                            }
                        } else {
                            player.sendMessage(new TextComponent(ChatColor.RED + "You are already verified! If you want to unlink, use " + ChatColor.YELLOW + "/unlink"));
                        }
                    }
                } else {
                    player.sendMessage(new TextComponent(ChatColor.RED + "The username you provided doesn't exist in our database."));
                }
            }
        }
    }

    private void sendInstructions(ProxiedPlayer player) {
        player.sendMessage(new TextComponent(ChatColor.GOLD + "==============="));
        player.sendMessage(new TextComponent(ChatColor.GREEN + "Verify forum account"));
        player.sendMessage(new TextComponent(""));
        player.sendMessage(new TextComponent(ChatColor.YELLOW + "Please head to https://inferris.com/community." +
                " If you aren't registered, please register. If you are registered, use: /verify <your-forum-username>"));
        player.sendMessage(new TextComponent(ChatColor.GOLD + "==============="));
    }

    private String getForumData(String username, String field) {

        try(RestClientManager restClientManager = new RestClientManager()){
            Response response = restClientManager.sendRequest(API_BASE_URL + "users/find-name/?username=" + username, RestClientManager.Method.GET,
                    API_KEY);

            String responseBody = response.body().string();
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<String, Object>>() {
            }.getType();
            Map<String, Object> responseMap = gson.fromJson(responseBody, type);

            Object exactObj = responseMap.get("exact");
            Object recommendationsObj = responseMap.get("recommendations");

            if (exactObj instanceof Map) {
                Map<String, Object> exactMap = (Map<String, Object>) exactObj;
                recipient_id = exactMap.get("user_id").toString();
                return exactMap.get(field).toString();
            } else if (recommendationsObj instanceof List) {
                List<Map<String, Object>> recommendationsList = (List<Map<String, Object>>) recommendationsObj;
                if (!recommendationsList.isEmpty()) {
                    Map<String, Object> firstRecommendationMap = recommendationsList.get(0);
                    recommendationName = firstRecommendationMap.get(field).toString();
                    return "Did you mean " + recommendationName + "?";
                }
            }
        }catch(Exception e){
            Inferris.getInstance().getLogger().severe(e.getMessage());
        }
        return null;
    }

    private void sendMessage() {
        String message = "Hi, your verification request has been received. Your unique code is [B]" + code + "[/B]." +
                " Do not give out this code. Use [ICODE]/verify key " + code + "[/ICODE] on the server to complete verification. " +
                "If you did not request verification, please ignore this conversation. If you feel that your account has been " +
                "compromised, or someone is abusing the system, please inform a staff member immediately. Thank you!";

        try (RestClientManager restClientManager = new RestClientManager()) {
            restClientManager.sendRequest(API_BASE_URL + "conversations", RestClientManager.Method.POST, API_KEY, MediaType.parse(ContentTypes.X_WWW_FORM_URLENCODED.getType()),
                    "recipient_ids[]=" + recipient_id + "&title=" + TITLE + "&message=" + message + "&conversation_open=false&open_invite=false");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void verify(ProxiedPlayer player) {
        String usernameMC = null;
        int recipient_id = 0;
        player.sendMessage(new TextComponent(ChatColor.GREEN + "Verification successful!"));
        player.sendMessage(new TextComponent(""));
        player.sendMessage(new TextComponent(ChatColor.YELLOW + "Your accounts have been successfully linked."));

        try (Connection connection = DatabasePool.getConnection();

            ResultSet resultSet = DatabaseUtils.queryData(connection, "verification_sessions", new String[]{"mc_username", "recipient_id"}, "uuid = '" + player.getUniqueId() + "'")){
            if(resultSet.next()){
                usernameMC = resultSet.getString(1);
                recipient_id = resultSet.getInt(2);


            String tableName = "verification";
            String[] columnNames = {"uuid", "xenforo_id"};
            Object[] values = {player.getUniqueId().toString(), recipient_id};

            String whereClause = "uuid = '" + player.getUniqueId() + "'";
            DatabaseUtils.removeData(connection, "verification_sessions", whereClause);

            DatabaseUtils.insertData(connection, tableName, columnNames, values);
            }//*
        } catch (SQLException e) {
            Inferris.getInstance().getLogger().severe(e.getMessage());
        }

        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
        playerData.getProfile().setXenforoId(recipient_id);
        PlayerDataManager.getInstance().updateAllData(player, playerData);

        try (RestClientManager restClientManager = new RestClientManager()) {
            restClientManager.sendRequest(API_BASE_URL + "users/" + + recipient_id + "/?=&custom_fields[minecraft]=" + usernameMC,
                    RestClientManager.Method.POST, API_KEY, MediaType.parse(ContentTypes.PLAIN.getType()), "");
            restClientManager.sendRequest(API_BASE_URL + "users/" + recipient_id + "/?=&custom_fields[uuid]=" + player.getUniqueId(),
                    RestClientManager.Method.POST, API_KEY, MediaType.parse(ContentTypes.PLAIN.getType()), "");
        }catch(Exception e){
            Inferris.getInstance().getLogger().severe(e.getMessage());
        }
    }

    public static boolean isVerificationCodeExpired(Timestamp timestamp) {
        long now = System.currentTimeMillis();
        long fifteenMinutesAgo = now - (15 * 60 * 1000); // 15 minutes in milliseconds

        return timestamp.getTime() < fifteenMinutesAgo;
    }

    public static void setGroups(ProxiedPlayer player){
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("key");
            return list;
        }
        return Collections.emptyList();
    }
}