package com.eatclubasaservice.app.jobs;

import com.eatclubasaservice.app.EatClubBotApplication;
import com.eatclubasaservice.app.Services.EatClubAPIService;
import com.eatclubasaservice.app.Utils.EatClubResponseUtils;
import com.eatclubasaservice.app.core.Meal;
import com.eatclubasaservice.app.db.MealDAO;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.annotations.On;
import de.spinscale.dropwizard.jobs.annotations.OnApplicationStart;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Optional;
import java.util.Set;

@OnApplicationStart
@On("1 0 17 ? * *")
public class ScrapeAvailableMealsJob extends Job {

    final String EMAIL = "raymond.chang@lendup.com";
    final String PASSWORD = "ilovechickentostadasalad";

    final EatClubAPIService eatClubAPIService;

    public ScrapeAvailableMealsJob(EatClubAPIService eatClubAPIService) {
        this.eatClubAPIService = eatClubAPIService;
    }

    @Override
    public void doJob(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String cookie = EatClubResponseUtils.getCookieStringFromMap(eatClubAPIService.login(EMAIL, PASSWORD));

        Set<Meal> allMeals = Sets.newHashSet();
        for (int day = 1; day <= 5; day++) {
            Optional<JsonObject> itemsOpt = eatClubAPIService.getDailyMenuItems(day, cookie);

            if (!itemsOpt.isPresent()) {
                continue;
            }

            allMeals.addAll(EatClubResponseUtils.parseDailyMeals(itemsOpt.get()));
        }

        SessionFactory sessionFactory = EatClubBotApplication.getSessionFactory();
        MealDAO mealDAO = new MealDAO(sessionFactory);
        Session session = sessionFactory.openSession();

        ManagedSessionContext.bind(session);
        Transaction transaction = session.beginTransaction();
        try {
            for (Meal meal : allMeals) {
                mealDAO.create(meal);
            }
            transaction.commit();
            session.close();
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
            ManagedSessionContext.unbind(sessionFactory);
        }
    }
}
