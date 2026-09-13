package com.nexusuniverse.admin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks who currently has each toggleable ability active. Deliberately in-memory only, not
 * persisted across a restart -- vanish/god/fly/freeze are all meant to be an active admin
 * session's tools, not a standing state that silently survives a server restart and gets
 * forgotten about (which would itself be a quiet-abuse risk: nobody wants to discover days later
 * that they were still invulnerable, or still invisible, because a toggle from a past session
 * never actually got switched back off).
 */
public class AbilityManager {

    private final Set<UUID> vanished = new HashSet<>();
    private final Set<UUID> godMode = new HashSet<>();
    private final Set<UUID> frozen = new HashSet<>();

    public boolean isVanished(UUID playerId) {
        return vanished.contains(playerId);
    }

    public boolean toggleVanish(Player player) {
        boolean nowVanished = !vanished.contains(player.getUniqueId());
        if (nowVanished) {
            vanished.add(player.getUniqueId());
        } else {
            vanished.remove(player.getUniqueId());
        }

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.hasPermission("nexusadmin.seevanish")) continue;
            if (nowVanished) {
                other.hidePlayer(Bukkit.getPluginManager().getPlugin("NexusAdmin"), player);
            } else {
                other.showPlayer(Bukkit.getPluginManager().getPlugin("NexusAdmin"), player);
            }
        }
        return nowVanished;
    }

    public boolean isGodMode(UUID playerId) {
        return godMode.contains(playerId);
    }

    public boolean toggleGodMode(Player player) {
        boolean nowGod = !godMode.contains(player.getUniqueId());
        if (nowGod) {
            godMode.add(player.getUniqueId());
        } else {
            godMode.remove(player.getUniqueId());
        }
        player.setInvulnerable(nowGod);
        return nowGod;
    }

    public boolean toggleFly(Player player) {
        boolean nowFlying = !player.getAllowFlight();
        player.setAllowFlight(nowFlying);
        player.setFlying(nowFlying);
        return nowFlying;
    }

    public boolean isFrozen(UUID playerId) {
        return frozen.contains(playerId);
    }

    /** @return true if now frozen, false if this call unfroze them */
    public boolean toggleFreeze(UUID playerId) {
        if (frozen.contains(playerId)) {
            frozen.remove(playerId);
            return false;
        }
        frozen.add(playerId);
        return true;
    }

    /** Clears vanish/god mode on disconnect -- personal session tools with no reason to survive a reconnect. Deliberately does NOT clear freeze: if someone was frozen for a moderation reason and logs off mid-investigation, staying frozen on rejoin is the safer default; an admin can always toggle it back off. */
    public void clearVanishAndGod(UUID playerId) {
        vanished.remove(playerId);
        godMode.remove(playerId);
    }
}
