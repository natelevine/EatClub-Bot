package com.eatclubasaservice.app.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MealList {

    private List<Meal> meals;

    public MealList(List<Meal> meals) {
        this.meals = meals;
    }

    @JsonProperty
    public List<Meal> getMeals() {
        return meals;
    }
}
