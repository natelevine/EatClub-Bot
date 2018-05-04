package com.eatclubasaservice.app.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PreferenceList {

    private UserRepresentation userRepresentation;

    private List<Long> preferences;

    @JsonCreator
    public PreferenceList(@JsonProperty("userRepresentation") UserRepresentation userRepresentation, @JsonProperty("preferences") List<Long> preferences) {
        this.userRepresentation = userRepresentation;
        this.preferences = preferences;
    }

    public UserRepresentation getUserRepresentation() {
        return userRepresentation;
    }

    public List<Long> getPreferences() {
        return preferences;
    }
}
