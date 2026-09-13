package com.nexusuniverse.admin.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * A generic clickable-button chest menu: every button is just a slot index mapped to a
 * Runnable. Used for the category-select menu and every per-plugin admin menu (Staff Powers,
 * Economy, Survival, Realms) -- none of them need anything more specific than "this slot does
 * this thing when clicked."
 */
public class ActionMenuHolder implements InventoryHolder {

    private Inventory inventory;
    private final Map<Integer, Runnable> actions = new HashMap<>();
    /** Runnable to invoke on a shift-click of the slot, if different from a plain click (used for
     * one destructive server-wide action that requires shift-click to actually confirm). */
    private final Map<Integer, Runnable> shiftActions = new HashMap<>();

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void setAction(int slot, Runnable action) {
        actions.put(slot, action);
    }

    public void setShiftAction(int slot, Runnable action) {
        shiftActions.put(slot, action);
    }

    public Runnable actionFor(int slot) {
        return actions.get(slot);
    }

    public Runnable shiftActionFor(int slot) {
        return shiftActions.get(slot);
    }
}
