package com.nexusuniverse.admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * This class is the actual answer to "without allowing admin abuse" -- not a restriction on what
 * abilities can do, but making sure nothing done with them is ever quiet. Three layers, all of
 * every single ability use, every time, no exceptions and no opt-out for the admin using it:
 *
 *  1. Written to a persistent log file on disk (log.file in config.yml) -- survives restarts,
 *     can be pulled and reviewed by server ownership at any time, including by someone who
 *     wasn't online when it happened.
 *  2. Kept in an in-memory ring buffer for /nexusadmin log -- a quick recent-history check
 *     in-game without needing file/console access.
 *  3. Broadcast LIVE to anyone holding nexusadmin.watch, the moment it happens -- this is the
 *     layer that actually matters most for abuse resistance specifically: a log file only helps
 *     after the fact, but a real-time feed means other trusted staff see an ability being used
 *     as it happens, the same way it would if they were standing right there watching. Give
 *     nexusadmin.watch to people who don't have the abilities themselves too, for genuine
 *     independent oversight rather than admins only ever being watched by other admins.
 */
public class AuditLogger {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JavaPlugin plugin;
    private final AdminConfig config;
    private final Deque<String> recent = new ArrayDeque<>();

    public AuditLogger(JavaPlugin plugin, AdminConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    /**
     * @param actor    who used the ability
     * @param ability  short name, e.g. "vanish", "freeze", "smite"
     * @param target   the player/entity it was used on, or null if it doesn't target anyone (vanish, god, fly)
     * @param reason   the admin's stated reason, or null if this ability didn't require one
     */
    public void log(CommandSender actor, String ability, String target, String reason) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP);
        StringBuilder line = new StringBuilder();
        line.append('[').append(timestamp).append("] ").append(actor.getName()).append(" used ").append(ability);
        if (target != null) line.append(" on ").append(target);
        if (reason != null && !reason.isBlank()) line.append(" -- reason: ").append(reason);
        String entry = line.toString();

        appendToFile(entry);

        recent.addLast(entry);
        while (recent.size() > config.recentBufferSize()) {
            recent.removeFirst();
        }

        if (config.watchBroadcastEnabled()) {
            broadcastToWatchers(entry);
        }
    }

    public List<String> recent(int count) {
        List<String> all = new ArrayList<>(recent);
        int from = Math.max(0, all.size() - count);
        return all.subList(from, all.size());
    }

    private void appendToFile(String entry) {
        File file = new File(plugin.getDataFolder(), config.logFileName());
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(entry);
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            plugin.getLogger().warning("NexusAdmin: couldn't write to the audit log file (" + e.getMessage()
                    + ") -- this entry only exists in the in-memory recent buffer and console for now: " + entry);
        }
        // always also to console -- a second, tamper-resistant copy independent of this
        // plugin's own data folder, and visible to anyone with server console access
        // regardless of any in-game permission
        plugin.getLogger().info("[AUDIT] " + entry);
    }

    private void broadcastToWatchers(String entry) {
        String formatted = ChatColor.DARK_AQUA + "[Admin Watch] " + ChatColor.GRAY + entry;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("nexusadmin.watch")) {
                player.sendMessage(formatted);
            }
        }
    }
}
