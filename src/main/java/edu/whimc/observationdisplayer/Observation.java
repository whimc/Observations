package edu.whimc.observationdisplayer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.handler.TouchHandler;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;

import edu.whimc.observationdisplayer.utils.Utils;

public class Observation {

    private static List<Observation> observations = new ArrayList<>();

    private ObservationDisplayer plugin;
    private int id;
    private Timestamp timestamp;
    private String playerName;
    private Location holoLoc;
    private Location viewLoc;
    private String observation;
    private Hologram hologram;

    private Observation() {}

    public static void createObservation(ObservationDisplayer plugin, Player player, Location viewLoc, String observation) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Observation obs = new Observation(plugin, -1, timestamp, player.getName(), viewLoc, observation, true);
        observations.add(obs);
    }

    public static void loadObservation(ObservationDisplayer plugin, int id, Timestamp timestamp, String playerName, Location viewLoc, String observation) {
        Observation obs = new Observation(plugin, id, timestamp, playerName, viewLoc, observation, false);
        observations.add(obs);
    }

    private Observation(ObservationDisplayer plugin, int id, Timestamp timestamp, String playerName, Location viewLoc, String observation, boolean isNew) {
        this.plugin = plugin;
        this.timestamp = timestamp;
        this.playerName = playerName;
        this.holoLoc = viewLoc.clone().add(0, 3, 0).add(viewLoc.getDirection().multiply(2));
        this.viewLoc = viewLoc;
        this.observation = observation;

        createHologram(plugin);

        if (isNew) {
            this.id = plugin.getQueryer().storeObservation(timestamp, playerName, viewLoc, observation);
        } else {
            this.id = id;
        }
    }

    private void createHologram(ObservationDisplayer plugin) {
        Hologram holo = HologramsAPI.createHologram(plugin, holoLoc);
        ItemLine signLine = holo.appendItemLine(new ItemStack(Material.OAK_SIGN));
        TextLine observationLine = holo.appendTextLine(ChatColor.translateAlternateColorCodes('&', observation));
        TextLine infoLine = holo.appendTextLine(ChatColor.GRAY + playerName + " - " + Utils.getDate(timestamp));

        ObservationClick clickListener = new ObservationClick(viewLoc);

        signLine.setTouchHandler(clickListener);
        observationLine.setTouchHandler(clickListener);
        infoLine.setTouchHandler(clickListener);

        this.hologram = holo;

    }

    private class ObservationClick implements TouchHandler {

        private Location loc;

        public ObservationClick(Location loc) {
            this.loc = loc;
        }

        @Override
        public void onTouch(Player player) {
            player.teleport(loc);
        }
    }

    public static List<Observation> getObservations() {
        return observations;
    }

    public static Iterator<Observation> getObservationsIterator() {
        return observations.iterator();
    }

    public static Observation getObservation(int id) {
        for (Observation obs : observations) {
            if (obs.getId() == id) return obs;
        }

        return null;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public String getPlayer() {
        return playerName;
    }

    public Location getHoloLocation() {
        return holoLoc;
    }

    public Location getViewLocation() {
        return viewLoc;
    }

    public String getObservation() {
        return observation;
    }

    public int getId() {
        return id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        String text = "&f&l" + this.observation;
        if (ChatColor.stripColor(text).length() > 20) {
            text = Utils.coloredSubstring(text, 20) + "&7 . . .";
        }

        return "&9&l" + this.id + ".&r &8\"" + text + "&8\" &9> &7&o" + this.playerName + " " +
        "&7(" + this.holoLoc.getWorld().getName() + ", " + this.holoLoc.getBlockX() + ", " +
        this.holoLoc.getBlockY() + ", "+ this.holoLoc.getBlockZ() + "&7)";
    }

    public void remove() {
        manualRemove();
        observations.remove(this);
    }

    public void manualRemove() {
        plugin.getQueryer().makeObservationInactive(this.id);
        this.hologram.delete();
    }

}
