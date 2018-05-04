package com.eatclubasaservice.app.resources;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.codahale.metrics.annotation.Timed;
import com.eatclubasaservice.app.api.MealRepresentation;
import com.eatclubasaservice.app.api.MealList;
import com.eatclubasaservice.app.api.UserRepresentation;
import com.eatclubasaservice.app.core.Meal;
import com.eatclubasaservice.app.core.Preference;
import com.eatclubasaservice.app.core.User;
import com.eatclubasaservice.app.db.MealDAO;
import com.eatclubasaservice.app.db.PreferenceDAO;
import com.eatclubasaservice.app.db.UserDAO;
import com.google.common.collect.Lists;
import io.dropwizard.hibernate.UnitOfWork;

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
    @UnitOfWork
    public MealList getAllKnownMeals() {
        List<Meal> allMeals = mealDAO.findAll();
        List<MealRepresentation> mealDTOs = Lists.newArrayList();
        for (Meal meal : allMeals) {
            mealDTOs.add(meal.getMealRepresentation());
        }
        return new MealList(mealDTOs);
    }

    @POST
    @Timed
    @UnitOfWork
    @Consumes(MediaType.APPLICATION_JSON)
    public void setPreferenceList(@NotNull @Valid UserRepresentation userRepresentation, @NotNull List<Long> mealPreferenceIds) {

        User user = userDAO.findByEmail(userRepresentation.getEmail());
        
        if (user != null) {
            // delete old prefs
            for (Preference userPreference : user.getMealPreferences()) {
                preferenceDAO.delete(userPreference);
            }
        } else {
            // create user
            // TODO: deal with password hashing
            user = new User(userRepresentation.getEmail(), userRepresentation.getPassword());
        }

        // create new preferences
        int rank = 1;
        for (Long mealPreferenceId : mealPreferenceIds) {

            Meal meal = mealDAO.findById(mealPreferenceId);
            if (meal != null) {
                Preference mealPreference = new Preference(user, meal, rank);
                preferenceDAO.create(mealPreference);
                rank++;
                // hacky safety mechanism just in case
                if (rank > 20) {
                    break;
                }
            }
        }
    }

    @DELETE
    @Timed
    @UnitOfWork
    public void unsubscribe(@NotNull @Valid UserRepresentation userRepresentation) {


    }
}