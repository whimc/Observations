package edu.whimc.observationdisplayer.observetemplate;

import edu.whimc.observationdisplayer.ObservationDisplayer;
import edu.whimc.observationdisplayer.libraries.SpigotCallback;
import edu.whimc.observationdisplayer.observetemplate.gui.TemplateGui;
import edu.whimc.observationdisplayer.observetemplate.gui.TemplateSelection;
import edu.whimc.observationdisplayer.observetemplate.models.ObservationTemplate;
import edu.whimc.observationdisplayer.observetemplate.models.ObservationType;

import java.util.HashMap;
import java.util.Map;

public class TemplateManager {

    private final TemplateGui gui;

    private final SpigotCallback spigotCallback;

    private final Map<ObservationType, ObservationTemplate> templates = new HashMap<>();

    public TemplateManager(ObservationDisplayer plugin) {
        this.spigotCallback = new SpigotCallback(plugin);

        for (ObservationType type : ObservationType.values()) {
            ObservationTemplate template = new ObservationTemplate(plugin, type);
            this.templates.put(type, template);
        }

        this.gui = new TemplateGui(plugin, this);
        for (ObservationType type : ObservationType.values()) {
            this.gui.addConsumer(type, player -> {
                new TemplateSelection(plugin, this.spigotCallback, player, getTemplate(type));
            });
        }
    }

    public TemplateGui getGui() {
        return this.gui;
    }

    public ObservationTemplate getTemplate(ObservationType type) {
        return this.templates.get(type);
    }

}
