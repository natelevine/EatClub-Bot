package com.eatclubasaservice.app.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.codahale.metrics.annotation.Timed;
import com.eatclubasaservice.app.api.Meal;
import com.eatclubasaservice.app.api.MealList;
import com.eatclubasaservice.app.db.MealDAO;
import com.eatclubasaservice.app.db.PreferenceDAO;
import com.eatclubasaservice.app.db.UserDAO;
import com.google.common.collect.Lists;

import java.util.List;


@Path("/index")
@Produces(MediaType.APPLICATION_JSON)
public class IndexResource {

    private UserDAO userDAO;

    private PreferenceDAO preferenceDAO;

    private MealDAO mealDAO;

    public IndexResource(UserDAO userDAO, PreferenceDAO preferenceDAO, MealDAO mealDAO) {
        this.userDAO = userDAO;
        this.preferenceDAO = preferenceDAO;
        this.mealDAO = mealDAO;
    }

    @GET
    @Timed
    public MealList getAllKnownMeals() {
        List<Meal> allMeals = mealDAO.findAll();
        return new MealList(allMeals);
    }

    @POST
    @Timed
    public void setPreferenceList() {

    }

    @DELETE
    @Timed
    public void unsubscribe() {

    }
}