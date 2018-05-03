package com.eatclubasaservice.app.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Meal {

    private Long id;

    private String name;

    private String imageUrl;

    public Meal(Long id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    @JsonProperty
    public Long getId() {
        return id;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public String getImageUrl() {
        return imageUrl;
    }
}