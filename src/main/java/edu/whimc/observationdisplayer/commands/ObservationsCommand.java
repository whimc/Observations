package edu.whimc.observationdisplayer.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edu.whimc.observationdisplayer.Observation;
import edu.whimc.observationdisplayer.utils.Utils;

public class ObservationsCommand implements CommandExecutor{
	
	private static final String[] SUB_COMMANDS = { "list", "near", "info", "teleport", "remove", "removeall" };
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("observations.admin")) {
			Utils.msg(sender,
					"&cYou do not have the required permission!",
					"  &f&oobservations.admin");
			return true;
		}
		
		if (args.length == 0 || !isSubCommand(args[0])) {
			sendSubCommands(sender);
			return true;
		}
		
		String subCmd = args[0];
		
		// ********
		//  /observations list <-p [player]> <-w ["world"]>
		// ********
		if (subCmd.equalsIgnoreCase("list")) {
			if (args.length == 1) {
				Utils.listObservations(sender, null, null);
				return true;
			} else {
				
				String argsString = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
				
				Matcher matcher = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(argsString);
				
				List<String> params = new ArrayList<>();
				
				while (matcher.find())
				    params.add(matcher.group(1).replace("\"", ""));
				
				String player = null;
				String world = null;
				
				for (int ind = 0; ind < params.size() - 1; ind++) {
					String param = params.get(ind).toLowerCase();
					
					if (param.equalsIgnoreCase("-p")) {
						player = params.get(ind + 1);
					}
					if (param.equalsIgnoreCase("-w")) {
						world = params.get(ind + 1);
					}
				}
				
				Utils.debug("Player: " + player + " | World: " + world);
				
				if (player == null && world == null) {
					Utils.msg(sender, "&cIncorrect parameter usage!",
							"  &f&o/observations list <-p [player]> <-w [\"world\"]>",
							"  &7Example: /observations list -p Poi -w \"Redstone World\"");
					return true;
				}
				
				Utils.listObservations(sender, player, world);
				return true;
			}
		}
		
		// ********
		//  /observations near [radius]
		// ********
		if (subCmd.equalsIgnoreCase("near")) {
			if (!(sender instanceof Player)) {
				Utils.msg(sender, "&cYou must be a player to use this command!");
				return true;
			}
			
			if (args.length == 1) {
				Utils.msg(sender, "&cIncorrect usage!",
						"  &f&o/observations near [radius]");
				return true;
			}
			
			int radius;
			try {
				radius = Integer.parseInt(args[1]);
			} catch (NumberFormatException exc) {
				Utils.msg(sender, "&c\"&4" + args[1] + "&c\" is an invalid number!");
				return true;
			}
			
			double radiusSquared = Math.pow(radius, 2);
			
			Player player = (Player) sender;
			boolean any = false;
			for (Observation obs : Observation.getObservations()) {
				if (!obs.getHoloLocation().getWorld().getName().equalsIgnoreCase(
						player.getLocation().getWorld().getName())) continue;
				if (player.getLocation().distanceSquared(obs.getHoloLocation()) > radiusSquared) continue;
				
				if (!any) {
					Utils.msgNoPrefix(sender, "&7&m-----------------&r &9&lObservation List&r &7&m------------------",
							"  &9Radius: &7&o" + radius + " &7block" + (radius == 1 ? "" : "s"),
							"");
					any = true;
				}
				
				Utils.msgNoPrefix(sender, " &7- " + obs.toString());
			}
			
			if (any) {
				Utils.msgNoPrefix(sender, "&7&m-----------------------------------------------------");
			} else {
				Utils.msg(sender, "&cThere are no observations within &4" + 
						radius + " &cblock" + (radius == 1 ? "" : "s") + " of you!");
			}
			
			
			return true;
		}
		
		// ********
		//  /observations info [id]
		// ********
		if (subCmd.equalsIgnoreCase("info")) {
			if (args.length == 1) {
				Utils.msg(sender, "&cIncorrect usage!",
						"  &f&o/observations info [id]");
				return true;
			}
			
			int id;
			try {
				id = Integer.parseInt(args[1]);
			} catch (NumberFormatException exc) {
				Utils.msg(sender, "&c\"&4" + args[1] + "&c\" is an invalid number!");
				return true;
			}
			
			Observation obs = Observation.getObservation(id);
			if (obs == null) {
				Utils.msg(sender, "&c\"&4" + id + "&c\" is not a valid observation id!");
				return true;
			}
			
			Utils.msgNoPrefix(sender, "&7&m-----------------&r &9&lObservation Info&r &7&m------------------",
					"  &9ID: &7" + obs.getId(),
					"  &9Created: &7" + Utils.getDate(obs.getTimestamp()),
					"  &9Player: &7" + obs.getPlayer(),
					"  &9Observation: &8\"&f&l" + obs.getObservation() + "&8\"",
					"",
					"  &9Holo Location:",
					"    " + Utils.locationString(obs.getHoloLocation(), false),
					"  &9View Location:",
					"    " + Utils.locationString(obs.getViewLocation(), true),
					"&7&m-----------------------------------------------------");
			return true;
		}
		
		// ********
		//  /observations teleport [id]
		// ********
		if (subCmd.equalsIgnoreCase("teleport")) {
			if (!(sender instanceof Player)) {
				Utils.msg(sender, "&cYou must be a player to use this command!");
				return true;
			}
			
			if (args.length == 1) {
				Utils.msg(sender, "&cIncorrect usage!",
						"  &f&o/observations teleport [id]");
				return true;
			}
			
			int id;
			try {
				id = Integer.parseInt(args[1]);
			} catch (NumberFormatException exc) {
				Utils.msg(sender, "&c\"&4" + args[1] + "&c\" is an invalid number!");
				return true;
			}
			
			Observation obs = Observation.getObservation(id);
			if (obs == null) {
				Utils.msg(sender, "&c\"&4" + id + "&c\" is not a valid observation id!");
				return true;
			}
			
			Player player = (Player) sender;
			player.teleport(obs.getViewLocation());
			Utils.msg(sender, "&aYou have been teleported to observation \"&2" + id + "&a\"!");
			return true;
		}
		
		// ********
		//  /observations remove [id]
		// ********
		if (subCmd.equalsIgnoreCase("remove")) {
			if (args.length == 1) {
				Utils.msg(sender, "&cIncorrect usage!",
						"  &f&o/observations info [id]");
				return true;
			}
			
			int id;
			try {
				id = Integer.parseInt(args[1]);
			} catch (NumberFormatException exc) {
				Utils.msg(sender, "&c\"&4" + args[1] + "&c\" is an invalid number!");
				return true;
			}
			
			Observation obs = Observation.getObservation(id);
			if (obs == null) {
				Utils.msg(sender, "&c\"&4" + id + "&c\" is not a valid observation id!");
				return true;
			}
			
			obs.remove();
			Utils.msg(sender, "&aObservation \"&2" + id + "&a\" removed!");
			return true;
		}
		
		// ********
		//  /observations removeall <-p [player]> <-w ["world"]>
		// ********
		if (subCmd.equalsIgnoreCase("removeall")) {
			
			if (args.length == 1) {
				Utils.msg(sender, "&cIncorrect usage!",
						"  &f&o/observations removeall <-p [player]> <-w [\"world\"]> ");
				return true;
			}
			
			String argsString = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
			
			Matcher matcher = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(argsString);
			
			List<String> params = new ArrayList<>();
			
			while (matcher.find())
			    params.add(matcher.group(1).replace("\"", ""));
			
			String player = null;
			String world = null;
			
			for (int ind = 0; ind < params.size() - 1; ind++) {
				String param = params.get(ind).toLowerCase();
				
				if (param.equalsIgnoreCase("-p")) {
					player = params.get(ind + 1);
				}
				if (param.equalsIgnoreCase("-w")) {
					world = params.get(ind + 1);
				}
			}
			
			Utils.debug("Player: " + player + " | World: " + world);
			
			if (player == null && world == null) {
				Utils.msg(sender, "&cIncorrect parameter usage!",
						"  &f&o/observations removeall <-p [player]> <-w [\"world\"]>",
						"  &7Examples:",
						"    /observations removeall -p Poi -w \"Redstone World\"",
						"    /observations removeall -p Poi",
						"    /observations removeall -w NoMoonFinal_");
				return true;
			}
			
			Iterator<Observation> obsIterator = Observation.getObservationsIterator();
			while(obsIterator.hasNext()) {
				
				Observation observation = obsIterator.next();
				
				Utils.debug("Attempting to remove " + observation.toString());
				
				boolean remove = false;
				
				if (player == null) {
					
					if (observation.getHoloLocation().getWorld().getName().equalsIgnoreCase(world)) {
						remove = true;
					}
					
				} else if (world == null) {
					
					if (observation.getPlayer().equalsIgnoreCase(player)) {
						remove = true;
					}
					
				} else if (observation.getHoloLocation().getWorld().getName().equalsIgnoreCase(world) && 
						observation.getPlayer().equalsIgnoreCase(player)) {
					remove = true;
				}
				
				if (!remove) continue;
				
				Utils.msgNoPrefix(sender, "&7Removed " + observation);
				observation.manualRemove();
				obsIterator.remove();
			}
			
		}
		
		return true;
	}
	
	private static boolean isSubCommand(String arg) {
		for (String str : SUB_COMMANDS) {
			if (str.equalsIgnoreCase(arg)) return true;
		}
		return false;
	}
	
	private static void sendSubCommands(CommandSender sender) {
		Utils.msgNoPrefix(sender,
				"&7&m------------------&r &8&l[&9&lObservations&8&l]&r &7&m------------------",
				"",
				"&7/observations &flist &7<&9-p&7 &9[player]&7> <&9-w&7 &9[\"world\"]&7> &8- &fLists observations",
				"&7/observations &fnear &9[radius] &8- &fLists observations near you",
				"&7/observations &finfo &9[id] &8- &fGives info about an observation",
				"&7/observations &fteleport &9[id] &8- &fTeleports you to an observation",
				"&7/observations &fremove &9[id] &8- &fMakes an observation inactive",
				"&7/observations &fremoveall &7<&9-p&7 &9[player]&7> <&9-w&7 &9[\"world\"]&7> &8- " + 
						"&fRemove observations matching the given arguments",
				"",
				"&7&m-----------------------------------------------------");
	}
}
