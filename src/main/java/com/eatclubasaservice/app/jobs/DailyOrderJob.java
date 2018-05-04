package com.eatclubasaservice.app.jobs;

import com.eatclubasaservice.app.EatClubBotApplication;
import com.eatclubasaservice.app.core.Meal;
import com.eatclubasaservice.app.core.Preference;
import com.eatclubasaservice.app.core.User;
import com.eatclubasaservice.app.db.UserDAO;
import com.google.common.collect.Lists;
import de.spinscale.dropwizard.jobs.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

public class DailyOrderJob extends Job {

    @Override
    public void doJob(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        UserDAO userDAO = new UserDAO(EatClubBotApplication.getSessionFactory());
        List<User> allUsers = userDAO.findAll();

        for (User user : allUsers) {
            List<Preference> preferences = user.getMealPreferences();

        }

    }

    private List<Meal> getUsersExistingOrders() {

        // TODO: fetch user's existing orders from EatClub API
        return Lists.newArrayList();
    }

    private List<Meal> getTodaysMeals() {

        return Lists.newArrayList();
    }

    private long getMostSuitableMeal(List<Meal> userPreferences, List<Meal> existingOrders) {
        return 1L;
    }
}
