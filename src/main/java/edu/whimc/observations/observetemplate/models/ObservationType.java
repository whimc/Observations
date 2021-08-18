package edu.whimc.observations.observetemplate.models;

import edu.whimc.observations.Observations;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum ObservationType {

    ANALOGY,

    COMPARATIVE,

    DESCRIPTIVE,

    INFERENCE,

    QUESTION,
    ;

    @Override
    public String toString() {
        return StringUtils.capitalize(super.toString().toLowerCase());
    }

    public Set<ObservationType> getConfiguredTypes(Observations plugin) {
        return Arrays.stream(this.values())
                .filter(type -> plugin.getConfig().isSet("templates." + type))
                .collect(Collectors.toSet());
    }

}
