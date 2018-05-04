package com.eatclubasaservice.app.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PreferenceList {

    private UserRepresentation userRepresentation;

    private List<Long> preferences;

    public PreferenceList(UserRepresentation userRepresentation, List<Long> preferences) {
        this.userRepresentation = userRepresentation;
        this.preferences = preferences;
    }

    @JsonProperty
    public UserRepresentation getUserRepresentation() {
        return userRepresentation;
    }

    @JsonProperty
    public List<Long> getPreferences() {
        return preferences;
    }
}
