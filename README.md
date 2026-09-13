# NexusAdmin v0.2.1

Powerful admin/staff abilities with built-in abuse resistance, plus a clickable in-game admin
panel: a chat button on join, or the bare `/admin` command, opens a category menu -- Staff Powers
(native to this plugin) plus Economy, Survival, and Realms when those plugins are installed --
with the real admin controls each of those plugins already offers, without needing to type or even
remember the underlying commands.

## Opening the panel

- Click the `[Open Admin Panel]` button sent in chat the moment you join (config-toggleable:
  `menu.join-message-enabled`), or
- Run `/admin` (or `/nexusadmin`, `/na`) with no arguments, or
- Run `/nexusadmin menu` explicitly.

All three require `nexusadmin.menu` (default: `op`) -- the same "highest level operators" standard
as the abilities themselves -- **or** being listed by name in `menu.priority-players` in
config.yml, a safety net for a server owner/trusted staffer whose permissions setup doesn't
actually grant them that node (see the Config section below). Anyone who fails both checks who
runs `/nexusadmin` still gets the plain usage text, same as before this menu existed.

## How the cross-plugin categories work

Every button in the Economy, Survival, and Realms categories dispatches that plugin's own
already-existing admin command -- `/economyadmin`, `/nexussurvival`, `/realms` -- exactly as if you
had typed it yourself. That means:

- Behavior always matches that plugin's real command: same permission checks, same cooldowns
  (where it has any), same chat feedback.
- Nothing here can drift out of sync with how that plugin actually works, because it IS that
  plugin's own code running, just triggered by a click instead of a keystroke.
- A category only appears at all if that plugin is actually installed and enabled on this server.
  No NexusEconomy? No Economy button. Nothing breaks either way.

## Staff Powers

- **Vanish / God Mode / Fly** -- toggle buttons. Their color reflects the real current state
  (green = on) since this plugin tracks that itself.
- **Freeze a Player / Smite a Player / Teleport to a Player / Bring a Player Here / Kill a
  Player** -- each opens a player-picker (a clickable head per player currently online) instead of
  asking you to type a name. Freeze and Bring Here then ask for a reason via a text-input prompt
  (an anvil you type into, same mechanic as NexusEconomy's shop search bar) -- required for both,
  matching `require-reason` in config.yml. Kill's reason prompt can be left blank unless
  `require-reason.kill-on-player` says otherwise.
- **View Audit Log** -- prints your last 20 audit-log entries to chat.

## Economy (only shown if NexusEconomy is installed)

Give / Set / Remove Money, and Set Hearts / Hunger / Oxygen (the shop upgrade-tab override) --
each: pick a player, type an amount in the anvil prompt, done. Runs `/economyadmin` under the
hood, so an invalid amount gets exactly the error `/economyadmin` would normally give you.

## Survival (only shown if NexusSurvival is installed)

Reload Config, View My Status, View Plague Deaths, and Reset My Own Stats are one click, no
target needed. Cure a Player uses the player picker. **Clear ALL Tracked State** is server-wide
and destructive (every player's thirst/radiation/disease state, gone) -- a normal click just warns
you; you have to **shift-click** it to actually confirm and run `/nexussurvival removeall`.

## Realms (only shown if NexusRealms is installed)

- **Toggle My Land Bypass** -- runs `/realms bypass`. Watch chat for the new on/off state; this
  menu has no way to read Realms' bypass state back, so the icon can't reflect it.
- **Open Terrain Settings** -- a sub-menu with an explicit **Turn ON** and **Turn OFF** button for
  each terrain type (Build/Containers/Doors/PvP/Elytra), rather than one ambiguous toggle -- for
  the same reason as bypass above: this menu genuinely doesn't know Realms' current setting, so it
  only ever tells you what a button will *set* it to, never claims to show what it currently is.

## Commands (all require `nexusadmin.menu` or the specific ability's own permission)

- `/nexusadmin` / `/admin` / `/na` -- with no arguments, opens the menu (if you have
  `nexusadmin.menu`); otherwise shows usage.
- `/nexusadmin menu` -- opens the menu explicitly.
- `/nexusadmin vanish` / `god` / `fly` -- toggle.
- `/nexusadmin freeze <player> <reason>` / `smite <target>` / `tp <player>` /
  `tphere <player> <reason>` / `kill <target> [reason]` -- unchanged from v0.1.0.
- `/nexusadmin log [count]` -- unchanged from v0.1.0.

## Config (`plugins/NexusAdmin/config.yml`)

Everything from v0.1.0 (`log.*`, `watch-broadcast.*`, `cooldowns.*`, `require-reason.*`) plus:

- `menu.join-message-enabled` -- the clickable join-chat button. Default `true`.
- `menu.priority-players` -- a list of player names (case-insensitive) who always get the admin
  panel -- join-chat button and `/nexusadmin`/`/admin` menu access -- even if `nexusadmin.menu`
  doesn't actually resolve `true` for them. Meant as a safety net for a server owner or trusted
  staffer whose permissions setup doesn't grant that "default: op" node the way they'd expect, not
  as a substitute for setting the permission up properly. Defaults to `[RealSociety5107]`; edit the
  list and restart (or reload the plugin) to change it.

## What this deliberately doesn't do yet

Combat isn't a category yet -- its source wasn't available when this was built, so it's a clean
follow-up whenever you want to add it, same shape as the other three (dispatch its own commands,
gate the category on it being installed). The menu also doesn't paginate the player-picker past
~21 concurrent players, or offer Realms' team-scoped bulk claim/unclaim admin command (it needs a
team name and radius typed in, which didn't make the cut for this pass) -- both still work exactly
as before via their own chat commands, and either is a small addition later if you want them in
the menu too.

## Build

```
mvn clean package
```

Produces `target/NexusAdmin-0.2.1.jar`. Requires Java 21 and network access to `repo.papermc.io` /
Maven Central. See CHANGES.md for how this was verified in the sandbox (a from-scratch stub
library covering the plugin's full GUI/menu surface, `javac -Xlint:all -Werror`: 0 errors, 0
warnings) -- run your own `mvn clean package` against the real Paper API.
