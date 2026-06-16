package edu.whimc.observations.models;

import edu.whimc.observations.Observations;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.event.HologramClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class HologramClickHandler implements Listener {

    private static final Map<Hologram, Location> viewLocations = new ConcurrentHashMap<>();
    private static HologramClickHandler instance;

    private final Observations plugin;

    private HologramClickHandler(Observations plugin) {
        this.plugin = plugin;
    }

    public static void enable(Observations plugin) {
        if (instance != null) {
            return;
        }
        instance = new HologramClickHandler(plugin);
        Bukkit.getPluginManager().registerEvents(instance, plugin);
    }

    public static void register(Hologram hologram, Location viewLocation) {
        viewLocations.put(hologram, viewLocation);
    }

    public static void unregister(Hologram hologram) {
        viewLocations.remove(hologram);
    }

    @EventHandler
    public void onClick(HologramClickEvent event) {
        Location viewLocation = viewLocations.get(event.getHologram());
        if (viewLocation == null) {
            return;
        }

        Bukkit.getScheduler().runTask(this.plugin, () -> event.getPlayer().teleport(viewLocation));
    }
}
