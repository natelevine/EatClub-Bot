package com.eatclubasaservice.app.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class MealList {

    private ArrayList<Meal> meals;

    public MealList(ArrayList<Meal> meals) {
        this.meals = meals;
    }

    @JsonProperty
    public ArrayList<Meal> getMeals() {
        return meals;
    }
}
