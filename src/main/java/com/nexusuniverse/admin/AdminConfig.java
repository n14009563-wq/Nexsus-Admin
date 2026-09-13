package com.nexusuniverse.admin;

import org.bukkit.plugin.java.JavaPlugin;

public class AdminConfig {

    private final JavaPlugin plugin;

    public AdminConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    public String logFileName() {
        return plugin.getConfig().getString("log.file", "audit-log.txt");
    }

    public int recentBufferSize() {
        return Math.max(1, plugin.getConfig().getInt("log.recent-buffer-size", 200));
    }

    public boolean watchBroadcastEnabled() {
        return plugin.getConfig().getBoolean("watch-broadcast.enabled", true);
    }

    public int smiteCooldownSeconds() {
        return Math.max(0, plugin.getConfig().getInt("cooldowns.smite-seconds", 5));
    }

    public int killCooldownSeconds() {
        return Math.max(0, plugin.getConfig().getInt("cooldowns.kill-seconds", 3));
    }

    public int tphereCooldownSeconds() {
        return Math.max(0, plugin.getConfig().getInt("cooldowns.tphere-seconds", 10));
    }

    public boolean reasonRequiredForFreeze() {
        return plugin.getConfig().getBoolean("require-reason.freeze", true);
    }

    public boolean reasonRequiredForTpHere() {
        return plugin.getConfig().getBoolean("require-reason.tphere", true);
    }

    public boolean reasonRequiredForKillOnPlayer() {
        return plugin.getConfig().getBoolean("require-reason.kill-on-player", true);
    }

    public boolean joinMessageEnabled() {
        return plugin.getConfig().getBoolean("menu.join-message-enabled", true);
    }
}
