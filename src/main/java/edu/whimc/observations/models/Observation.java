package edu.whimc.observations.models;

import edu.whimc.observations.Observations;
import edu.whimc.observations.observetemplate.models.ObservationTemplate;
import edu.whimc.observations.utils.ObservationContentValidator;
import edu.whimc.observations.utils.Utils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Observation {

    private static final List<Observation> observations = new ArrayList<>();
    private static final Map<Integer, Observation> observationsById = new HashMap<>();

    private final Observations plugin;
    private final Timestamp timestamp;
    private final String playerName;
    private final Location holoLoc;
    private final Location viewLoc;
    private final String observation;
    private final boolean isTemporary;
    private final ObservationTemplate template;
    private int id;
    private Hologram hologram;
    private Timestamp expiration;
    private Material hologramItem;
    protected Observation(Observations plugin, int id, Timestamp timestamp, String playerName,
                          Location viewLoc, String observation, Timestamp expiration, ObservationTemplate template,
                          boolean isTemporary, boolean isNew) {
        this.plugin = plugin;
        this.timestamp = timestamp;
        this.playerName = playerName;
        this.holoLoc = viewLoc.clone().add(0, 3, 0).add(viewLoc.getDirection().multiply(2));
        this.viewLoc = viewLoc;
        this.observation = observation;
        this.expiration = expiration;
        this.template = template;
        this.isTemporary = isTemporary;

        if (this.template == null) {
            this.hologramItem = Utils.matchMaterial(
                    plugin, plugin.getConfig().getString("template-gui.uncategorized.item"), Material.STONE);
        } else {
            this.hologramItem = this.template.getGuiItem();
        }

        if (!isNew) {
            this.id = id;
            createHologram();
            return;
        }

        plugin.getQueryer().storeNewObservation(this, newId -> {
            this.id = newId;
            observationsById.put(this.id, this);
            createHologram();
        });
    }

    /**
     * Creates a new observation and calls a new observe event with current time and configured expiration.
     * @return The new observation, or null if the text was rejected
     */
    public static Observation createPlayerObservation(Observations plugin, Player player, String observation, ObservationTemplate template) {
        ObservationContentValidator.Result validation = ObservationContentValidator.validate(observation);
        if (!validation.isAccepted()) {
            ObservationContentValidator.sendRejection(player, validation);
            if (validation.getFailure() == ObservationContentValidator.Failure.PROFANITY) {
                ObservationContentValidator.notifyOps(plugin, player, observation);
            }
            return null;
        }

        int days = plugin.getConfig().getInt("expiration-days");
        Timestamp expiration = Timestamp.from(Instant.now().plus(days, ChronoUnit.DAYS));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        Observation obs = new Observation(plugin, -1, timestamp, player.getName(), player.getLocation(), observation, expiration, template, false, true);
        Utils.msg(player,
                "&7Your observation has been placed:",
                "  &8\"&f&l" + observation + "&8\"");

        // Call custom event
        ObserveEvent observeEvent = new ObserveEvent(obs, player);
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getServer().getPluginManager().callEvent(observeEvent));
        observations.add(obs);
        if (obs.id > 0) {
            observationsById.put(obs.id, obs);
        }
        return obs;
    }

    public static Observation loadObservation(Observations plugin, int id, Timestamp timestamp,
                                              String playerName, Location viewLoc, String observation,
                                              Timestamp expiration, ObservationTemplate template, boolean isTemporary) {
        Observation obs = new Observation(plugin, id, timestamp, playerName, viewLoc, observation, expiration, template, isTemporary, false);
        observations.add(obs);
        observationsById.put(id, obs);
        return obs;
    }

    public static void startExpiredObservationScanningTask(Observations plugin) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            Utils.debug("Scanning for expired observations...");
            List<Observation> toRemove = observations.stream()
                    .filter(Observation::hasExpired)
                    .filter(v -> !v.isTemporary())
                    .collect(Collectors.toList());

            int count = toRemove.size();
            toRemove.forEach(observation -> observation.deleteObservation());

            if (count > 0) {
                plugin.getQueryer().makeExpiredObservationsInactive(dbCount -> {
                    Utils.debug("Removed " + count + " expired observation(s). (" + dbCount + " from the database)");
                });
            }
        }, 20 * 60, 20 * 60);
    }

    public static List<Observation> getObservations() {
        return observations;
    }

    public static Observation getObservation(int id) {
        return observationsById.get(id);
    }

    public static List<String> getObservationsTabComplete(String hint) {
        return observations.stream()
                .filter(v -> Integer.toString(v.getId()).startsWith(hint))
                .sorted(Comparator.comparing(Observation::getId))
                .map(v -> Integer.toString(v.getId()))
                .collect(Collectors.toList());
    }

    public static List<String> getPlayersTabComplete(String hint) {
        Set<String> players = observations.stream()
                .map(Observation::getPlayer)
                .distinct()
                .filter(v -> v.toLowerCase().startsWith(hint.toLowerCase()))
                .collect(Collectors.toSet());
        players.addAll(Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toSet()));
        return new ArrayList<>(players);
    }

    private void createHologram() {
        Hologram holo = DHAPI.createHologram(Integer.toString(this.id), this.holoLoc);

        DHAPI.addHologramLine(holo, this.hologramItem);
        DHAPI.addHologramLine(holo, Utils.color(this.observation));
        DHAPI.addHologramLine(holo, ChatColor.GRAY + this.playerName + " - " + Utils.getDate(this.timestamp));

        if (this.expiration != null) {
            DHAPI.addHologramLine(holo, ChatColor.GRAY + "Expires " + Utils.getDate(this.expiration));
        }

        if (this.isTemporary) {
            DHAPI.addHologramLine(holo, ChatColor.DARK_GRAY + "*temporary*");
        }

        this.hologram = holo;

        if (this.plugin.getConfig().getBoolean("enable-click-to-view")) {
            HologramClickHandler.register(this.hologram, this.viewLoc);
        }
    }

    public void reRender() {
        deleteHologramOnly();
        createHologram();
    }

    public String getPlayer() {
        return this.playerName;
    }

    public Location getHoloLocation() {
        return this.holoLoc;
    }

    public Location getViewLocation() {
        return this.viewLoc;
    }

    public String getObservation() {
        return this.observation;
    }

    public int getId() {
        return this.id;
    }

    public Hologram getHologram() {
        return this.hologram;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public Timestamp getExpiration() {
        return this.expiration;
    }

    public void setExpiration(Timestamp timestamp) {
        this.expiration = timestamp;
    }

    public boolean hasExpired() {
        return this.expiration != null && Instant.now().isAfter(this.expiration.toInstant());
    }

    public ObservationTemplate getTemplate() {
        return this.template;
    }

    public boolean isTemporary() {
        return this.isTemporary;
    }

    @Override
    public String toString() {
        String text = Utils.color("&f&l" + this.observation);
        if (ChatColor.stripColor(text).length() > 20) {
            text = Utils.coloredSubstring(text, 20) + "&7 . . .";
        }

        return "&9&l" + this.id + ".&r &8\"" + text + "&8\" &9> &7&o" + this.playerName + " " +
                "&7(" + this.holoLoc.getWorld().getName() + ", " + this.holoLoc.getBlockX() + ", " +
                this.holoLoc.getBlockY() + ", " + this.holoLoc.getBlockZ() + "&7)";
    }

    public void deleteAndSetInactive(Runnable callback) {
        this.plugin.getQueryer().makeSingleObservationInactive(this.id, callback);
        deleteObservation();
    }

    public void deleteObservation() {
        deleteHologramOnly();
        observations.remove(this);
        if (this.id > 0) {
            observationsById.remove(this.id);
        }
    }

    public void deleteHologramOnly() {
        if (this.hologram != null) {
            HologramClickHandler.unregister(this.hologram);
            this.hologram.delete();
            this.hologram = null;
        }
    }

}
