package edu.whimc.observations.observetemplate.models;

import org.apache.commons.lang.StringUtils;

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

}
