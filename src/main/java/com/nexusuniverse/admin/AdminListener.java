package com.nexusuniverse.admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AdminListener implements Listener {

    private final AbilityManager abilities;
    private final AdminConfig config;

    public AdminListener(AbilityManager abilities, AdminConfig config) {
        this.abilities = abilities;
        this.config = config;
    }

    /**
     * The clickable "open the admin panel" chat button, sent once per join to anyone who can
     * actually use it -- nexusadmin.menu (default: op), same permission the bare /nexusadmin
     * command checks. Config-toggleable (menu.join-message-enabled) for anyone who'd rather rely
     * on typing /admin instead.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!config.joinMessageEnabled()) return;
        var player = event.getPlayer();
        if (!player.hasPermission("nexusadmin.menu")) return;

        Component button = Component.text("[Open Admin Panel]", NamedTextColor.AQUA)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/nexusadmin menu"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to open the staff admin panel (or run /admin any time).")));
        player.sendMessage(button);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!abilities.isFrozen(event.getPlayer().getUniqueId())) return;
        // only cancel actual movement, not just looking around -- a frozen player being unable
        // to even turn their camera reads as a bug, not a moderation tool
        if (event.getFrom().getX() == event.getTo().getX()
                && event.getFrom().getY() == event.getTo().getY()
                && event.getFrom().getZ() == event.getTo().getZ()) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        abilities.clearVanishAndGod(event.getPlayer().getUniqueId());
    }
}
