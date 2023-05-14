package com.inferris.channel;

import com.inferris.Tags;
import net.md_5.bungee.api.ChatColor;

public enum Channels {
    STAFF(Tags.STAFF.getText(true)),
    SPECIAL(ChatColor.GOLD + "[Special]"),
    NONE("");

    private final String tag;

    Channels(String tag){
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public String getTag(boolean withSpace) {
        return tag + ChatColor.RESET + " ";
    }
}
