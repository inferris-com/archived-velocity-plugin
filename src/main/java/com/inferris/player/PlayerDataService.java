package com.inferris.player;

import com.inferris.player.coins.Coins;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import net.md_5.bungee.api.ChatColor;

import java.util.UUID;

public interface PlayerDataService {

    UUID getUuid();
    String getUsername();
    Rank getRank();
    Profile getProfile();
    Coins getCoins();
    Channel getChannel();
    VanishState getVanishState();
    ChatColor getNameColor();
    boolean isStaff();
    void setCoins(int amount);
    void setRank(Branch branch, int level);
    void setChannel(Channel channel);
    void setVanishState(VanishState vanishState);
}
