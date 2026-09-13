package com.nexusuniverse.admin;

import com.nexusuniverse.admin.menu.AdminMenus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NexusAdminCommand implements CommandExecutor {

    private final AdminConfig config;
    private final AbilityManager abilities;
    private final AuditLogger log;
    private final AdminMenus menus;

    private final Map<UUID, Long> smiteCooldown = new HashMap<>();
    private final Map<UUID, Long> killCooldown = new HashMap<>();
    private final Map<UUID, Long> tphereCooldown = new HashMap<>();

    public NexusAdminCommand(AdminConfig config, AbilityManager abilities, AuditLogger log, AdminMenus menus) {
        this.config = config;
        this.abilities = abilities;
        this.log = log;
        this.menus = menus;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length == 0) {
            // Bare /nexusadmin (or its /admin alias) opens the clickable menu for anyone allowed
            // to see it; players without nexusadmin.menu just get the usual usage text instead.
            if (player.hasPermission("nexusadmin.menu")) {
                player.openInventory(menus.buildCategoryMenu(player));
            } else {
                sendUsage(player);
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "menu" -> handleMenu(player);
            case "vanish" -> handleVanish(player);
            case "god" -> handleGod(player);
            case "fly" -> handleFly(player);
            case "freeze" -> handleFreeze(player, args);
            case "smite" -> handleSmite(player, args);
            case "tp" -> handleTp(player, args);
            case "tphere" -> handleTpHere(player, args);
            case "kill" -> handleKill(player, args);
            case "log" -> handleLog(player, args);
            default -> sendUsage(player);
        }
        return true;
    }

    private void handleMenu(Player player) {
        if (!requirePermission(player, "nexusadmin.menu")) return;
        player.openInventory(menus.buildCategoryMenu(player));
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Usage: /nexusadmin <menu|vanish|god|fly|freeze <player> <reason>|"
                + "smite <target>|tp <player>|tphere <player> <reason>|kill <target> [reason]|log [count]>");
    }

    private void handleVanish(Player player) {
        if (!requirePermission(player, "nexusadmin.vanish")) return;
        boolean nowVanished = abilities.toggleVanish(player);
        player.sendMessage(ChatColor.AQUA + (nowVanished ? "You're now vanished." : "You're visible again."));
        log.log(player, "vanish", null, nowVanished ? "vanished" : "un-vanished");
    }

    private void handleGod(Player player) {
        if (!requirePermission(player, "nexusadmin.god")) return;
        boolean nowGod = abilities.toggleGodMode(player);
        player.sendMessage(ChatColor.AQUA + (nowGod ? "God mode on." : "God mode off."));
        log.log(player, "god", null, nowGod ? "enabled" : "disabled");
    }

    private void handleFly(Player player) {
        if (!requirePermission(player, "nexusadmin.fly")) return;
        boolean nowFlying = abilities.toggleFly(player);
        player.sendMessage(ChatColor.AQUA + (nowFlying ? "Flight enabled." : "Flight disabled."));
        log.log(player, "fly", null, nowFlying ? "enabled" : "disabled");
    }

    private void handleFreeze(Player player, String[] args) {
        if (!requirePermission(player, "nexusadmin.freeze")) return;
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /nexusadmin freeze <player> <reason>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }
        String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
        if (config.reasonRequiredForFreeze() && reason.isBlank()) {
            player.sendMessage(ChatColor.RED + "A reason is required: /nexusadmin freeze <player> <reason>");
            return;
        }

        boolean nowFrozen = abilities.toggleFreeze(target.getUniqueId());
        player.sendMessage(ChatColor.AQUA + target.getName() + (nowFrozen ? " is now frozen." : " is unfrozen."));
        target.sendMessage(ChatColor.RED + "You've been " + (nowFrozen ? "frozen" : "unfrozen") + " by " + player.getName()
                + (nowFrozen && !reason.isBlank() ? ": " + reason : "") + ".");
        log.log(player, "freeze", target.getName(), (nowFrozen ? "froze" : "unfroze") + (reason.isBlank() ? "" : " -- " + reason));
    }

    private void handleSmite(Player player, String[] args) {
        if (!requirePermission(player, "nexusadmin.smite")) return;
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /nexusadmin smite <player>");
            return;
        }
        if (onCooldown(player, smiteCooldown, config.smiteCooldownSeconds())) return;

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        target.getWorld().strikeLightning(target.getLocation());
        player.sendMessage(ChatColor.AQUA + "Smote " + target.getName() + ".");
        log.log(player, "smite", target.getName(), null);
    }

    private void handleTp(Player player, String[] args) {
        if (!requirePermission(player, "nexusadmin.teleport")) return;
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /nexusadmin tp <player>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        player.teleport(target.getLocation());
        player.sendMessage(ChatColor.AQUA + "Teleported to " + target.getName() + ".");
        log.log(player, "tp", target.getName(), null);
    }

    private void handleTpHere(Player player, String[] args) {
        if (!requirePermission(player, "nexusadmin.teleport")) return;
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /nexusadmin tphere <player> <reason>");
            return;
        }
        if (onCooldown(player, tphereCooldown, config.tphereCooldownSeconds())) return;

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }
        String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
        if (config.reasonRequiredForTpHere() && reason.isBlank()) {
            player.sendMessage(ChatColor.RED + "A reason is required: /nexusadmin tphere <player> <reason>");
            return;
        }

        target.teleport(player.getLocation());
        target.sendMessage(ChatColor.YELLOW + "You were teleported to " + player.getName()
                + (reason.isBlank() ? "" : ": " + reason) + ".");
        player.sendMessage(ChatColor.AQUA + "Brought " + target.getName() + " to you.");
        log.log(player, "tphere", target.getName(), reason.isBlank() ? null : reason);
    }

    private void handleKill(Player player, String[] args) {
        if (!requirePermission(player, "nexusadmin.kill")) return;
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /nexusadmin kill <player-or-mob-name> [reason]");
            return;
        }
        if (onCooldown(player, killCooldown, config.killCooldownSeconds())) return;

        Player targetPlayer = Bukkit.getPlayerExact(args[1]);
        String reason = args.length > 2 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : "";

        if (targetPlayer != null) {
            if (config.reasonRequiredForKillOnPlayer() && reason.isBlank()) {
                player.sendMessage(ChatColor.RED + "A reason is required to kill a player: /nexusadmin kill <player> <reason>");
                return;
            }
            targetPlayer.setHealth(0.0);
            player.sendMessage(ChatColor.AQUA + "Killed " + targetPlayer.getName() + ".");
            log.log(player, "kill", targetPlayer.getName(), reason.isBlank() ? null : reason);
            return;
        }

        // not an online player -- look for the nearest matching-type mob within the player's
        // world, since there's no server-wide "entity by name" lookup
        LivingEntity nearestMob = null;
        double nearestDistance = Double.MAX_VALUE;
        for (org.bukkit.entity.Entity nearby : player.getWorld().getEntities()) {
            if (!(nearby instanceof LivingEntity living) || living instanceof Player) continue;
            if (!living.getType().name().equalsIgnoreCase(args[1])) continue;
            double distance = living.getLocation().distanceSquared(player.getLocation());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestMob = living;
            }
        }
        if (nearestMob == null) {
            player.sendMessage(ChatColor.RED + "No online player or nearby mob of that type found.");
            return;
        }
        nearestMob.setHealth(0.0);
        player.sendMessage(ChatColor.AQUA + "Killed the nearest " + args[1] + ".");
        log.log(player, "kill", args[1] + " (mob)", reason.isBlank() ? null : reason);
    }

    private void handleLog(Player player, String[] args) {
        if (!requirePermission(player, "nexusadmin.log")) return;
        int count = 20;
        if (args.length >= 2) {
            try {
                count = Math.max(1, Integer.parseInt(args[1]));
            } catch (NumberFormatException ignored) {
            }
        }
        player.sendMessage(ChatColor.GRAY + "--- Last " + count + " admin actions ---");
        for (String entry : log.recent(count)) {
            player.sendMessage(ChatColor.GRAY + entry);
        }
    }

    private boolean requirePermission(Player player, String permission) {
        if (player.hasPermission(permission)) return true;
        player.sendMessage(ChatColor.RED + "No permission.");
        return false;
    }

    private boolean onCooldown(Player player, Map<UUID, Long> cooldowns, int cooldownSeconds) {
        if (cooldownSeconds <= 0) return false;
        long now = System.currentTimeMillis();
        Long last = cooldowns.get(player.getUniqueId());
        if (last != null && now - last < cooldownSeconds * 1000L) {
            long remaining = (cooldownSeconds * 1000L - (now - last) + 999) / 1000;
            player.sendMessage(ChatColor.RED + "That's on cooldown for another " + remaining + "s.");
            return true;
        }
        cooldowns.put(player.getUniqueId(), now);
        return false;
    }
}
