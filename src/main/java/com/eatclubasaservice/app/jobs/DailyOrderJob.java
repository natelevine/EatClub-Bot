package com.eatclubasaservice.app.jobs;

import com.eatclubasaservice.app.EatClubBotApplication;
import com.eatclubasaservice.app.Services.EatClubAPIService;
import com.eatclubasaservice.app.Utils.EatClubResponseUtils;
import com.eatclubasaservice.app.core.Meal;
import com.eatclubasaservice.app.core.Preference;
import com.eatclubasaservice.app.core.User;
import com.eatclubasaservice.app.db.UserDAO;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.spinscale.dropwizard.jobs.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

//@OnApplicationStart
public class DailyOrderJob extends Job {

    final EatClubAPIService eatClubAPIService;

    public DailyOrderJob(EatClubAPIService eatClubAPIService) {
        this.eatClubAPIService = eatClubAPIService;
    }

    @Override
    public void doJob(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        for (User user : getUsers()) {

            Map<String, NewCookie> cookies = eatClubAPIService.login(user.getEmail(), user.getPassword());
            Optional<JsonArray> existingOrders = eatClubAPIService.getUsersExistingOrders(cookies);

            Set<Long> existingOrderIds = Sets.newHashSet();
            if (existingOrders.isPresent()) {
                existingOrderIds = EatClubResponseUtils.parseOrderIdsFromFutureOrderArray(existingOrders.get());
            }

            String cookieString = EatClubResponseUtils.getCookieStringFromMap(cookies);
            Optional<JsonObject> todaysMenuItems = eatClubAPIService.getDailyMenuItems(5, cookieString);

            // If its a holiday, weekend, or no LendUp meal available
            if (!todaysMenuItems.isPresent()) {
                return;
            }

            Set<Meal> todaysMeals = EatClubResponseUtils.parseDailyMeals(todaysMenuItems.get());

            Set<Meal> existingOrderMeals = Sets.newHashSet();
            for (Long id : existingOrderIds) {
                // we only care about the id for equality
                existingOrderMeals.add(new Meal(id, "dummyname", "dummyUrl"));
            }

            Optional<Meal> mealToOrder = getMostSuitableMeal(user.getMealPreferences(), existingOrderMeals, todaysMeals);
            if (!mealToOrder.isPresent()) {
                // TODO: Notify the user here... we're not ordering
                continue;
            }
            // Get Order Id for Cart
            Long orderId = eatClubAPIService.getOrderIdForDate(LocalDate.now().plusDays(7), cookies);
            eatClubAPIService.putOrderIntoCart(orderId, mealToOrder.get().getId(), cookies);
            eatClubAPIService.checkout(cookies);
        }

    }

    private List<User> getUsers() {
        UserDAO userDAO = new UserDAO(EatClubBotApplication.getSessionFactory());
        return userDAO.findAll();
    }

    /**
     * Fetches the cookie that contains the session ID by logging in with the provided credentials
     * @return A cookie containing sessionID to be used by other API Requests
     */
    private Map<String, NewCookie> getLoginCookie(String email, String password) {
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

        return cookies;
    }


    private List<Meal> getTodaysMeals() {

        return Lists.newArrayList();
    }

    private Optional<Meal> getMostSuitableMeal(List<Preference> userPreferences, Set<Meal> existingOrders, Set<Meal> todaysMeals) {
        for (Preference preference : userPreferences) {
            Meal meal = preference.getMeal();
            if (todaysMeals.contains(meal) && !existingOrders.contains(meal)) {
                return Optional.of(meal);
            }
        }
        return Optional.empty();
    }
}
