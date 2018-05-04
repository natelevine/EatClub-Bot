package com.eatclubasaservice.app.jobs;

import com.eatclubasaservice.app.EatClubBotApplication;
import com.eatclubasaservice.app.core.Meal;
import com.eatclubasaservice.app.db.MealDAO;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.annotations.OnApplicationStart;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

@OnApplicationStart
public class ScrapeAvailableMealsJob extends Job {

    @Override
    public void doJob(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        try {
            JsonParser jsonParser = new JsonParser();
            File jsonFile = new File("./src/main/resources/eatClub.json");
            JsonObject jsonObject = jsonParser.parse(new FileReader(jsonFile)).getAsJsonObject();

            JsonObject items = jsonObject.get("items").getAsJsonObject();
            SessionFactory sessionFactory = EatClubBotApplication.getSessionFactory();

            for (Map.Entry<String,JsonElement> entry : items.entrySet()) {
                JsonObject item = entry.getValue().getAsJsonObject();

                Long id = item.get("id").getAsLong();
                String itemName = item.get("item").getAsString();
                String photoURL = item.get("photo").getAsJsonObject().get("url").getAsString();

                Meal meal = new Meal(id, itemName, photoURL);
                persistMealEntity(meal, sessionFactory);
            }
        }
        catch (FileNotFoundException e) {
            System.err.println("Cannot load file");
            e.printStackTrace();
        }
    }

    private void persistMealEntity(Meal meal, SessionFactory sessionFactory) {
        Session session = sessionFactory.openSession();
        try {
            ManagedSessionContext.bind(session);
            Transaction transaction = session.beginTransaction();
            try {
                MealDAO mealDAO = new MealDAO(sessionFactory);
                mealDAO.create(meal);
                transaction.commit();
                session.close();
            }
            catch (Exception e) {
                transaction.rollback();
                throw new RuntimeException(e);
            }
        }
        finally {
            session.close();
            ManagedSessionContext.unbind(sessionFactory);
        }
    }
}
