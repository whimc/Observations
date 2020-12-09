package edu.whimc.observationdisplayer.commands.observations;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edu.whimc.observationdisplayer.Observation;
import edu.whimc.observationdisplayer.ObservationDisplayer;
import edu.whimc.observationdisplayer.commands.AbstractSubCommand;
import edu.whimc.observationdisplayer.utils.Utils;

public class ObservationsRemoveAll extends AbstractSubCommand {

    public ObservationsRemoveAll(ObservationDisplayer plugin, String baseCommand, String subCommand) {
        super(plugin, baseCommand, subCommand);
        super.description("Removes observations matching the given query");
        super.arguments("[-p <player>] [-w world...]");
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

        if (player == null && world == null) {
            Utils.msg(sender, "&cIncorrect parameter usage!",
                    "  " + super.getUsage(),
                    "  &7Examples:",
                    "    /observations removeall -p Poi -w \"Redstone World\"",
                    "    /observations removeall -p Poi",
                    "    /observations removeall -w NoMoon");
            return true;
        }

        String finalPlayer = player;
        String finalWorld = world;

        List<Observation> toRemove = Observation.getObservations().stream()
                .filter(v -> finalWorld == null || v.getHoloLocation().getWorld().getName().equalsIgnoreCase(finalWorld))
                .filter(v -> finalPlayer == null || v.getPlayer().equalsIgnoreCase(finalPlayer))
                .collect(Collectors.toList());
        toRemove.stream()
                .forEachOrdered(Observation::deleteObservation);

        if (toRemove.size() > 0) {
            plugin.getQueryer().makeObservationsInactive(world, player, count -> {
                Utils.msg(sender, "&7Finished removing " + count + " observation(s)");
            });
        } else {
            Utils.msg(sender, "&7No observations matched your input query!");
        }
        return true;
    }

    @Override
    protected void missingArguments(CommandSender sender, String missingArgs) {
        Utils.msg(sender, "&cMissing parameters: " + missingArgs,
                "  " + super.getUsage(),
                "  &7Examples:",
                "    /observations removeall -p Poi -w \"Redstone World\"",
                "    /observations removeall -p Poi",
                "    /observations removeall -w NoMoon");
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
            return Bukkit.getOnlinePlayers().stream()
                    .filter(v -> v.getName().toLowerCase().startsWith(hint))
                    .map(Player::getName)
                    .sorted()
                    .collect(Collectors.toList());
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
