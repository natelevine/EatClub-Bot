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
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.eatclubasaservice.app.api.MealRepresentation;
import com.eatclubasaservice.app.api.MealList;
import com.eatclubasaservice.app.api.PreferenceList;
import com.eatclubasaservice.app.api.UserRepresentation;
import com.eatclubasaservice.app.core.Meal;
import com.eatclubasaservice.app.core.Preference;
import com.eatclubasaservice.app.core.User;
import com.eatclubasaservice.app.db.MealDAO;
import com.eatclubasaservice.app.db.PreferenceDAO;
import com.eatclubasaservice.app.db.UserDAO;
import com.eatclubasaservice.app.utils.Encryption;
import com.google.common.collect.Lists;
import io.dropwizard.hibernate.UnitOfWork;

import java.util.List;
import java.util.Optional;


@Path("/")
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
    public void setPreferenceList(@NotNull @Valid PreferenceList preferenceList) {

        UserRepresentation userRepresentation = preferenceList.getUserRepresentation();
        Optional<User> userOption = userDAO.findByEmail(userRepresentation.getEmail());

        User user;
        if (userOption.isPresent()) {
            // delete old prefs
            user = userOption.get();
            user.deleteAllPrefs();
        } else {
            // create user
            // TODO: deal with password hashing
            String encryptedPw;
            try {
                encryptedPw = Encryption.encrypt(userRepresentation.getPassword());
            } catch(Exception e) {
                Response.serverError();
                return;
            }

            user = new User(userRepresentation.getEmail(), encryptedPw);
            userDAO.create(user);
        }

        // create new preferences
        int rank = 1;
        for (Long mealPreferenceId : preferenceList.getPreferences()) {

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
        Response.ok();
    }

    @DELETE
    @Timed
    @UnitOfWork
    public void unsubscribe(@NotNull @Valid UserRepresentation userRepresentation) {

        Optional<User> userOption = userDAO.findByEmail(userRepresentation.getEmail());
        if (userOption.isPresent()) {
            User user = userOption.get();
            user.deleteAllPrefs();
            userDAO.delete(user);
        }
        Response.ok();
    }
}