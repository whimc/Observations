package edu.whimc.observationdisplayer.commands.observations;

import org.bukkit.command.CommandSender;

import edu.whimc.observationdisplayer.ObservationDisplayer;
import edu.whimc.observationdisplayer.commands.AbstractSubCommand;

public class TEST_PARSING extends AbstractSubCommand {

    public TEST_PARSING(ObservationDisplayer plugin, String baseCommand, String subCommand) {
        super(plugin, baseCommand, subCommand);
        super.arguments("args...");
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        String[] arguments = parseArgs(args, "[", "]");
        int minArgs = 0;
        for (String arg : args) {
            if (arg.startsWith("[") && arg.endsWith("]")) break;
            minArgs++;
        }

        sender.sendMessage("min args: " + minArgs);
        for (String arg : arguments) {
            sender.sendMessage(arg);
        }
        return true;
    }

}
