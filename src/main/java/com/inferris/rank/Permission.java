package com.inferris.rank;

public enum Permission {
    // TODO: Make inheritance
    RESYNC("inferris.admin.resync", RankRegistry.ADMIN),
    SHOUT("inferris.admin.shout", RankRegistry.ADMIN),
    TEST("inf.test", RankRegistry.ADMIN),
    LITEBANS_BAN("litebans.ban", RankRegistry.ADMIN),
    LITEBANS_IPBAN("litebans.ipban", RankRegistry.ADMIN),
    LITEBANS_TEMPBAN("litebans.tempban", RankRegistry.ADMIN),
    LITEBANS_UNBAN("litebans.unban", RankRegistry.ADMIN),
    LITEBANS_MUTE("litebans.mute", RankRegistry.ADMIN),
    LITEBANS_IPMUTE("litebans.ipmute", RankRegistry.ADMIN),
    LITEBANS_TEMPMUTE("litebans.tempmute", RankRegistry.ADMIN),
    LITEBANS_UNMUTE("litebans.unmute", RankRegistry.ADMIN),
    LITEBANS_WARN("litebans.warn", RankRegistry.ADMIN),
    LITEBANS_UNWARN("litebans.unwarn", RankRegistry.ADMIN),
    LITEBANS_KICK("litebans.kick", RankRegistry.ADMIN),
    LITEBANS_HISTORY("litebans.history", RankRegistry.ADMIN),
    LITEBANS_WARNINGS("litebans.warnings", RankRegistry.ADMIN),
    CHANNEL_STAFF("inf.channel.staff", RankRegistry.ADMIN, RankRegistry.MOD, RankRegistry.HELPER);

    private final String permission;
    private final RankRegistry[] ranks;
    Permission(String permission, RankRegistry... ranks){
        this.permission = permission;
        this.ranks = ranks;
    }

    public RankRegistry[] getRanks() {
        return ranks;
    }

    public String getPermission() {
        return permission;
    }
}
