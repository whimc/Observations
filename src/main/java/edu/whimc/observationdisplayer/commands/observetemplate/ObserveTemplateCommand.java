package edu.whimc.observationdisplayer.commands.observetemplate;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edu.whimc.observationdisplayer.ObservationDisplayer;
import edu.whimc.observationdisplayer.commands.observetemplate.gui.TemplateGuiManager;
import edu.whimc.observationdisplayer.utils.Utils;

public class ObserveTemplateCommand implements CommandExecutor {

    private TemplateGuiManager manager;

    public ObserveTemplateCommand(ObservationDisplayer plugin) {
        this.manager = new TemplateGuiManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Utils.msg(sender, ChatColor.RED + "You must be a player!");
            return true;
        }

        if (!sender.hasPermission("observations.observetemplate")) {
            Utils.msg(sender,
                    "&cYou do not have the required permission!",
                    "  &f&oobservations.observetemplate");
            return true;
        }

        this.manager.openInventory((Player) sender);

        return true;
    }

}
