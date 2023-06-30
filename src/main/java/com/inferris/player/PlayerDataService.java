package com.inferris.player;

import com.inferris.player.coins.Coins;
import com.inferris.player.registry.Registry;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import net.md_5.bungee.api.ChatColor;

public interface PlayerDataService {

    Registry getRegistry();
    Rank getRank();
    Profile getProfile();
    Coins getCoins();
    Channels getChannel();
    VanishState getVanishState();
    ChatColor getNameColor();
    void setCoins(int amount);
    void setRank(Branch branch, int level);
    void setChannel(Channels channel);
    void setVanishState(VanishState vanishState);
}
