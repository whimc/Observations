package edu.whimc.observations.models;

import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.module.BukkitCustomObjective;
import me.pikamug.quests.Quests;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;

/**
 * A custom observation objective using the Quest plugin.
 */
public class QuestObservationObjective extends BukkitCustomObjective {

    public QuestObservationObjective() {
        this.setName("Observation Objective");
        this.setAuthor("WHIMC");
        this.setShowCount(true);
        this.setCountPrompt("Enter the number of observations the player must make:");
        this.setDisplay("Make %count% observations");
    }

    @EventHandler
    public void onObserve(ObserveEvent event) {

        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("Quests");
        if (plugin == null) {
            return;
        }
        Quests quests = (Quests) plugin;

        for (Quest quest : quests.getQuester(event.getPlayer().getUniqueId()).getCurrentQuests().keySet()) {
            incrementObjective(event.getPlayer().getUniqueId(), this, quest, 1);
        }
    }

}
