package edu.whimc.observationdisplayer.commands.observations;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import edu.whimc.observationdisplayer.Observation;
import edu.whimc.observationdisplayer.ObservationDisplayer;
import edu.whimc.observationdisplayer.commands.AbstractSubCommand;
import edu.whimc.observationdisplayer.utils.Utils;

public class ObservationsList extends AbstractSubCommand {

    public ObservationsList(ObservationDisplayer plugin, String baseCommand, String subCommand) {
        super(plugin, baseCommand, subCommand);
        super.description("Lists all active observations");
        super.arguments("[-p <player>] [-w <\"world...\">]");
        super.bypassArgumentChecks();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        String player = null;
        String world = null;

        for (int ind = 0; ind < args.length - 1; ind++) {
            String param = args[ind].toLowerCase();

            if (param.equalsIgnoreCase("-p")) {
                player = args[ind + 1];
            }
            if (param.equalsIgnoreCase("-w")) {
                world = args[ind + 1];
            }
        }

        Utils.debug("Player: " + player + " | World: " + world);

        if (args.length > 0 && player == null && world == null) {
            Utils.msg(sender, "&cIncorrect parameter usage!",
                    "  " + super.getUsage(0),
                    "  &7Example:", "    &7/observations &blist &3-p Poi -w &7\"&3Redstone World&7\"");
            return true;
        }

        Utils.listObservations(sender, player, world);
        return true;
    }

    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> res = Arrays.asList("-p", "-w");
        if (args.length == 1) {
            return res;
        }

        String prev = args[args.length - 2];
        String hint = args[args.length - 1].toLowerCase();
        if (prev.equalsIgnoreCase("-p")) {
            return Observation.getPlayersTabComplete(hint);
        }
        if (prev.equalsIgnoreCase("-w")) {
            return Bukkit.getWorlds().stream()
                    .filter(v -> v.getName().toLowerCase().startsWith(hint))
                    .map(World::getName)
                    .sorted()
                    .collect(Collectors.toList());
        }

        return res;
    }

}
