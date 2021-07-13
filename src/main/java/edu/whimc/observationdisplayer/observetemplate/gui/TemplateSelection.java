package edu.whimc.observationdisplayer.observetemplate.gui;

import edu.whimc.observationdisplayer.ObservationDisplayer;
import edu.whimc.observationdisplayer.libraries.CenteredText;
import edu.whimc.observationdisplayer.libraries.SpigotCallback;
import edu.whimc.observationdisplayer.models.Observation;
import edu.whimc.observationdisplayer.observetemplate.models.ObservationPrompt;
import edu.whimc.observationdisplayer.observetemplate.models.ObservationTemplate;
import edu.whimc.observationdisplayer.utils.Utils;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class TemplateSelection implements Listener {

    private static final String CHECK = "\u2714";

    private static final String CROSS = "\u274C";

    private static final String BULLET = "\u2022";

    /**
     * Instance of main class.
     */
    private final ObservationDisplayer plugin;

    /**
     * Used to create clickable messags with callbacks.
     */
    private final SpigotCallback spigotCallback;

    /**
     * The selected template to fill out.
     */
    private final ObservationTemplate template;

    /**
     * The UUID of the player making the selection.
     */
    private final UUID uuid;

    /**
     * The selected prompt from the template.
     */
    private ObservationPrompt prompt = null;

    /**
     * The current responses that have been chosen
     */
    private final List<String> responses = new ArrayList<>();

    /**
     * The stage of the selection.
     */
    private TemplateSelectionStage stage = TemplateSelectionStage.SELECT_PROMPT;

    /**
     * The response index that is being selected.
     */
    private int responseIndex = 0;

    /**
     * Selections that are currently happening.
     */
    private static final Map<UUID, TemplateSelection> ongoingSelections = new HashMap<>();

    public TemplateSelection(ObservationDisplayer plugin, SpigotCallback spigotCallback, Player player, ObservationTemplate template) {
        UUID uuid = player.getUniqueId();
        if (ongoingSelections.containsKey(uuid)) {
            ongoingSelections.get(uuid).destroySelection();
            Utils.msg(player, "Your previous observation was canceled because you started a new one!");
        }

        this.plugin = plugin;
        this.spigotCallback = spigotCallback;
        this.template = template;
        this.uuid = uuid;

        // Register this class as a listener to cancel clickables if they change worlds
        Bukkit.getPluginManager().registerEvents(this, plugin);

//        if (template.getPrompts().size() == 1) {
//            this.prompt = template.getPrompts().get(0);
//            this.stage = TemplateSelectionStage.SELECT_RESPONSE;
//        }

        ongoingSelections.put(uuid, this);
        doStage();
    }

    private void doStage() {
        // Clear all other callbacks before entering the next stage
        this.spigotCallback.clearCallbacks(getPlayer());

        switch (this.stage) {
            case SELECT_PROMPT:
                doSelectPrompt();
                return;
            case SELECT_RESPONSE:
                doSelectResponse();
                return;
            case CONFIRM:
                doConfirm();
                return;
        }
    }

    private void doSelectPrompt() {
        Player player = getPlayer();

        sendHeader();
        Utils.msgNoPrefix(player, "&lClick the template you would like to fill out:", "");

        for (ObservationPrompt curPrompt : this.template.getPrompts()) {
            sendComponent(
                    player,
                    "&8" + BULLET + " &r" + curPrompt.getPrompt(),
                    "&aClick here to select \"&r" + curPrompt.getPrompt() + "&a\"",
                    p -> {
                        this.prompt = curPrompt;
                        this.stage = TemplateSelectionStage.SELECT_RESPONSE;
                        doStage();
                    });
        }

        sendFooter(false, p -> {
            destroySelection();
            this.plugin.getTemplateManager().getGui().openTemplateInventory(player);
        });
    }

    private void doSelectResponse() {
        Player player = getPlayer();
        List<String> responses = this.prompt.getResponses(player.getWorld(), this.responseIndex);
        String highlight = this.template.getColor() + "&l";
        String filledIn = replaceFirst(getFilledInPrompt(), ObservationPrompt.FILLIN, highlight + "[&n   " + highlight + "]&r");

        sendHeader();
        Utils.msgNoPrefix(player, filledIn, "");

        for (String response : responses) {
            sendComponent(
                    player,
                    "&8" + BULLET + " &r" + response,
                    "&aClick here to select \"&r" + response + "&a\"",
                    p -> {
                        this.responses.add(response);
                        this.responseIndex += 1;
                        if (this.responseIndex == this.prompt.getNumberOfFillIns()) {
                            this.stage = TemplateSelectionStage.CONFIRM;
                        }
                        doStage();
                    });
        }

        sendFooter(false, p -> {
            if (this.responseIndex == 0) {
                this.stage = TemplateSelectionStage.SELECT_PROMPT;
            } else {
                this.responses.remove(this.responses.size() - 1);
                this.responseIndex -= 1;
            }
            doStage();
        });
    }

    private void doConfirm() {
        String filledIn = getFilledInPrompt();
        Player player = getPlayer();

        sendHeader();

        Utils.msgNoPrefix(player,
                "&f&lSubmit the following observation?",
                "",
                filledIn);

        sendFooter(true, p -> {
            this.responses.remove(this.responses.size() - 1);
            this.responseIndex -= 1;
            this.stage = TemplateSelectionStage.SELECT_RESPONSE;
            doStage();
        });
    }

    private String getFilledInPrompt() {
        String result = this.prompt.getPrompt();
        String highlight = this.template.getColor();
        for (String response : this.responses) {
            result = replaceFirst(result, ObservationPrompt.FILLIN, highlight + response + "&r");
        }
        return result;
    }

    public String replaceFirst(String str, String pattern, String replacement) {
        return str.replaceFirst(Pattern.quote(pattern), replacement);
    }

    private void addCallback(TextComponent component, UUID playerUUID, Consumer<Player> onClick) {
        this.spigotCallback.createCommand(playerUUID, component, onClick);
    }

    private TextComponent createComponent(String text, String hoverText, Consumer<Player> onClick) {
        TextComponent message = new TextComponent(Utils.color(text));
        message.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(Utils.color(hoverText)).create()));
        addCallback(message, this.uuid, onClick);
        return message;
    }

    private void sendComponent(Player player, String text, String hoverText, Consumer<Player> onClick) {
        player.spigot().sendMessage(createComponent(text, hoverText, onClick));
    }

    private void sendHeader() {
        Player player = getPlayer();
        String header = "&r " + this.template.getGuiItemName() + " ";
        CenteredText.sendCenteredMessage(player, header, "&7&m &r");
    }

    private void sendFooter(boolean withConfirm, Consumer<Player> goBackCallback) {
        Player player = getPlayer();
        ComponentBuilder builder = new ComponentBuilder("");

        if (withConfirm) {
            Consumer<Player> confirmCallback = p -> {
                String text = Utils.color(getFilledInPrompt());
                int days = this.plugin.getConfig().getInt("expiration-days");
                Timestamp expiration = Timestamp.from(Instant.now().plus(days, ChronoUnit.DAYS));

                Observation obs = Observation.createObservation(this.plugin, player, player.getLocation(), text, expiration);
                obs.setHologramItem(this.template.getGuiItem());

                Utils.msg(player,
                        "&7Your observation has been placed:",
                        "  &8\"&f&l" + text + "&8\"");
                destroySelection();
            };

            builder.append(createComponent(
                    "&a&l" + CHECK + " Confirm",
                    "&aClick to submit your observation!",
                    confirmCallback))
                    .append("  ");
        }

        Consumer<Player> cancelCallback = p -> {
            Utils.msg(p, "Observation canceled!");
            destroySelection();
        };

        builder.append(createComponent(
                "&c&l" + CROSS + " Cancel",
                "&cClick to cancel your observation",
                cancelCallback));

        if (goBackCallback != null) {
            builder.append("  ").append(createComponent(
                    "&e&l< Go Back",
                    "&eClick to go back to the previous panel",
                    goBackCallback));
        }

        Utils.msgNoPrefix(player, "");
        player.spigot().sendMessage(builder.create());
        CenteredText.sendCenteredMessage(player, " &7Click to make a selection ", "&7&m &r");
    }

    private Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    public void destroySelection() {
        // Clear callbacks
        this.spigotCallback.clearCallbacks(getPlayer());

        // Remove this as an ongoing selection
        ongoingSelections.remove(this.uuid);

        // Unregister events
        PlayerChangedWorldEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (!this.uuid.equals(player.getUniqueId())) {
            return;
        }

        Utils.msg(player, "Your observation has been canceled because you changed worlds!");
        destroySelection();
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if (this.uuid.equals(event.getPlayer().getUniqueId())) {
            destroySelection();
        }
    }

}
