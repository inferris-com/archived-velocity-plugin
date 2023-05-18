package com.inferris.commands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.inferris.Inferris;
import com.inferris.PlayerTaskManager;
import com.inferris.player.PlayerDataManager;
import com.inferris.rank.RankRegistry;
import com.inferris.util.BungeeChannels;
import com.inferris.util.Subchannel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import org.w3c.dom.Text;

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
        if (sender instanceof ProxiedPlayer player) {
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
                if (playerName.toLowerCase().startsWith(partialPlayerName.toLowerCase())) {
                    playerNames.add(playerName);
                }
            }
            return playerNames;
        }
        return Collections.emptyList();
    }

    private void sendJoke(ProxiedPlayer receiver, String message) {
        if (cacheHandler == null) {
            cacheHandler = Caffeine.newBuilder().build();
        }
        PlayerCommandCache cache = cacheHandler.asMap().computeIfAbsent(receiver.getUniqueId(), uuid -> new PlayerCommandCache());
        cache.incrementCommandCount(receiver.getUniqueId());
        int commandCount = cache.getCommandCount(receiver.getUniqueId());

        switch (commandCount) {
            case 1 ->
                    receiver.sendMessage(new TextComponent(ChatColor.YELLOW + "Did... you just try messaging yourself?"));
            case 2 -> receiver.sendMessage(new TextComponent(ChatColor.YELLOW + "Are you okay...?"));

            case 3 -> receiver.sendMessage(new TextComponent(ChatColor.YELLOW + "I don't think you are lmfao"));
            case 4 -> receiver.sendMessage(new TextComponent(ChatColor.YELLOW + "Okay seriously are you okay"));

            default -> {
                if (commandCount >= 5 && commandCount <= 10) {
                    List<String> messageList = messageList(receiver);
                    int messageCount = messageList.size();
                    String randomMessage = generateRandomMessage(messageList);

                    receiver.sendMessage(new TextComponent(ChatColor.YELLOW + randomMessage));
                } else {
                    receiver.sendMessage(new TextComponent(ChatColor.RED + "OKAYYY! Okay."));
                    RankRegistry receiverRank = PlayerDataManager.getInstance().getPlayerData(receiver).getByBranch();
                    BaseComponent[] components = new ComponentBuilder().create();

                    PlayerTaskManager taskManager = new PlayerTaskManager(ProxyServer.getInstance().getScheduler());
                    Runnable task1 = () -> {
                        receiver.sendMessage(new ComponentBuilder().color(ChatColor.YELLOW).append("Fine. Is this what you wanted?").create());
                    };

                    Runnable task2 = () -> {
                        receiver.sendMessage(new TextComponent(ChatColor.GREEN + "Message sent!"));
                        receiver.sendMessage(new TextComponent(ChatColor.GRAY + "To " + receiverRank.getPrefix(true) + receiver.getName() + ChatColor.RESET + ": " + message));
                        receiver.sendMessage(new TextComponent(ChatColor.GRAY + "From " + receiverRank.getPrefix(true) + receiver.getName() + ChatColor.RESET + ": " + message));
                    };

                    Runnable task3 = () -> {
                        receiver.sendMessage(new TextComponent(ChatColor.YELLOW + "See? Was it worth it? Was it worth all of the key smashing, blood sweat and tears, just to message yourself?"));
                    };

                    Runnable task4 = () -> {
                        receiver.sendMessage(new TextComponent(ChatColor.YELLOW + "I mean, I'm not judging..."));
                    };

                    Runnable task5 = () -> {
                        receiver.sendMessage(new ComponentBuilder()
                                .append("[").color(ChatColor.DARK_GRAY).append("Alert").color(ChatColor.DARK_RED).append("] ").color(ChatColor.DARK_GRAY)
                                .append("Attention, attention, chatizens! Hold onto your funny bones, because we have a hilarious update for you! ").color(ChatColor.RESET)
                                .append(receiver.getName()).color(ChatColor.YELLOW).append(" has been caught red-handed engaging in some serious self-talk!" ).color(ChatColor.RESET)
                                        .append("Yes, you heard it right, folks. They've been having lively conversations with their mirror, captivating audiences of one!")
                                .append(" We kindly suggest sending them a referral to the illustrious Jokes Anonymous... because their self-comedy game needs a little help, to say the least. *wink*").color(ChatColor.RESET)
                                .create());
                    };

                    Runnable task6 = () -> {
                        receiver.sendMessage(new TextComponent(ChatColor.YELLOW + "This is the end of the joke. Seriously. There's nothing else here. Now go home."));
                    };

                    taskManager.addTaskForPlayer(task1, 2, TimeUnit.SECONDS);
                    taskManager.addTaskForPlayer(task2, 3, TimeUnit.SECONDS);
                    taskManager.addTaskForPlayer(task3, 4, TimeUnit.SECONDS);
                    taskManager.addTaskForPlayer(task4, 4, TimeUnit.SECONDS);
                    taskManager.addTaskForPlayer(task5, 4, TimeUnit.SECONDS);
                    taskManager.addTaskForPlayer(task6, 5, TimeUnit.SECONDS);
                }
            }
        }
    }

    private static String generateRandomMessage(List<String> messages) {
        Random random = new Random();
        int index = random.nextInt(messages.size());
        return messages.get(index);
    }

    private List<String> messageList(ProxiedPlayer player) {
        String name = player.getName();
        return List.of(
                "Are you trying to have a conversation with your mirror?",
                "Congratulations, you just invented self-messaging " + name + "!",
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
                "When they say 'love yourself,' I don't think this is what they meant " + name + ".",
                "Don't worry, we won't judge you... much.",
                "Are you trying to confuse your future self with past messages, " + name + "?",
                "If no one messages you, at least you can count on yourself.",
                "You're one step closer to having an imaginary friend.",
                "Some people talk to their plants, but you talk to yourself.",

                "Congratulations, you just won the 'Messaging Yourself' award!",
                "Hey there, future self! Did you forget to set your clock again?",
                "Note to self: Avoid having deep conversations with... yourself.",
                "Wow, talking to yourself has never been this entertaining!",
                "I heard talking to yourself is the first sign of genius... or madness.",
                "Breaking news: You've just discovered a new form of self-entertainment!",
                "Messaging yourself is like being in a one-person comedy show.",
                "Remember, the key to a healthy social life is messaging other people... just saying.",
                "Talking to yourself is a sign that you're exceptionally captivating.",
                "Roses are red, violets are blue, I'm messaging myself, and it's funnier than you.",
                "If only you could give yourself a high-five for messaging yourself!",
                "I bet your notifications are jealous of all the attention you're giving yourself.",
                "Next time you're feeling lonely, try messaging an actual human being. It works wonders!",
                "Talking to yourself is okay, as long as you don't start answering back... or do you?",
                "You're like a chatbot, but with a lot more personality... and no one else to talk to.",
                "Warning: Prolonged self-messaging may lead to uncontrollable laughter.",
                "Just a heads up, you might be the funniest person you know. Emphasis on 'might'.",
                "If only talking to yourself burned calories, you'd be the fittest person alive!",
                "Sending messages to yourself is a skill, and you, my friend, are a master.",
                "Don't worry, we won't judge. Well, maybe just a little... but with lots of love.",

                "Congratulations on becoming the CEO of the Lonely Messages Club. Membership has its privileges!",
                "Ah, the art of messaging oneself, where you're like a stand-up comedian performing to a sold-out crowd of... well, just you and your reflection in the mirror!",
                "Hey there, " + player.getName() + "! We've got a special recommendation just for you. Have you considered attending Jokes Anonymous?" +
                        " It seems your self-jokes are getting a little too cozy with just you. Let's find you an audience that can enjoy your comedic genius too!");
    }

    private TaskScheduler taskScheduler(ProxiedPlayer player, String message, long delay, TimeUnit timeUnit) {
        TaskScheduler taskScheduler = ProxyServer.getInstance().getScheduler();
        taskScheduler.schedule(Inferris.getInstance(), new Runnable() {
            @Override
            public void run() {
                player.sendMessage(new TextComponent(message));
            }
        }, delay, timeUnit);
        return taskScheduler;
    }
}
