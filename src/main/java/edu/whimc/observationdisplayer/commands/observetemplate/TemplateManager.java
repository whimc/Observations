package edu.whimc.observationdisplayer.commands.observetemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.whimc.observationdisplayer.ObservationDisplayer;
import edu.whimc.observationdisplayer.commands.observetemplate.gui.TemplateGui;
import edu.whimc.observationdisplayer.utils.Utils;

public class TemplateManager {

    private TemplateGui gui;

    private Map<ObservationType, ObservationTemplate> templates = new HashMap<>();

    public TemplateManager(ObservationDisplayer plugin) {
        this.gui = new TemplateGui(plugin);

        for (ObservationType type : ObservationType.values()) {
            ObservationTemplate template = new ObservationTemplate(plugin, type);

            this.templates.put(type, template);
            this.gui.addConsumer(type, player -> {

                Utils.msgNoPrefix(player, "Prompts for " + type + ":");

                for (ObservationPrompt prompt : template.getPrompts()) {
                    Utils.msgNoPrefix(player, prompt.getPrompt());
                    int size = prompt.getNumberOfFillIns();
                    for (int ind = 0; ind < size; ind++) {
                        List<String> fillins = prompt.getResponses(player.getWorld(), ind);
                        for (String fillin : fillins) {
                            Utils.msgNoPrefix(player, " " + ind + ": " + fillin);
                        }
                    }
                }
            });
        }

    }

    public TemplateGui getGui() {
        return this.gui;
    }

}
