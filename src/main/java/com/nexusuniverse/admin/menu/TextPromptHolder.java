package com.nexusuniverse.admin.menu;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.function.Consumer;

/**
 * A virtual anvil used purely as a text-input box, same trick NexusEconomy's shop search bar
 * uses: a disposable "seed" item sits in the input slot, the admin types into the rename field,
 * and clicking the (renamed) output item confirms it. See AdminMenuListener for the
 * PrepareAnvilEvent/InventoryClickEvent handling that makes this work, and for why the seed item
 * is tagged for cleanup on close.
 */
public class TextPromptHolder implements InventoryHolder {

    private Inventory inventory;
    private final String placeholder;
    private final Consumer<String> onConfirm;
    /** Optional: allows confirming with blank/placeholder text (e.g. an optional reason). */
    private final boolean allowBlank;

    public TextPromptHolder(String placeholder, boolean allowBlank, Consumer<String> onConfirm) {
        this.placeholder = placeholder;
        this.allowBlank = allowBlank;
        this.onConfirm = onConfirm;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public String placeholder() {
        return placeholder;
    }

    public boolean allowBlank() {
        return allowBlank;
    }

    public Consumer<String> onConfirm() {
        return onConfirm;
    }

    public static NamespacedKey seedTag(org.bukkit.plugin.Plugin plugin) {
        return new NamespacedKey(plugin, "admin_menu_prompt_seed");
    }
}
