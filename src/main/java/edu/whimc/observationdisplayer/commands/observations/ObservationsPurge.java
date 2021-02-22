package edu.whimc.observationdisplayer.commands.observations;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;

import edu.whimc.observationdisplayer.ObservationDisplayer;
import edu.whimc.observationdisplayer.commands.AbstractSubCommand;
import edu.whimc.observationdisplayer.models.Observation;
import edu.whimc.observationdisplayer.utils.Utils;

public class ObservationsPurge extends AbstractSubCommand {

    public ObservationsPurge(ObservationDisplayer plugin, String baseCommand, String subCommand) {
        super(plugin, baseCommand, subCommand);
        super.description("Remove temporarily reactivated observations");
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        List<Observation> toRemove = Observation.getObservations().stream()
                .filter(Observation::isTemporary)
                .collect(Collectors.toList());
        toRemove.stream().forEachOrdered(Observation::deleteObservation);

        Utils.msg(sender, "&aRemoved &2" + toRemove.size() + " &atemporary observation(s)!");
        return true;
    }

}
