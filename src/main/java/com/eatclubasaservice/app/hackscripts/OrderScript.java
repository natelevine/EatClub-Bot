package com.eatclubasaservice.app.hackscripts;

import com.eatclubasaservice.app.Services.EatClubAPIService;
import com.eatclubasaservice.app.Utils.EatClubResponseUtils;
import com.eatclubasaservice.app.core.Meal;
import com.eatclubasaservice.app.core.User;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.ws.rs.core.NewCookie;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class OrderScript {

    private static EatClubAPIService eatClubAPIService = new EatClubAPIService();

    public static void orderColesFood(User user) {

        Map<String, NewCookie> cookies = eatClubAPIService.login(user.getEmail(), user.getPassword());
        Optional<JsonArray> existingOrders = eatClubAPIService.getUsersExistingOrders(cookies);

        Set<Long> existingOrderIds = Sets.newHashSet();
        if (existingOrders.isPresent()) {
            existingOrderIds = EatClubResponseUtils.parseOrderIdsFromFutureOrderArray(existingOrders.get());
        }

        Set<Meal> existingOrderMeals = Sets.newHashSet();
        for (Long id : existingOrderIds) {
            // we only care about the id for equality
            existingOrderMeals.add(new Meal(id, "dummyname", "dummyUrl"));
        }

        String cookieString = EatClubResponseUtils.getCookieStringFromMap(cookies);

        for (int day = 1; day < 5; day++) {
            Optional<JsonObject> todaysMenuItems = eatClubAPIService.getDailyMenuItems(day, cookieString);

            // If its a holiday, weekend, or no LendUp meal available
            if (!todaysMenuItems.isPresent()) {
                return;
            }

            Set<Meal> todaysMeals = EatClubResponseUtils.parseDailyMeals(todaysMenuItems.get());

            Optional<Meal> mealToOrder = EatClubResponseUtils.getMostSuitableMeal(user.getMealPreferences(), existingOrderMeals, todaysMeals);
            if (!mealToOrder.isPresent()) {
                // TODO: Notify the user here... we're not ordering
                return;
            }
            // Get Order Id for Cart
            Long orderId = eatClubAPIService.getOrderIdForDate(LocalDate.now().plusDays(day - 1), cookies, day);
            eatClubAPIService.putOrderIntoCart(orderId, mealToOrder.get().getId(), cookies, day);
            eatClubAPIService.checkout(cookies, day);
        }
    }
}
