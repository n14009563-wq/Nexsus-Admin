package com.nexusuniverse.admin.menu;

import com.nexusuniverse.admin.AbilityManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.function.Consumer;

/**
 * Builds every menu this plugin's GUI shows. Every "does something to another plugin" action
 * here works the exact same way an admin typing the command themselves would: it dispatches that
 * plugin's own already-existing, already-permission-checked admin command via
 * Bukkit.dispatchCommand(admin, ...) rather than reaching into that plugin's internals. That
 * means every button in this menu behaves identically to (and stays in sync with) whatever that
 * plugin's real command does -- no separate logic to keep in sync, no reflection into private
 * classes, and a plugin that isn't installed just doesn't get a category shown for it.
 */
public final class AdminMenus {

    private static final int SIZE = 27;
    private static final int BACK_SLOT = 22;

    private final JavaPlugin plugin;
    private final AbilityManager abilities;

    public AdminMenus(JavaPlugin plugin, AbilityManager abilities) {
        this.plugin = plugin;
        this.abilities = abilities;
    }

    private void dispatch(Player admin, String command) {
        Bukkit.dispatchCommand(admin, command);
    }

    private boolean installed(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

    // ---------------------------------------------------------------- category menu

    public Inventory buildCategoryMenu(Player viewer) {
        ActionMenuHolder holder = new ActionMenuHolder();
        Inventory inv = Bukkit.createInventory(holder, SIZE, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Nexus Admin Panel");
        holder.setInventory(inv);

        inv.setItem(10, MenuIcons.icon(Material.NETHERITE_CHESTPLATE, ChatColor.GOLD, "Staff Powers",
                "Vanish, God Mode, Fly,", "freeze/smite/teleport/kill,", "and the audit log."));
        holder.setAction(10, () -> viewer.openInventory(buildStaffPowersMenu(viewer)));

        if (installed("NexusEconomy")) {
            inv.setItem(12, MenuIcons.icon(Material.GOLD_INGOT, ChatColor.GOLD, "Economy",
                    "Give/set/remove balance,", "override Hearts/Hunger/Oxygen."));
            holder.setAction(12, () -> viewer.openInventory(buildEconomyMenu(viewer)));
        }

        if (installed("NexusSurvival")) {
            inv.setItem(14, MenuIcons.icon(Material.WATER_BUCKET, ChatColor.GOLD, "Survival",
                    "Reload, status, cure a player,", "clear all tracked state."));
            holder.setAction(14, () -> viewer.openInventory(buildSurvivalMenu(viewer)));
        }

        if (installed("NexusRealms")) {
            inv.setItem(16, MenuIcons.icon(Material.GRASS_BLOCK, ChatColor.GOLD, "Realms",
                    "Land-protection bypass,", "open-terrain defaults."));
            holder.setAction(16, () -> viewer.openInventory(buildRealmsMenu(viewer)));
        }

        inv.setItem(BACK_SLOT, MenuIcons.closeButton());
        holder.setAction(BACK_SLOT, viewer::closeInventory);
        return inv;
    }

    // ---------------------------------------------------------------- staff powers

    public Inventory buildStaffPowersMenu(Player viewer) {
        ActionMenuHolder holder = new ActionMenuHolder();
        Inventory inv = Bukkit.createInventory(holder, SIZE, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Staff Powers");
        holder.setInventory(inv);

        inv.setItem(0, MenuIcons.toggleIcon(abilities.isVanished(viewer.getUniqueId()), "Vanish"));
        holder.setAction(0, () -> {
            dispatch(viewer, "nexusadmin vanish");
            viewer.openInventory(buildStaffPowersMenu(viewer));
        });

        inv.setItem(1, MenuIcons.toggleIcon(abilities.isGodMode(viewer.getUniqueId()), "God Mode"));
        holder.setAction(1, () -> {
            dispatch(viewer, "nexusadmin god");
            viewer.openInventory(buildStaffPowersMenu(viewer));
        });

        inv.setItem(2, MenuIcons.toggleIcon(viewer.getAllowFlight(), "Fly"));
        holder.setAction(2, () -> {
            dispatch(viewer, "nexusadmin fly");
            viewer.openInventory(buildStaffPowersMenu(viewer));
        });

        inv.setItem(4, MenuIcons.icon(Material.PACKED_ICE, ChatColor.AQUA, "Freeze a Player",
                "Locks them in place.", "Requires a typed reason."));
        holder.setAction(4, () -> viewer.openInventory(buildPlayerPicker(viewer, "Freeze Who?",
                target -> viewer.openInventory(buildTextPrompt("Reason for freezing " + target.getName(), false,
                        reason -> {
                            dispatch(viewer, "nexusadmin freeze " + target.getName() + " " + reason);
                            viewer.closeInventory();
                        })),
                () -> viewer.openInventory(buildStaffPowersMenu(viewer)))));

        inv.setItem(5, MenuIcons.icon(Material.TRIDENT, ChatColor.AQUA, "Smite a Player",
                "Calls down real lightning.", "Cooldown-limited."));
        holder.setAction(5, () -> viewer.openInventory(buildPlayerPicker(viewer, "Smite Who?",
                target -> {
                    dispatch(viewer, "nexusadmin smite " + target.getName());
                    viewer.closeInventory();
                },
                () -> viewer.openInventory(buildStaffPowersMenu(viewer)))));

        inv.setItem(6, MenuIcons.icon(Material.ENDER_PEARL, ChatColor.AQUA, "Teleport to a Player"));
        holder.setAction(6, () -> viewer.openInventory(buildPlayerPicker(viewer, "Teleport to Who?",
                target -> {
                    dispatch(viewer, "nexusadmin tp " + target.getName());
                    viewer.closeInventory();
                },
                () -> viewer.openInventory(buildStaffPowersMenu(viewer)))));

        inv.setItem(7, MenuIcons.icon(Material.LEAD, ChatColor.AQUA, "Bring a Player Here",
                "Teleports them to you.", "Requires a typed reason.", "Cooldown-limited."));
        holder.setAction(7, () -> viewer.openInventory(buildPlayerPicker(viewer, "Bring Who Here?",
                target -> viewer.openInventory(buildTextPrompt("Reason for bringing " + target.getName(), false,
                        reason -> {
                            dispatch(viewer, "nexusadmin tphere " + target.getName() + " " + reason);
                            viewer.closeInventory();
                        })),
                () -> viewer.openInventory(buildStaffPowersMenu(viewer)))));

        inv.setItem(8, MenuIcons.icon(Material.IRON_SWORD, ChatColor.AQUA, "Kill a Player",
                "Reason optional unless", "config requires one.", "Cooldown-limited."));
        holder.setAction(8, () -> viewer.openInventory(buildPlayerPicker(viewer, "Kill Who?",
                target -> viewer.openInventory(buildTextPrompt("Reason (optional)", true,
                        reason -> {
                            dispatch(viewer, "nexusadmin kill " + target.getName() + (reason.isBlank() ? "" : " " + reason));
                            viewer.closeInventory();
                        })),
                () -> viewer.openInventory(buildStaffPowersMenu(viewer)))));

        inv.setItem(13, MenuIcons.icon(Material.BOOK, ChatColor.YELLOW, "View Audit Log",
                "Prints the last 20 entries", "to your chat."));
        holder.setAction(13, () -> {
            dispatch(viewer, "nexusadmin log 20");
            viewer.closeInventory();
        });

        inv.setItem(BACK_SLOT, MenuIcons.backButton());
        holder.setAction(BACK_SLOT, () -> viewer.openInventory(buildCategoryMenu(viewer)));
        return inv;
    }

    // ---------------------------------------------------------------- economy

    public Inventory buildEconomyMenu(Player viewer) {
        ActionMenuHolder holder = new ActionMenuHolder();
        Inventory inv = Bukkit.createInventory(holder, SIZE, ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Economy Admin");
        holder.setInventory(inv);

        inv.setItem(0, MenuIcons.icon(Material.EMERALD, ChatColor.GREEN, "Give Money",
                "Adds to a player's balance."));
        holder.setAction(0, () -> viewer.openInventory(buildPlayerPicker(viewer, "Give Money to Who?",
                target -> viewer.openInventory(buildTextPrompt("Amount to give " + target.getName(), false,
                        amount -> {
                            dispatch(viewer, "economyadmin add " + target.getName() + " " + amount);
                            viewer.closeInventory();
                        })),
                () -> viewer.openInventory(buildEconomyMenu(viewer)))));

        inv.setItem(1, MenuIcons.icon(Material.GOLD_INGOT, ChatColor.GOLD, "Set Money",
                "Sets a player's balance", "to an exact amount."));
        holder.setAction(1, () -> viewer.openInventory(buildPlayerPicker(viewer, "Set Money for Who?",
                target -> viewer.openInventory(buildTextPrompt("New balance for " + target.getName(), false,
                        amount -> {
                            dispatch(viewer, "economyadmin set " + target.getName() + " " + amount);
                            viewer.closeInventory();
                        })),
                () -> viewer.openInventory(buildEconomyMenu(viewer)))));

        inv.setItem(2, MenuIcons.icon(Material.REDSTONE, ChatColor.RED, "Remove Money",
                "Withdraws from a player's", "balance."));
        holder.setAction(2, () -> viewer.openInventory(buildPlayerPicker(viewer, "Remove Money from Who?",
                target -> viewer.openInventory(buildTextPrompt("Amount to remove from " + target.getName(), false,
                        amount -> {
                            dispatch(viewer, "economyadmin remove " + target.getName() + " " + amount);
                            viewer.closeInventory();
                        })),
                () -> viewer.openInventory(buildEconomyMenu(viewer)))));

        inv.setItem(4, MenuIcons.icon(Material.COOKED_BEEF, ChatColor.AQUA, "Set Hearts",
                "Override for the Hearts", "shop upgrade tab."));
        holder.setAction(4, () -> viewer.openInventory(buildPlayerPicker(viewer, "Set Hearts for Who?",
                target -> viewer.openInventory(buildTextPrompt("Hearts level for " + target.getName(), false,
                        level -> {
                            dispatch(viewer, "economyadmin vitals " + target.getName() + " hearts " + level);
                            viewer.closeInventory();
                        })),
                () -> viewer.openInventory(buildEconomyMenu(viewer)))));

        inv.setItem(5, MenuIcons.icon(Material.BREAD, ChatColor.AQUA, "Set Hunger",
                "Override for the Hunger", "shop upgrade tab."));
        holder.setAction(5, () -> viewer.openInventory(buildPlayerPicker(viewer, "Set Hunger for Who?",
                target -> viewer.openInventory(buildTextPrompt("Hunger level for " + target.getName(), false,
                        level -> {
                            dispatch(viewer, "economyadmin vitals " + target.getName() + " hunger " + level);
                            viewer.closeInventory();
                        })),
                () -> viewer.openInventory(buildEconomyMenu(viewer)))));

        inv.setItem(6, MenuIcons.icon(Material.TURTLE_HELMET, ChatColor.AQUA, "Set Oxygen",
                "Override for the Oxygen", "shop upgrade tab."));
        holder.setAction(6, () -> viewer.openInventory(buildPlayerPicker(viewer, "Set Oxygen for Who?",
                target -> viewer.openInventory(buildTextPrompt("Oxygen level for " + target.getName(), false,
                        level -> {
                            dispatch(viewer, "economyadmin vitals " + target.getName() + " oxygen " + level);
                            viewer.closeInventory();
                        })),
                () -> viewer.openInventory(buildEconomyMenu(viewer)))));

        inv.setItem(BACK_SLOT, MenuIcons.backButton());
        holder.setAction(BACK_SLOT, () -> viewer.openInventory(buildCategoryMenu(viewer)));
        return inv;
    }

    // ---------------------------------------------------------------- survival

    public Inventory buildSurvivalMenu(Player viewer) {
        ActionMenuHolder holder = new ActionMenuHolder();
        Inventory inv = Bukkit.createInventory(holder, SIZE, ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Survival Admin");
        holder.setInventory(inv);

        inv.setItem(0, MenuIcons.icon(Material.REDSTONE_TORCH, ChatColor.YELLOW, "Reload Config",
                "Spawn/infection chances", "and severity apply immediately."));
        holder.setAction(0, () -> {
            dispatch(viewer, "nexussurvival reload");
            viewer.closeInventory();
        });

        inv.setItem(1, MenuIcons.icon(Material.PAPER, ChatColor.AQUA, "View My Status",
                "Thirst, Rad-O2, dirtiness,", "and infection, in chat."));
        holder.setAction(1, () -> {
            dispatch(viewer, "nexussurvival status");
            viewer.closeInventory();
        });

        inv.setItem(2, MenuIcons.icon(Material.SKELETON_SKULL, ChatColor.DARK_RED, "View Plague Deaths",
                "Recent plague-death log,", "in chat."));
        holder.setAction(2, () -> {
            dispatch(viewer, "nexussurvival plaguedeaths");
            viewer.closeInventory();
        });

        inv.setItem(3, MenuIcons.icon(Material.MILK_BUCKET, ChatColor.GREEN, "Cure a Player",
                "Force-cures whatever they", "currently have, no item needed."));
        holder.setAction(3, () -> viewer.openInventory(buildPlayerPicker(viewer, "Cure Who?",
                target -> {
                    dispatch(viewer, "nexussurvival cureplayer " + target.getName());
                    viewer.closeInventory();
                },
                () -> viewer.openInventory(buildSurvivalMenu(viewer)))));

        inv.setItem(4, MenuIcons.icon(Material.POTION, ChatColor.LIGHT_PURPLE, "Reset My Own Stats",
                "Full thirst/oxygen, no", "infection. Testing tool."));
        holder.setAction(4, () -> {
            dispatch(viewer, "nexussurvival resetme");
            viewer.closeInventory();
        });

        inv.setItem(6, MenuIcons.icon(Material.TNT, ChatColor.RED, "Clear ALL Tracked State",
                ChatColor.RED + "Server-wide -- every player.", ChatColor.YELLOW + "Shift-click to confirm."));
        holder.setAction(6, () -> viewer.sendMessage(ChatColor.RED
                + "That clears every player's tracked thirst/radiation/disease state server-wide -- shift-click it to confirm."));
        holder.setShiftAction(6, () -> {
            dispatch(viewer, "nexussurvival removeall");
            viewer.closeInventory();
        });

        inv.setItem(BACK_SLOT, MenuIcons.backButton());
        holder.setAction(BACK_SLOT, () -> viewer.openInventory(buildCategoryMenu(viewer)));
        return inv;
    }

    // ---------------------------------------------------------------- realms

    public Inventory buildRealmsMenu(Player viewer) {
        ActionMenuHolder holder = new ActionMenuHolder();
        Inventory inv = Bukkit.createInventory(holder, SIZE, ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Realms Admin");
        holder.setInventory(inv);

        inv.setItem(0, MenuIcons.icon(Material.SHIELD, ChatColor.AQUA, "Toggle My Land Bypass",
                "Build/use/fight anywhere,", "ignoring all land protection.",
                "Check chat for the new state --", "this menu can't read it back."));
        holder.setAction(0, () -> {
            dispatch(viewer, "realms bypass");
            viewer.closeInventory();
        });

        inv.setItem(2, MenuIcons.icon(Material.GRASS_BLOCK, ChatColor.GREEN, "Open Terrain Settings",
                "Default build/container/door/", "PvP/elytra rules for land", "nobody has claimed."));
        holder.setAction(2, () -> viewer.openInventory(buildTerrainMenu(viewer)));

        inv.setItem(BACK_SLOT, MenuIcons.backButton());
        holder.setAction(BACK_SLOT, () -> viewer.openInventory(buildCategoryMenu(viewer)));
        return inv;
    }

    private static final String[][] TERRAIN_TYPES = {
            {"build", "Build"},
            {"containers", "Containers"},
            {"doors", "Doors"},
            {"pvp", "PvP"},
            {"elytra", "Elytra"},
    };

    public Inventory buildTerrainMenu(Player viewer) {
        ActionMenuHolder holder = new ActionMenuHolder();
        Inventory inv = Bukkit.createInventory(holder, SIZE, ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Open Terrain Settings");
        holder.setInventory(inv);

        int slot = 0;
        for (String[] type : TERRAIN_TYPES) {
            String key = type[0];
            String label = type[1];
            inv.setItem(slot, MenuIcons.icon(Material.LIME_WOOL, ChatColor.GREEN, label + ": Turn ON"));
            holder.setAction(slot, () -> {
                dispatch(viewer, "realms openterrain " + key + " on");
                viewer.closeInventory();
            });
            slot++;
            inv.setItem(slot, MenuIcons.icon(Material.RED_WOOL, ChatColor.RED, label + ": Turn OFF"));
            holder.setAction(slot, () -> {
                dispatch(viewer, "realms openterrain " + key + " off");
                viewer.closeInventory();
            });
            slot += 2;
        }

        inv.setItem(BACK_SLOT, MenuIcons.backButton());
        holder.setAction(BACK_SLOT, () -> viewer.openInventory(buildRealmsMenu(viewer)));
        return inv;
    }

    // ---------------------------------------------------------------- shared: player picker

    public Inventory buildPlayerPicker(Player viewer, String title, Consumer<Player> onSelect, Runnable onBack) {
        PlayerPickerHolder holder = new PlayerPickerHolder();
        Inventory inv = Bukkit.createInventory(holder, SIZE, ChatColor.DARK_GRAY + title);
        holder.setInventory(inv);
        holder.setOnSelect(onSelect);
        holder.setOnBack(onBack);

        List<? extends Player> online = List.copyOf(Bukkit.getOnlinePlayers());
        int slot = 0;
        for (Player candidate : online) {
            if (slot >= BACK_SLOT) break; // more online than fit -- a paginated picker is a
            // reasonable future improvement once a server is regularly over ~21 concurrent staff targets
            inv.setItem(slot, MenuIcons.playerHead(candidate));
            holder.putPlayer(slot, candidate.getUniqueId());
            slot++;
        }

        inv.setItem(BACK_SLOT, MenuIcons.backButton());
        return inv;
    }

    // ---------------------------------------------------------------- shared: text prompt anvil

    public Inventory buildTextPrompt(String title, boolean allowBlank, Consumer<String> onConfirm) {
        TextPromptHolder holder = new TextPromptHolder("Type here...", allowBlank, onConfirm);
        Inventory inv = Bukkit.createInventory(holder, InventoryType.ANVIL, ChatColor.DARK_AQUA + title);
        holder.setInventory(inv);

        ItemStack seed = new ItemStack(Material.PAPER);
        var meta = seed.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + holder.placeholder());
        meta.getPersistentDataContainer().set(TextPromptHolder.seedTag(plugin),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
        seed.setItemMeta(meta);
        inv.setItem(0, seed);

        return inv;
    }
}
