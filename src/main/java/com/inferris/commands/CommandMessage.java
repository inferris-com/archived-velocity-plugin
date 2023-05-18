package com.inferris.commands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.inferris.Inferris;
import com.inferris.player.PlayerDataManager;
import com.inferris.rank.RankRegistry;
import com.inferris.util.BungeeChannels;
import com.inferris.util.Subchannel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CommandMessage extends Command implements TabExecutor {
    private static Cache<UUID, PlayerCommandCache> cacheHandler;

    public CommandMessage(String name) {
        super(name);
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;
        if(sender instanceof ProxiedPlayer player) {
            if (length == 0 || length == 1) {
                player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /message <player> <message>"));
            }
            if (length >= 2) {
                if (ProxyServer.getInstance().getPlayer(args[0]) != null) {
                    ProxiedPlayer receiver = ProxyServer.getInstance().getPlayer(args[0]);
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();

                    /*
                    We might not need rankdata here, because we may make the registry update from Spigot, and/or
                    put our setrank command on BungeeCord.
                     */

/*
                    out.writeUTF(BungeeChannels.DIRECT_MESSAGE.getName());
                    out.writeUTF(Subchannel.REQUEST.toLowerCase());
                    out.writeUTF("rankdata");
*/

                    out.writeUTF(BungeeChannels.DIRECT_MESSAGE.getName());
                    out.writeUTF(Subchannel.FORWARD.toLowerCase());
                    String message = String.join(" ", Arrays.copyOfRange(args, 1, length));
                    out.writeUTF(message);

                    receiver.getServer().sendData(BungeeChannels.DIRECT_MESSAGE.getName(), out.toByteArray());
                    RankRegistry playerRank = PlayerDataManager.getInstance().getPlayerData(player).getByBranch();
                    RankRegistry receiverRank = PlayerDataManager.getInstance().getPlayerData(receiver).getByBranch();
                    message = message + ChatColor.RESET;
                    boolean toSelf;

                    if (receiver.getUniqueId() == player.getUniqueId()) {
                        sendJoke(receiver, message);
                        return;
                    }
                    player.sendMessage(new TextComponent(ChatColor.GREEN + "Message sent!"));
                    player.sendMessage(new TextComponent(ChatColor.GRAY + "To " + receiverRank.getPrefix(true) + receiver.getName() + ChatColor.RESET + ": " + message));
                    receiver.sendMessage(new TextComponent(ChatColor.GRAY + "From " + playerRank.getPrefix(true) + player.getName() + ChatColor.RESET + ": " + message));
                }
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {

        if (args.length == 1 && sender instanceof ProxiedPlayer player) {
            String partialPlayerName = args[0];
            List<String> playerNames = new ArrayList<>();
            for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
                String playerName = proxiedPlayers.getName();
                if(playerName.toLowerCase().startsWith(partialPlayerName.toLowerCase())){
                    playerNames.add(playerName);
                }
            }
            return playerNames;
        }
        return Collections.emptyList();
    }

    private void sendJoke(ProxiedPlayer receiver, String message) {
        if(cacheHandler == null){
            cacheHandler = Caffeine.newBuilder().build();
        }
        PlayerCommandCache cache = cacheHandler.asMap().computeIfAbsent(receiver.getUniqueId(), uuid -> new PlayerCommandCache());
        cache.incrementCommandCount(receiver.getUniqueId());
        int commandCount = cache.getCommandCount(receiver.getUniqueId());

        switch (commandCount){
            case 1 -> {
                receiver.sendMessage(new TextComponent(ChatColor.YELLOW + "Did... you just try messaging yourself?"));
            }
            case 2 -> {
                receiver.sendMessage(new TextComponent(ChatColor.YELLOW + "Are you okay...?"));
            }
            case 3 -> {
                receiver.sendMessage(new TextComponent(ChatColor.YELLOW + "I don't think you are lmfao"));
            }
            case 4 -> {
                receiver.sendMessage(new TextComponent(ChatColor.YELLOW + "Okay seriously are you okay"));
            }
            case 5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20 -> {
                List<String> messages = List.of(
                        "Are you trying to have a conversation with your mirror?",
                        "Congratulations, you just invented self-messaging!",
                        "Talking to yourself is a sign of genius... or something.",
                        "Well, that's one way to keep yourself entertained.",
                        "You must be your own biggest fan.",
                        "You're messaging yourself? You should get an award for that.",
                        "Why waste time on others when you can talk to yourself, right?",
                        "Just a friendly reminder: you can't text yourself into a better mood.",
                        "Is this your way of testing the limits of self-awareness?",
                        "It's official, you're the most popular person in your own inbox.",
                        "Remember, conversations with yourself should be kept private.",
                        "Messaging yourself is a surefire way to never be left on read.",
                        "You've reached the pinnacle of self-reliance: messaging yourself.",
                        "When they say 'love yourself,' I don't think this is what they meant.",
                        "Don't worry, we won't judge you... much.",
                        "Are you trying to confuse your future self with past messages?",
                        "If no one messages you, at least you can count on yourself.",
                        "You're one step closer to having an imaginary friend.",
                        "Some people talk to their plants, but you talk to yourself."
                );

                String randomMessage = generateRandomMessage(messages);

                receiver.sendMessage(new TextComponent(ChatColor.YELLOW + randomMessage));


            }
        }

//        ProxyServer.getInstance().getScheduler().schedule(Inferris.getInstance(), new Runnable() {
//            @Override
//            public void run() {
//                receiver.sendMessage(new TextComponent(ChatColor.YELLOW + "I'll read it back to you can sit in a corner and realize what you have just done: " + ChatColor.RESET + message));
//
//            }
//        }, 3, TimeUnit.SECONDS);
//        return;
    }

    private static String generateRandomMessage(List<String> messages) {
        Random random = new Random();
        int index = random.nextInt(messages.size());
        return messages.get(index);
    }
}
