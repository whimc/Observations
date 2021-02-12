package edu.whimc.observationdisplayer.commands.observetemplate.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import edu.whimc.observationdisplayer.ObservationDisplayer;

public class TemplateGuiManager {

    private ObservationDisplayer plugin;

    private TemplateGui gui;

    public TemplateGuiManager(ObservationDisplayer plugin) {
        this.plugin = plugin;
        this.gui = new TemplateGui(plugin, this);
        Bukkit.getPluginManager().registerEvents(this.gui, plugin);
    }

    public void openInventory(Player player) {
        this.gui.openTemplateInventory(player);
    }

}
