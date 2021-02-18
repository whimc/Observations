package edu.whimc.observationdisplayer.commands.observetemplate;

import java.util.HashMap;
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
                }
            });
        }

    }

    public TemplateGui getGui() {
        return this.gui;
    }

}
