package edu.whimc.observations.commands;

import edu.whimc.observations.Observations;
import edu.whimc.observations.models.Observation;
import edu.whimc.observations.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class ObserveCommand implements CommandExecutor, TabCompleter {

    public static final String TEMPLATED_PERM = Observations.PERM_PREFIX + ".observe";
    public static final String FREE_HAND_PERM = Observations.PERM_PREFIX + ".observe.freehand";
    public static final String CUSTOM_RESPONSE_PERM = Observations.PERM_PREFIX + ".observe.customresponse";

    private final Observations plugin;

    public ObserveCommand(Observations plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Utils.msg(sender, ChatColor.RED + "You must be a player!");
            return true;
        }

        if (!sender.hasPermission(TEMPLATED_PERM)) {
            Utils.msg(sender,
                    "&cYou do not have the required permission!",
                    "  &f&o" + TEMPLATED_PERM);
            return true;
        }

        Player player = (Player) sender;

        if (!sender.hasPermission(FREE_HAND_PERM) || args.length == 0) {
            this.plugin.getTemplateManager().getGui().openTemplateInventory(player);
            return true;
        }

        String text = StringUtils.join(args, " ");
        Observation.createPlayerObservation(this.plugin, player, text, null);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Don't show any tab completions
        return Arrays.asList();
    }
}
