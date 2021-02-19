package edu.whimc.observationdisplayer.observetemplate.gui;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import edu.whimc.observationdisplayer.Observation;
import edu.whimc.observationdisplayer.ObservationDisplayer;
import edu.whimc.observationdisplayer.observetemplate.models.ObservationPrompt;
import edu.whimc.observationdisplayer.observetemplate.models.ObservationTemplate;
import edu.whimc.observationdisplayer.observetemplate.models.SpigotCallback;
import edu.whimc.observationdisplayer.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class TemplateSelection implements Listener {

    /** Instance of main class. */
    private ObservationDisplayer plugin;

    /** Used to create clickable messags with callbacks. */
    private SpigotCallback spigotCallback;

    /** The selected template to fill out. */
    private ObservationTemplate template;

    /** The UUID of the player making the selection. */
    private UUID uuid;

    /** The selected prompt from the template. */
    private ObservationPrompt prompt = null;

    /** The current responses that have been chosen */
    private List<String> responses = new ArrayList<>();

    /** The stage of the selection. */
    private TemplateSelectionStage stage = TemplateSelectionStage.SELECT_PROMPT;

    /** The response index that is being selected. */
    private int responseIndex = 0;

    public TemplateSelection(ObservationDisplayer plugin, SpigotCallback spigotCallback, Player player, ObservationTemplate template) {
        this.plugin = plugin;
        this.spigotCallback = spigotCallback;
        this.template = template;
        this.uuid = player.getUniqueId();

        // Register this class as a listener to cancel clickables if they change worlds
        Bukkit.getPluginManager().registerEvents(this, plugin);

        if (template.getPrompts().size() == 1) {
            this.prompt = template.getPrompts().get(0);
            this.stage = TemplateSelectionStage.SELECT_RESPONSE;
        }

        doStage();
    }

    private void doStage() {
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
        Player player = Bukkit.getPlayer(this.uuid);
        Utils.msgNoPrefix(player,
                "&7&m-----------------------------------------------------",
                "&lClick the template you would like to fill out:",
                "");
        for (ObservationPrompt curPrompt : this.template.getPrompts()) {
            sendComponent(
                    player,
                    "&8• &r" + curPrompt.getPrompt(),
                    "&aClick here to select \"&r" + curPrompt.getPrompt() + "&a\"",
                    p -> {
                        this.prompt = curPrompt;
                        this.stage = TemplateSelectionStage.SELECT_RESPONSE;
                        this.spigotCallback.clearCallbacks(p);
                        doStage();
                    });
        }
        Utils.msgNoPrefix(player,
                "",
                getCancelComponent(),
                "&7&m-----------------------------------------------------");
    }

    private void doSelectResponse() {
        Player player = Bukkit.getPlayer(this.uuid);
        List<String> responses = this.prompt.getResponses(player.getWorld(), this.responseIndex);
        String filledIn = replaceFirst(getFilledInPrompt(), ObservationPrompt.FILLIN, "&6&l[   ]&r");

        Utils.msgNoPrefix(player,
                "&7&m-----------------------------------------------------",
                filledIn,
                "");

        for (String response : responses) {
            sendComponent(
                    player,
                    "&8• &r" + response,
                    "&aClick here to select \"&r" + response + "&a\"",
                    p -> {
                        this.responses.add(response);
                        this.responseIndex += 1;
                        if (this.responseIndex == this.prompt.getNumberOfFillIns()) {
                            this.stage = TemplateSelectionStage.CONFIRM;
                        }
                        this.spigotCallback.clearCallbacks(p);
                        doStage();
                    });
        }
        Utils.msgNoPrefix(player,
                "",
                getCancelComponent(),
                "&7&m-----------------------------------------------------");
    }

    private void doConfirm() {
        String filledIn = getFilledInPrompt();
        Player player = Bukkit.getPlayer(this.uuid);

        Consumer<Player> confirmCallback = p -> {
            String text = ChatColor.stripColor(filledIn);
            int days = this.plugin.getConfig().getInt("expiration-days");
            Timestamp expiration = Timestamp.from(Instant.now().plus(days, ChronoUnit.DAYS));

            Observation.createObservation(this.plugin, player, player.getLocation(), text, expiration);
            Utils.msg(player,
                    "&7Your observation has been placed:",
                    "  &8\"&f&l" + text + "&8\"");
            this.spigotCallback.clearCallbacks(p);
        };

        BaseComponent[] confirm = new ComponentBuilder("")
                .append(createComponent(
                        "&a&l✔ Confirm",
                        "&aClick to submit your observation!",
                        confirmCallback))
                .append("  ")
                .append(getCancelComponent())
                .create();

        Utils.msgNoPrefix(player,
                "&7&m-----------------------------------------------------",
                "&f&lSubmit the following observation?",
                "",
                filledIn,
                "");

        player.spigot().sendMessage(confirm);
        Utils.msgNoPrefix(player, "&7&m-----------------------------------------------------");
    }

    private String getFilledInPrompt() {
        String result = this.prompt.getPrompt();
        for (String response : this.responses) {
            result = replaceFirst(result, ObservationPrompt.FILLIN, "&e" + response + "&r");
        }
        return result;
    }

    private TextComponent getCancelComponent() {
        Consumer<Player> cancelCallback = p -> {
            Utils.msg(p, "Observation canceled!");
            this.spigotCallback.clearCallbacks(p);
        };

        return createComponent(
                "&c&l❌ Cancel",
                "&cClick to cancel your observation",
                cancelCallback);
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

    private void cancelSelection() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (!this.uuid.equals(player.getUniqueId())) {
            return;
        }

        Utils.msg(player, "Your observation has been canceled because you changed worlds!");
        this.spigotCallback.clearCallbacks(event.getPlayer());
//        BUST DOWN THOTIANA
//        BLUE FACE BABY
//        YEAH AIGHT
//        BUST DOWN THOTIANA
//        YEAH AIGHT
//        I WANNA SEE YOU BUST DOWN
//        BUST DOWN THOTIANA BUST DOWN THOTIANA
//        I WANNA SEE YOU BUST DOWN
//        PICK IT UP NOW BREAK THAT SHIT DOWN
//        SPEED IT UP THEN SLOW THAT SHIT DOWN ON THE GANG
//        BUST IT
//        BUST DOWN ON THE GANG
//        BUST DOWN THOTIANA
//        I WANNA SEE YOU BUST DOWN
//        PICK IT UP NOW BREAK THAT SHIT DOWN
//        SPEED IT UP NOW SLOW THAT SHIT DOWN ON THE GANG
//        BUIST IT BUST DOWN BUST IT BUST IT
//        BUST DOWN ON THE GANG
//        BLUEFACE BABY
//        YEAH AIGHT IM EVERY WOMANS FANTASY
//        MAMA ALWAYS TOLD ME I WAS GON BREAK HEARTS]
//                I GUESS ITS HER FAULT STUPID
//                DONT BE MAD AT ME
//                I WANNA SEE YOU BUST DOWN
//                BEND THAT SHIT OVER ON THE GANG
//                MAKE THAT SHIT CLAP
//                SHE THREW IT BACK SO I HAD TO DOUBLE BACK ION THE GANG
//                SMACKIN HIGH OFF THEM DRUGS
//                I TRIED TO TELL MYSELF TWO TIMES WAS ENOUGH
//                THEN A  RELAPSED ON THE DEAD LOCS
//                AINT NO RUNNIN THOTIANA
//                YOU GON TAKE THESE DAMN STROKES
//                I BEAT THE PUSSY UP NOW ITS A MURDER SCENE
//                KEEP SHIT PLAYER THOTIANA
//                LIKE YOU AINT EVER NEVER EVEN HEARD OF ME
//                BUST DOWN THOTIANA I WANNA SEE YOU BUST DOWN
//                BEND THAT SHIT OVER
//                YEAH AIGHT NOW MAKE THAT SHIT CLAP ON THE GANG
//                NOW TOOT THAT THING UP
//                THROW THAT SHIT BACK
//                I NEED MY EXTRAS ON THE DEAD LOCS
//                BUST DOWN THOTIANA
//                I WANNA SEE YTOU BUST DOWN
//                PICK IT UP NOW BREAK THAT SHIT DOWN
//                SPEED IT UP THEN SLOW THAT SHIT DOWN ON THE GANG
//                BUST IT BUST DOWN BUST IT BUST IT
//                BUST DOWN ON THE GANG
//                BUST DOWN THOTIANA
//                I WANNA SEE YOU BUST DOWN
    }

}
