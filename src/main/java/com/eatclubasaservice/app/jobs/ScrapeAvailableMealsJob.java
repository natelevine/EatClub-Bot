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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

@OnApplicationStart
public class ScrapeAvailableMealsJob extends Job {

    final String EMAIL = "raymond.cj.chang@gmail.com";
    final String PASSWORD = "ilovechickentostadasalad";

    @Override
    public void doJob(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String cookie = getLoginCookie(EMAIL, PASSWORD);

        SessionFactory sessionFactory = EatClubBotApplication.getSessionFactory();

        for (int day = 1; day <= 5; day++) {
            Optional<JsonObject> itemsOpt = getItemsForDay(day, cookie);

            if (!itemsOpt.isPresent()) {
                continue;
            }

            for (Map.Entry<String, JsonElement> entry : itemsOpt.get().entrySet()) {
                JsonObject item = entry.getValue().getAsJsonObject();

                Long id = item.get("id").getAsLong();
                String itemName = item.get("item").getAsString();
                String photoURL = item.get("photo").getAsJsonObject().get("url").getAsString();

                Meal meal = new Meal(id, itemName, photoURL);
                persistMealEntity(meal, sessionFactory);
            }
        }
    }

    /**
     * Fetches the cookie that contains the session ID by logging in with the provided credentials
     * @return A cookie containing sessionID to be used by other API Requests
     */
    private String getLoginCookie(String email, String password) {
        Client client = ClientBuilder.newClient();

        Form form = new Form();
        form.param("email", email);
        form.param("password", password);

        Response response = client
            .target("https://www.eatclub.com")
            .path("/public/api/log-in/")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .put(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        Map<String, NewCookie> cookies = response.getCookies();
        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, NewCookie> cookieEntry : cookies.entrySet()) {
            String cookieName = cookieEntry.getKey();
            String cookieValue = cookieEntry.getValue().toCookie().getValue();
            stringBuilder.append(String.format("%s=%s; ", cookieName, cookieValue));
        }

        return stringBuilder.toString();
    }

    private Optional<JsonObject> getItemsForDay(int day, String cookie) {
        Client client = ClientBuilder.newClient();
        JsonParser jsonParser = new JsonParser();

        String responseJSON = client
            .target("https://www.eatclub.com")
            .path("/menus")
            .queryParam("categorized_menu", true)
            .queryParam("day", day)
            .queryParam("menu_type", "individual")
            .request(MediaType.APPLICATION_JSON)
            .header("cookie", cookie)
            .get(String.class);

        JsonObject jsonObject = jsonParser.parse(responseJSON).getAsJsonObject();

        // skip day when Eat Club doesn't have items
        if (!jsonObject.has("items")) {
            return Optional.empty();
        }

        JsonObject items = jsonObject.get("items").getAsJsonObject();
        return Optional.of(items);
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
