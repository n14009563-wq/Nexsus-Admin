package com.nexusuniverse.admin.menu;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Small shared helper for building the ItemStacks every menu in this package uses as buttons. */
public final class MenuIcons {
    private MenuIcons() {
    }

    public static ItemStack icon(Material material, ChatColor nameColor, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(nameColor + name);
        if (lore.length > 0) {
            List<String> loreLines = new ArrayList<>();
            for (String line : lore) {
                loreLines.add(ChatColor.GRAY + line);
            }
            meta.setLore(loreLines);
        }
        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack toggleIcon(boolean currentlyOn, String name, String... extraLore) {
        Material material = currentlyOn ? Material.LIME_DYE : Material.GRAY_DYE;
        ChatColor color = currentlyOn ? ChatColor.GREEN : ChatColor.GRAY;
        List<String> lore = new ArrayList<>();
        lore.add("Currently: " + (currentlyOn ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
        lore.add(ChatColor.YELLOW + "Click to toggle.");
        lore.addAll(Arrays.asList(extraLore));
        return icon(material, color, name, lore.toArray(new String[0]));
    }

    public static ItemStack backButton() {
        return icon(Material.ARROW, ChatColor.YELLOW, "← Back");
    }

    public static ItemStack closeButton() {
        return icon(Material.BARRIER, ChatColor.RED, "Close");
    }

    /** A clickable player head for the player-picker menu. */
    public static ItemStack playerHead(OfflinePlayer target, String... lore) {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = stack.getItemMeta();
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(target);
            meta = skullMeta;
        }
        meta.setDisplayName(ChatColor.YELLOW + target.getName());
        if (lore.length > 0) {
            List<String> loreLines = new ArrayList<>();
            for (String line : lore) {
                loreLines.add(ChatColor.GRAY + line);
            }
            meta.setLore(loreLines);
        }
        stack.setItemMeta(meta);
        return stack;
    }
}
