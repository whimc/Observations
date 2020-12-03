package edu.whimc.observationdisplayer.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edu.whimc.observationdisplayer.Observation;
import edu.whimc.observationdisplayer.ObservationDisplayer;
import edu.whimc.observationdisplayer.utils.Utils;

public class ObserveCommand implements CommandExecutor {
	
	ObservationDisplayer plugin;
	
	public ObserveCommand(ObservationDisplayer plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			Utils.msg(sender, ChatColor.RED + "You must be a player!");
			return true;
		}
		
		if (!sender.hasPermission("observations.observe")) {
			Utils.msg(sender,
					"&cYou do not have the required permission!",
					"  &f&oobservations.observe");
			return true;
		}
		
		if (args.length == 0) {
			Utils.msg(sender, 
					"&cIncorrect usage!",
					"  &f&o/observe [text]");
			return true;
		}
		
		Player player = (Player) sender;
		
		StringBuilder builder = new StringBuilder();
		for (String str : args) {
			builder.append(str).append(" ");
		}
		String text = builder.toString().trim();
		
		Observation.createObservation(plugin, player, player.getLocation(), text);
		
		Utils.msg(sender,
				"&7Your observation has been placed:",
				"  &8\"&f&l" + text + "&8\"");
		return true;
	}
}
