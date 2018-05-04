package com.eatclubasaservice.app.Services;

import com.eatclubasaservice.app.Utils.EatClubResponseUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;


/**
 * Encapsulates Eat Club "API" access
 */
public class EatClubAPIService {

    /**
     * Fetches the cookie that contains the session ID by logging in with the provided credentials
     * @return A cookie containing sessionID to be used by other API Requests
     */
    public Map<String, NewCookie> login(String email, String password) {
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
        client.close();
        return cookies;
    }

    /**
     * Gets a json blob from Eat Club of all menu items available on a given day of the week
     * if none, return empty option
     * @param day
     * @param cookie
     * @return
     */
    public Optional<JsonObject> getDailyMenuItems(int day, String cookie) {

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
        client.close();

        // skip day when Eat Club doesn't have items
        if (!jsonObject.has("items")) {
            return Optional.empty();
        }

        JsonObject items = jsonObject.get("items").getAsJsonObject();
        return Optional.of(items);
    }

    /**
     * Gets a user's existing meal orders
     * @param cookies
     * @return
     */
    public Optional<JsonArray> getUsersExistingOrders(Map<String, NewCookie> cookies) {

        Client client = ClientBuilder.newClient();
        JsonParser jsonParser = new JsonParser();

        String xcsrfToken = cookies.get("csrftoken").getValue();
        String cookie = EatClubResponseUtils.getCookieStringFromMap(cookies);

        String responseJSON = client
                .target("https://www.eatclub.com")
                .path("/member/api/future-orders/")
                .queryParam("status", "active")
                .request(MediaType.APPLICATION_JSON)
                .header("cookie", cookie)
                .header("x-csrftoken", xcsrfToken)
                .get(String.class);

        JsonArray jsonArray = jsonParser.parse(responseJSON).getAsJsonArray();
        client.close();

        return Optional.of(jsonArray);
    }

    public Long getOrderIdForDate(LocalDate date, Map<String, NewCookie> cookies, int day) {

        Client client = ClientBuilder.newClient();
        JsonParser jsonParser = new JsonParser();

        String xcsrfToken = cookies.get("csrftoken").getValue();
        String cookie = EatClubResponseUtils.getCookieStringFromMap(cookies);

        Form form = new Form();
        form.param("date", "2018-05-09");
        Response response = client
                .target("https://www.eatclub.com")
                .path("/foodcourt/order/")
                .request(MediaType.APPLICATION_JSON)
                .header("cookie", cookie)
                .header("x-csrftoken", xcsrfToken)
                .header("authority", "www.eatclub.com")
                .header("origin", "https://www.eatclub.com")
                // Hardcoded day to 5 because we only order for the most recent meal
                .header("referer", String.format("https://www.eatclub.com/menu/%d", day))
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        JsonObject jsonObject = jsonParser.parse(response.readEntity(String.class)).getAsJsonObject();
        return jsonObject.get("id").getAsLong();
    }

    public void putOrderIntoCart(Long orderId, Long mealId, Map<String, NewCookie> cookies, int day) {
        Client client = ClientBuilder.newClient();

        String xcsrfToken = cookies.get("csrftoken").getValue();
        String cookie = EatClubResponseUtils.getCookieStringFromMap(cookies);

        Form form = new Form();
        form.param("count", "1");
        form.param("item_id", mealId.toString());
        form.param("side_item_id", null);

        client.target("https://www.eatclub.com")
                .path(String.format("/foodcourt/order/%s/item/%s/", orderId.toString(), mealId.toString()))
                .request(MediaType.APPLICATION_JSON)
                .header("cookie", cookie)
                .header("x-csrftoken", xcsrfToken)
                .header("authority", "www.eatclub.com")
                .header("origin", "https://www.eatclub.com")
                .header("referer", String.format("https://www.eatclub.com/menu/%d", day))
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

    }

    public void checkout(Map<String, NewCookie> cookies, int day) {
        Client client = ClientBuilder.newClient();

        String xcsrfToken = cookies.get("csrftoken").getValue();
        String cookie = EatClubResponseUtils.getCookieStringFromMap(cookies);

        Form form = new Form();

        client.target("https://www.eatclub.com")
                .path("/api/orders/checkout/")
                .request(MediaType.APPLICATION_JSON)
                .header("cookie", cookie)
                .header("x-csrftoken", xcsrfToken)
                .header("authority", "www.eatclub.com")
                .header("origin", "https://www.eatclub.com")
                .header("referer", String.format("https://www.eatclub.com/menu/%d", day))
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

    }
}
