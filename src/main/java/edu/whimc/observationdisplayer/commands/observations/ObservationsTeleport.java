package edu.whimc.observationdisplayer.commands.observations;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edu.whimc.observationdisplayer.Observation;
import edu.whimc.observationdisplayer.ObservationDisplayer;
import edu.whimc.observationdisplayer.commands.AbstractSubCommand;
import edu.whimc.observationdisplayer.utils.Utils;

public class ObservationsTeleport extends AbstractSubCommand {

    public ObservationsTeleport(ObservationDisplayer plugin, String baseCommand, String subCommand) {
        super(plugin, baseCommand, subCommand);
        super.description("Teleports you to an observation");
        super.arguments("id");
        super.requiresPlayer();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Observation obs = Utils.getObervationWithError(sender, args[0]);
        if (obs == null) return true;

        ((Player) sender).teleport(obs.getViewLocation());
        Utils.msg(sender, "&aYou have been teleported to observation \"&2" + obs.getId() + "&a\"!");
        return true;
    }

    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        return Observation.getObservationsTabComplete(args[0]);
    }

}
