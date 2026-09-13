package com.nexusuniverse.admin;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Names (case-insensitive) that always get the admin panel -- the join-chat button and the
     * bare /nexusadmin/menu command -- regardless of nexusadmin.menu. Exists for exactly the
     * situation this was built for: a server owner whose permissions setup doesn't actually grant
     * them a "default: op" permission node (a misconfigured or overriding permissions plugin, an
     * account that isn't flagged OP the way they expect, etc.) shouldn't be locked out of their
     * own admin panel over that. Manage this list by hand-editing config.yml and /nexusadmin
     * reload -- it's meant to be a short, rarely-changed owner/trusted-staff list, not a
     * replacement for the real permission.
     */
    public boolean isPriorityPlayer(String playerName) {
        return priorityPlayers().stream().anyMatch(name -> name.equalsIgnoreCase(playerName));
    }

    public List<String> priorityPlayers() {
        List<String> names = new ArrayList<>();
        for (String raw : plugin.getConfig().getStringList("menu.priority-players")) {
            if (raw != null && !raw.isBlank()) {
                names.add(raw.trim());
            }
        }
        return names;
    }
}
