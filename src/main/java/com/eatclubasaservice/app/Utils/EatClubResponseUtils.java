package com.eatclubasaservice.app.Utils;

import com.eatclubasaservice.app.core.Meal;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.ws.rs.core.NewCookie;
import java.util.Map;
import java.util.Set;

/**
 * Static parsing utils
 */
public class EatClubResponseUtils {

    public static String getCookieStringFromMap(Map<String, NewCookie> cookies) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, NewCookie> cookieEntry : cookies.entrySet()) {
            String cookieName = cookieEntry.getKey();
            String cookieValue = cookieEntry.getValue().toCookie().getValue();
            stringBuilder.append(String.format("%s=%s; ", cookieName, cookieValue));
        }
        return stringBuilder.toString();
    }

    public static Set<Long> parseOrderIdsFromFutureOrderArray(JsonArray existingOrders) {

        Set<Long> existingOrderIds = Sets.newHashSet();

        for (JsonElement element : existingOrders) {
            JsonArray ordersForOneDay = element.getAsJsonObject().get("order_items").getAsJsonArray();
            JsonElement idElement = ordersForOneDay.get(0).getAsJsonObject().get("id");
            existingOrderIds.add(Long.parseLong(idElement.getAsString()));
        }
        return existingOrderIds;
    }

    public static Set<Meal> parseDailyMeals(JsonObject mealsObject) {

        Set<Meal> dailyMeals = Sets.newHashSet();
        for (Map.Entry<String, JsonElement> entry : mealsObject.entrySet()) {
            JsonObject item = entry.getValue().getAsJsonObject();

            Long id = item.get("id").getAsLong();
            String itemName = item.get("item").getAsString();
            String photoURL = item.get("photo").getAsJsonObject().get("url").getAsString();

            dailyMeals.add(new Meal(id, itemName, photoURL));
        }
        return dailyMeals;
    }
}
