package com.eatclubasaservice.app.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MealList {

    private List<MealRepresentation> mealRepresentations;

    public MealList(List<MealRepresentation> mealRepresentations) {
        this.mealRepresentations = mealRepresentations;
    }

    @JsonProperty
    public List<MealRepresentation> getMealRepresentations() {
        return mealRepresentations;
    }
}
