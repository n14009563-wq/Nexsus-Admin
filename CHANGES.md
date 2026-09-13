# NexusAdmin changelog

## v0.2.1 -- priority-players safety net

Built from a bug report right after v0.2.0 shipped: the admin panel's join-chat button never
appeared for the server owner. The likely cause isn't the menu logic itself -- it's that
`nexusadmin.menu` defaults to `op`, and something in this server's actual permissions setup (a
permissions plugin overriding defaults, an account not flagged OP the way it looks, etc.) isn't
resolving that node `true` for their account the way it's expected to. Rather than trying to
diagnose a third-party permissions setup this plugin doesn't control, this adds a second,
independent path to the same access: `menu.priority-players` in config.yml, a plain list of player
names (case-insensitive) who always get the join-chat button and `/nexusadmin`/`/admin` menu
access, regardless of what `nexusadmin.menu` resolves to for them.

**Where it's checked:** `AdminListener#onJoin` (the join-chat button) and both menu-opening paths
in `NexusAdminCommand` (bare `/nexusadmin` with no arguments, and `/nexusadmin menu` explicitly) now
all use the same rule -- `nexusadmin.menu` OR listed in `menu.priority-players` -- via a new shared
`AdminConfig#isPriorityPlayer(String)` / `canOpenMenu(Player)` check. Nothing about the actual
`nexusadmin.menu` permission changed; this is purely an additional way in, for exactly the "I should
obviously have access to my own server's admin panel" case.

**Config:** `menu.priority-players` defaults to `[RealSociety5107]`. It's meant to stay a short,
rarely-touched list -- a real fix to the underlying permissions setup is still worth doing when
there's time, this just makes sure no one's locked out of their own panel in the meantime.

**Verification:** recompiled the whole plugin (all v0.1.0/v0.2.0 files plus this change) against
the same stub library used for v0.2.0, extended with nothing new (no new Bukkit API surface was
needed for this fix) -- `javac -Xlint:all -Werror`: 0 errors, 0 warnings.

## v0.2.0 -- the clickable admin panel

Built from: "let's make one called Nexus admins... every time I log in, there's a clickable
button in Game Chat that'll open up a menu, or I can run the command slash admin, and it'll open
up the menu... any admin operator buttons, switches, anything that only operators can do... a
different category for each of the mods... click it open and then see all the options and then be
able to interact inside that category with whatever I'm trying to do... only gonna be able to be
used by admins and operators, highest level operators."

**Scope for this first version, agreed up front:** the menu is built directly into NexusAdmin
(not a separate plugin) and covers four categories -- Staff Powers (native to this plugin),
Economy, Survival, and Realms -- with real admin controls per category (toggle switches, targeted
actions, and this plugin's own settings), not just placeholder buttons. Combat wasn't included
because its source wasn't available this session; adding it later is a small, self-contained
follow-up once it is.

**How categories reach into other plugins:** every button that acts on NexusEconomy, NexusSurvival,
or NexusRealms dispatches that plugin's own already-existing admin command
(`Bukkit.dispatchCommand(admin, "economyadmin add ...")`, `"nexussurvival reload"`,
`"realms bypass"`, etc.) exactly as if the admin had typed it. That was a deliberate choice over
reflecting into those plugins' internal manager classes: it means every button behaves identically
to, and never drifts out of sync with, that plugin's real command -- including its permission
checks, cooldowns, and chat feedback -- with zero duplicated logic and zero risk of guessing a
private method signature wrong. A category is only shown at all if that plugin is actually
installed and enabled (`softdepend`, checked live via `isPluginEnabled`), so a server without
NexusEconomy simply never sees an Economy button.

**Opening it:** the bare `/nexusadmin` command (and its existing `/admin`/`/na` aliases) now opens
the menu directly for anyone with the new `nexusadmin.menu` permission (default: op) instead of
printing usage text; `/nexusadmin menu` does the same explicitly. A one-time clickable
`[Open Admin Panel]` chat button is also sent on every join to the same permission, built as a
real Adventure `Component` with a `runCommand` click event (config-toggleable:
`menu.join-message-enabled`).

**Staff Powers category:** Vanish/God Mode/Fly as real toggle buttons whose icon color reflects
current state (green = on) -- this plugin already tracks that state itself, so these render
accurately, unlike the cross-plugin categories below. Freeze/Smite/Teleport-to/Bring-here/Kill open
a player-picker sub-menu (one clickable head per online player) instead of asking the admin to
type a name; the two that require a reason (freeze, bring-here) chain into a text-input anvil
prompt afterward. View Audit Log dumps the last 20 entries to chat.

**Economy category:** Give/Set/Remove money and Set Hearts/Hunger/Oxygen each open the same
player-picker, then a text-input anvil for the amount/level, then dispatch `/economyadmin`
exactly as documented.

**Survival category:** Reload, View My Status, View Plague Deaths, and Reset My Own Stats are
one-click (no target needed). Cure a Player uses the player picker. Clear ALL Tracked State is
server-wide and destructive, so it deliberately does NOT fire on a normal click -- clicking it
explains that a shift-click is required, and only the shift-click actually dispatches
`nexussurvival removeall`.

**Realms category:** Toggle My Land Bypass dispatches `/realms bypass` directly (its own chat
reply is the only way to see the new state -- this plugin has no way to query it back without
reflecting into Realms' internals, which was avoided on purpose). Open Terrain Settings opens a
sub-menu with an explicit ON and OFF button per terrain type (build/containers/doors/pvp/elytra)
rather than a single ambiguous toggle, for the same reason: this menu can't read Realms' current
setting, so it's honest about what each button does rather than pretending to track state it
doesn't have.

**Anvil text-input pattern:** reused, not reinvented -- the same technique NexusEconomy's shop
search bar already uses (a disposable "seed" paper in a virtual anvil's input slot, forced to
always populate the output slot with whatever's currently typed, read back off the output item
when clicked). Shared here as one generic `TextPromptHolder`/listener pair instead of copy-pasted
per use.

**Verification:** built a from-scratch stub library covering this plugin's full menu/GUI surface
(`InventoryHolder`/`Inventory`/`ItemStack`/`ItemMeta`/`SkullMeta`, `InventoryClickEvent`/
`PrepareAnvilEvent`/`InventoryCloseEvent`, `PlayerMoveEvent`, the Adventure `Component`/`ClickEvent`
/`HoverEvent` builder chain, and the extra `Player`/`LivingEntity`/`World`/`Location` methods this
release's code and the pre-existing v0.1.0 code both needed) and compiled the whole plugin --
including the original v0.1.0 files, which had never actually been compiled against a stub library
before -- with `javac -Xlint:all -Werror`: 0 errors, 0 warnings.

## v0.1.0 -- first release

Built from: "is it possible to build an admin plugin with cool badass abilities that doesn't
enable admin abuse?" Delivered vanish, god mode, fly, freeze, smite, teleport/bring-here, and
instant-kill, with the anti-abuse design as three mandatory layers on every single use: a
persistent audit-log file plus console line, an in-memory recent buffer for `/nexusadmin log`, and
a live broadcast to anyone holding the separate `nexusadmin.watch` permission -- meant for peer
oversight by trusted staff who don't necessarily have the abilities themselves. The abilities that
act on another player without asking them first (freeze, bring-here, kill-on-player) require a
typed reason, logged verbatim; the more drastic ones are cooldown-limited per admin.
