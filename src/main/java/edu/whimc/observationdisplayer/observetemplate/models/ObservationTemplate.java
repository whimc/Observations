package edu.whimc.observationdisplayer.observetemplate.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.whimc.observationdisplayer.ObservationDisplayer;

public class ObservationTemplate {

    private List<ObservationPrompt> prompts = new ArrayList<>();

    private ObservationType type;

    private String title;

    public ObservationTemplate(ObservationDisplayer plugin, ObservationType type) {
        this.type = type;
        String path = "templates." + type.name() + ".prompts";

        List<Map<?, ?>> entries = plugin.getConfig().getMapList(path);
        for (Map<?, ?> entry : entries) {
            this.prompts.add(new ObservationPrompt(entry));
        }

        this.title = plugin.getConfig().getString("templates." + type.name() + ".gui.name");
    }

    public List<ObservationPrompt> getPrompts() {
        return this.prompts;
    }

    public String getTitle() {
        return this.title;
    }

    public ObservationType getType() {
        return this.type;
    }

}
