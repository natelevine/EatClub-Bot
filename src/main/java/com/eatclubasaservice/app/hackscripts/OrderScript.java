package com.eatclubasaservice.app.hackscripts;

import com.eatclubasaservice.app.Services.EatClubAPIService;
import com.eatclubasaservice.app.Utils.EatClubResponseUtils;
import com.eatclubasaservice.app.core.Meal;
import com.eatclubasaservice.app.core.User;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.ws.rs.core.NewCookie;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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

        // shortcut, no need to order
        if (existingOrderIds.size() == 5) {
            return;
        }
        Set<String> existingOrderDateStrings = EatClubResponseUtils.parseOrderDatesFromFutureOrdersArray(existingOrders.get());

        Set<Meal> existingOrderMeals = Sets.newHashSet();
        for (Long id : existingOrderIds) {
            // we only care about the id for equality
            existingOrderMeals.add(new Meal(id, "dummyname", "dummyUrl"));
        }

        String cookieString = EatClubResponseUtils.getCookieStringFromMap(cookies);

        JsonObject mealDaysMap = eatClubAPIService.getAvailableMealDays(cookies);
        Set<String> stringDaysSet = mealDaysMap.keySet();
        // there should only be 5 at a time
        List<String> sortedMealDateStrings = Lists.newArrayList(stringDaysSet);
        Collections.sort(sortedMealDateStrings);

        Iterator<String> mealDatesIterator = sortedMealDateStrings.iterator();
        for (int orderDayIndex = 1; orderDayIndex <= 5; orderDayIndex++) {
            Optional<JsonObject> todaysMenuItems = eatClubAPIService.getDailyMenuItems(orderDayIndex, cookieString);

            // If its a holiday, weekend, or no LendUp meal available
            if (!todaysMenuItems.isPresent()) {
                continue;
            }

            Set<Meal> todaysMeals = EatClubResponseUtils.parseDailyMeals(todaysMenuItems.get());

            Optional<Meal> mealToOrder = EatClubResponseUtils.getMostSuitableMeal(user.getMealPreferences(), existingOrderMeals, todaysMeals);
            String nextDateToOrder = mealDatesIterator.next();
            if (!mealToOrder.isPresent() || existingOrderDateStrings.contains(nextDateToOrder)) {
                // TODO: Notify the user here... we're not ordering
                continue;
            }
            // Get Order Id for Cart
            Long orderId = eatClubAPIService.getOrderIdForDate(LocalDate.parse(nextDateToOrder), cookies, orderDayIndex);
            eatClubAPIService.putOrderIntoCart(orderId, mealToOrder.get().getId(), cookies, orderDayIndex);

            // cache the meal we just ordered to check for next time
            existingOrderMeals.add(mealToOrder.get());
            eatClubAPIService.checkout(cookies, orderDayIndex);
            orderDayIndex++;
        }
    }
}
