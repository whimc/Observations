package edu.whimc.observationdisplayer.utils;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import edu.whimc.observationdisplayer.Observation;

public class Utils {
	
	private static boolean debug = false;
	private static String debugPrefix = "[Observations] ";
	
	private static final String prefix = "&8&l[&9&lObservations&8&l]&r ";
	
	/**
	 * Prints a debug message if "debug" is true.
	 * @param str Message to print
	 */
	public static void debug(String str) {
		if (!debug) return;
		Bukkit.getLogger().info(color(debugPrefix + str));
	}
	
	/**
	 * Sets the debug message prefix.
	 * @param prefix Prefix to be set
	 */
	public static void setDebugPrefix(String prefix) {
		debugPrefix = "[" + prefix + "] ";
	}
	
	/**
	 * Gets a nice formatted date.
	 * @param timestamp Timestamp of date to format
	 * @return A formatted version of the given date
	 */
	public static String getDate(Timestamp timestamp) {
		String pattern = "MMMM d, h:mm a z";
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		
		return format.format(new Date(timestamp.getTime()));
	}
	
	public static void msg(CommandSender sender, String... messages) {
		for (int ind = 0; ind < messages.length; ind++) {
			if (ind == 0) {
				sender.sendMessage(color(prefix + messages[ind]));
			} else {
				sender.sendMessage(color(messages[ind]));
			}
		}
	}
	
	public static String locationString(Location loc, boolean yawPitch) {
		NumberFormat formatter = new DecimalFormat("#0.00");
		
		StringBuilder message = new StringBuilder();
		message.append("&7World: &f&o" + loc.getWorld().getName());
		message.append("  &7X: &f&o" + formatter.format(loc.getX()));
		message.append("  &7Y: &f&o" + formatter.format(loc.getY()));
		message.append("  &7Z: &f&o" + formatter.format(loc.getZ()));
		
		if (yawPitch) {
			message.append("\n" + "    &7Pitch: &f&o" + formatter.format(loc.getPitch()));
			message.append("  &7Yaw: &f&o" + formatter.format(loc.getYaw()));
		}
		
		return message.toString();
	}
	
	public static void listObservations(CommandSender sender, String player, String world) {
		if (Observation.getObservations().size() == 0) {
			Utils.msg(sender, "&7There are currently no observations!");
			return;
		}
		
		Utils.msgNoPrefix(sender, "&7&m-----------------&r &9&lObservation List&r &7&m------------------",
				"  &9Player: " + (player == null ? "&7N/A" : "&8\"&7&o" + player + "&8\"") +
				"    &9World: " + (world == null ? "&7N/A" : "&8\"&7&o" + world + "&8\""),
				"");
		
		for (Observation obs : Observation.getObservations()) {
			if (player != null && !player.equalsIgnoreCase(obs.getPlayer())) continue;
			if (world != null && !world.equalsIgnoreCase(obs.getHoloLocation().getWorld().getName())) continue;
			
			Utils.msgNoPrefix(sender, " &7- " + obs.toString());
		}
		
		Utils.msgNoPrefix(sender, "&7&m-----------------------------------------------------");
	}
	
	public static String coloredSubstring(String str, int length) {
		str = color(str);
		StringBuilder newStr = new StringBuilder();
		int count = 0;
		boolean ignore = false;
		for (char chr : str.toCharArray()) {
			if (count >= length) break;
			newStr.append(chr);
			
			if (ignore) {
				ignore = false;
				continue;
			}
			
			if (chr == ChatColor.COLOR_CHAR) ignore = true;
			if (chr != ChatColor.COLOR_CHAR && !ignore) count++;
		}
		
		return newStr.toString().replace(ChatColor.COLOR_CHAR, '&');
	}
	
	public static void msgNoPrefix(CommandSender sender, String... messages) {
		for (String str : messages) {
			sender.sendMessage(color(str));
		}
	}
	
	public static String color(String str) {
		return ChatColor.translateAlternateColorCodes('&', str);
	}
	
}
