package com.inferris.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandLocate extends Command implements TabExecutor {
    public CommandLocate(String name) {
        super(name);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] strings) {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }
}
