package edu.whimc.observationdisplayer.commands;

import edu.whimc.observationdisplayer.ObservationDisplayer;
import edu.whimc.observationdisplayer.models.Observation;
import edu.whimc.observationdisplayer.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ObserveCommand implements CommandExecutor {

    private static final String TEMPLATED_OBSERVATION = ObservationDisplayer.PERM_PREFIX + ".observe";
    private static final String FREE_HAND_OBSERVATION = ObservationDisplayer.PERM_PREFIX + ".observe.freehand";

    private final ObservationDisplayer plugin;

    public ObserveCommand(ObservationDisplayer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Utils.msg(sender, ChatColor.RED + "You must be a player!");
            return true;
        }

        if (!sender.hasPermission(TEMPLATED_OBSERVATION)) {
            Utils.msg(sender,
                    "&cYou do not have the required permission!",
                    "  &f&o" + TEMPLATED_OBSERVATION);
            return true;
        }

        Player player = (Player) sender;

        if (!sender.hasPermission(FREE_HAND_OBSERVATION) || args.length == 0) {
            this.plugin.getTemplateManager().getGui().openTemplateInventory(player);
            return true;
        }

        String text = StringUtils.join(args, " ");
        int days = this.plugin.getConfig().getInt("expiration-days");
        Timestamp expiration = Timestamp.from(Instant.now().plus(days, ChronoUnit.DAYS));

        Observation.createObservation(this.plugin, player, player.getLocation(), text, expiration);
        Utils.msg(sender,
                "&7Your observation has been placed:",
                "  &8\"&f&l" + text + "&8\"");
        return true;
    }
}
