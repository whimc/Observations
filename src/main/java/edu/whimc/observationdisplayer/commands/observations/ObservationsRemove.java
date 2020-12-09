package edu.whimc.observationdisplayer.commands.observations;

import java.util.List;

import org.bukkit.command.CommandSender;

import edu.whimc.observationdisplayer.Observation;
import edu.whimc.observationdisplayer.ObservationDisplayer;
import edu.whimc.observationdisplayer.commands.AbstractSubCommand;
import edu.whimc.observationdisplayer.utils.Utils;

public class ObservationsRemove extends AbstractSubCommand {

    public ObservationsRemove(ObservationDisplayer plugin, String baseCommand, String subCommand) {
        super(plugin, baseCommand, subCommand);
        super.description("Removes an observation");
        super.arguments("id");
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        Observation obs = Utils.getObervationWithError(sender, args[0]);
        if (obs == null) return true;

        obs.deleteAndSetInactive(() -> {
            Utils.msg(sender, "&aObservation \"&2" + obs.getId() + "&a\" removed!");
        });

        return true;
    }

    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        return Observation.getObservationsTabComplete(args[0]);
    }
}
