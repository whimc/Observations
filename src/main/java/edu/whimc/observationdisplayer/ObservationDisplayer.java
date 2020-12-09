package edu.whimc.observationdisplayer;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import edu.whimc.observationdisplayer.commands.ObserveCommand;
import edu.whimc.observationdisplayer.commands.observations.ObservationsCommand;
import edu.whimc.observationdisplayer.utils.Queryer;
import edu.whimc.observationdisplayer.utils.Utils;

public class ObservationDisplayer extends JavaPlugin {

    public static final String PERM_PREFIX = "whimc-observations";

    private Queryer queryer;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);

        if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            getLogger().severe("*** HolographicDisplays is not installed or not enabled. ***");
            getLogger().severe("*** This plugin will be disabled. ***");
            this.setEnabled(false);
            return;
        }


        queryer = new Queryer(this, q -> {
            if (q == null) {
                this.getLogger().severe("Could not create MySQL connection! Disabling plugin...");
                this.getPluginLoader().disablePlugin(this);
            } else {
                Utils.setDebugPrefix(getDescription().getName());
                Utils.debug("Starting to load observations...");
                q.loadObservations(() -> {
                    Utils.debug("Finished loading observations!");
                });
                Observation.scanForExpiredObservations(this);

                Permission parent = new Permission(PERM_PREFIX + ".*");
                Bukkit.getPluginManager().addPermission(parent);

                Permission entry = new Permission(PERM_PREFIX + ".entry.*");
                entry.addParent(parent, true);
                Bukkit.getPluginManager().addPermission(entry);

                getCommand("observe").setExecutor(new ObserveCommand(this));

                ObservationsCommand oc = new ObservationsCommand(this);
                getCommand("observations").setExecutor(oc);
                getCommand("observations").setTabCompleter(oc);

            }
        });
    }

    public Queryer getQueryer() {
        return queryer;
    }

}
