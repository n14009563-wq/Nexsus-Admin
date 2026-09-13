package com.nexusuniverse.admin.menu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * The one shared click/anvil-prepare/close handler for every menu AdminMenus builds. Nothing in
 * here is menu-specific -- each holder type (ActionMenuHolder, PlayerPickerHolder,
 * TextPromptHolder) already carries exactly what it needs to react (a Runnable per slot, a
 * Consumer<Player>, a Consumer<String>), so this class is just the wiring that calls them.
 */
public final class AdminMenuListener implements Listener {

    private final JavaPlugin plugin;

    public AdminMenuListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        Object holder = top.getHolder();

        if (holder instanceof ActionMenuHolder actionMenu) {
            event.setCancelled(true);
            if (event.getClickedInventory() != top) return;
            boolean shift = event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT;
            Runnable action = shift ? actionMenu.shiftActionFor(event.getSlot()) : null;
            if (action == null) {
                action = actionMenu.actionFor(event.getSlot());
            }
            if (action != null) {
                action.run();
            }
            return;
        }

        if (holder instanceof PlayerPickerHolder picker) {
            event.setCancelled(true);
            if (event.getClickedInventory() != top) return;
            if (!(event.getWhoClicked() instanceof Player admin)) return;

            UUID targetId = picker.playerAt(event.getSlot());
            if (targetId != null) {
                Player target = Bukkit.getPlayer(targetId);
                if (target == null || !target.isOnline()) {
                    admin.sendMessage(ChatColor.RED + "That player just went offline -- try again.");
                    return;
                }
                if (picker.onSelect() != null) {
                    picker.onSelect().accept(target);
                }
                return;
            }
            if (picker.onBack() != null) {
                picker.onBack().run();
            }
            return;
        }

        if (holder instanceof TextPromptHolder prompt) {
            event.setCancelled(true);
            if (event.getClickedInventory() == null || event.getClickedInventory() != top) return;
            if (event.getSlot() != 2) return; // only the anvil's output slot confirms
            if (!(event.getWhoClicked() instanceof Player admin)) return;

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            String typed = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            boolean blank = typed == null || typed.isBlank() || typed.equals(prompt.placeholder());
            if (blank && !prompt.allowBlank()) {
                admin.sendMessage(ChatColor.RED + "Type something first, then click it to confirm.");
                return;
            }
            admin.closeInventory();
            if (prompt.onConfirm() != null) {
                prompt.onConfirm().accept(blank ? "" : typed);
            }
        }
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!(event.getInventory().getHolder() instanceof TextPromptHolder prompt)) return;

        AnvilInventory anvil = event.getInventory();
        anvil.setRepairCost(0);

        ItemStack seed = anvil.getItem(0);
        if (seed == null) {
            event.setResult(null);
            return;
        }

        String typed = anvil.getRenameText();
        ItemStack result = seed.clone();
        ItemMeta meta = result.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + (typed == null || typed.isBlank() ? prompt.placeholder() : typed));
        result.setItemMeta(meta);
        event.setResult(result);
    }

    /**
     * A virtual anvil hands back whatever's still sitting in its slots when it closes, same as
     * any other inventory. Without this, closing a text-prompt anvil without confirming would
     * leave the admin holding a stray "Type here..." paper -- so any left in an admin's own
     * inventory get swept up one tick after any of our anvils close (same pattern NexusEconomy's
     * shop search bar uses).
     */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof TextPromptHolder)) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            ItemStack[] contents = player.getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack stack = contents[i];
                if (stack == null || !stack.hasItemMeta()) continue;
                Boolean isSeed = stack.getItemMeta().getPersistentDataContainer()
                        .get(TextPromptHolder.seedTag(plugin), PersistentDataType.BOOLEAN);
                if (Boolean.TRUE.equals(isSeed)) {
                    player.getInventory().setItem(i, null);
                }
            }
        });
    }
}
