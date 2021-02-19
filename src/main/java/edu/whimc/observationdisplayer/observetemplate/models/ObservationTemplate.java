package edu.whimc.observationdisplayer.observetemplate.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.whimc.observationdisplayer.ObservationDisplayer;

public class ObservationTemplate {

    private List<ObservationPrompt> prompts = new ArrayList<>();

    public ObservationTemplate(ObservationDisplayer plugin, ObservationType type) {
        String path = "templates." + type + ".prompts";

        List<Map<?, ?>> entries = plugin.getConfig().getMapList(path);
        for (Map<?, ?> entry : entries) {
            this.prompts.add(new ObservationPrompt(entry));
        }
    }

    public List<ObservationPrompt> getPrompts() {
        return this.prompts;
    }

}
