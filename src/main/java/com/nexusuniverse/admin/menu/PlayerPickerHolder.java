package com.nexusuniverse.admin.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * "Pick an online player" sub-menu -- one player head per online player, click one to run
 * onSelect against them. Used by every staff-power/plugin-admin action that targets a specific
 * player (freeze, smite, give money, cure a disease, etc.) instead of asking the admin to type a
 * name.
 */
public class PlayerPickerHolder implements InventoryHolder {

    private Inventory inventory;
    private final Map<Integer, UUID> slotPlayers = new HashMap<>();
    private Consumer<Player> onSelect;
    private Runnable onBack;

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void putPlayer(int slot, UUID playerId) {
        slotPlayers.put(slot, playerId);
    }

    public UUID playerAt(int slot) {
        return slotPlayers.get(slot);
    }

    public void setOnSelect(Consumer<Player> onSelect) {
        this.onSelect = onSelect;
    }

    public Consumer<Player> onSelect() {
        return onSelect;
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    public Runnable onBack() {
        return onBack;
    }
}
