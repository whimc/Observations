package edu.whimc.observationdisplayer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import edu.whimc.observationdisplayer.commands.ObservationsCommand;
import edu.whimc.observationdisplayer.commands.ObserveCommand;
import edu.whimc.observationdisplayer.utils.Queryer;
import edu.whimc.observationdisplayer.utils.Utils;

public class ObservationDisplayer extends JavaPlugin {

    private Queryer queryer;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(false);

        if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            getLogger().severe("*** HolographicDisplays is not installed or not enabled. ***");
            getLogger().severe("*** This plugin will be disabled. ***");
            this.setEnabled(false);
            return;
        }

        getCommand("observe").setExecutor(new ObserveCommand(this));
        getCommand("observations").setExecutor(new ObservationsCommand());

        queryer = new Queryer(this, q -> {
            if (q == null) {
                this.getLogger().severe("Could not create MySQL connection! Disabling plugin...");
                this.getPluginLoader().disablePlugin(this);
            } else {
                Utils.setDebugPrefix(getDescription().getName());
                Utils.debug("Starting to load observations...");
                q.loadObservations(this);
            }
        });
    }

    public Queryer getQueryer() {
        return queryer;
    }

}
