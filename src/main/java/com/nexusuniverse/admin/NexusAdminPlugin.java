package com.nexusuniverse.admin;

import com.nexusuniverse.admin.menu.AdminMenuListener;
import com.nexusuniverse.admin.menu.AdminMenus;
import org.bukkit.plugin.java.JavaPlugin;

public class NexusAdminPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        AdminConfig config = new AdminConfig(this);
        AuditLogger auditLogger = new AuditLogger(this, config);
        AbilityManager abilities = new AbilityManager();
        AdminMenus menus = new AdminMenus(this, abilities);

        getServer().getPluginManager().registerEvents(new AdminListener(abilities, config), this);
        getServer().getPluginManager().registerEvents(new AdminMenuListener(this), this);
        getCommand("nexusadmin").setExecutor(new NexusAdminCommand(config, abilities, auditLogger, menus));

        getLogger().info("NexusAdmin enabled -- every ability use is logged to " + config.logFileName()
                + " and " + (config.watchBroadcastEnabled() ? "broadcast live to nexusadmin.watch." : "not broadcast live (watch-broadcast disabled in config)."));
        getLogger().info("Admin panel: run /admin (or /nexusadmin menu), or click the join-message button, "
                + "for anyone with nexusadmin.menu.");
    }
}
