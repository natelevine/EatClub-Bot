package com.eatclubasaservice.app.jobs;

import com.google.gson.JsonParser;
import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.annotations.OnApplicationStart;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

//@OnApplicationStart
public class DailyOrderJob extends Job {

    final String EMAIL = "raymond.chang@lendup.com";
    final String PASSWORD = "ilovechickentostadasalad";

    @Override
    public void doJob(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String cookie = getLoginCookie(EMAIL, PASSWORD);

        getOrderId(cookie);
    }

    private Long getOrderId(String cookie) {

        System.out.println(cookie);

        Client client = ClientBuilder.newClient();
        JsonParser jsonParser = new JsonParser();

        Response getResponse = client
            .target("https://www.eatclub.com")
            .path("/menus")
            .queryParam("categorized_menu", true)
            .queryParam("day", 1)
            .queryParam("menu_type", "individual")
            .request(MediaType.APPLICATION_JSON)
            .header("XSRF-TOKEN", "93sdfhj45832ihgdfjk24t")
            .header("cookie", cookie)
            .get();

        System.out.println(getResponse.getHeaders());

        Form form = new Form();
        form.param("date", "2018-05-09");

        Response response = client
            .target("https://www.eatclub.com")
            .path("/foodcourt/order")
            .request(MediaType.APPLICATION_JSON)
            .header("cookie", cookie)
            .header("XSRF-TOKEN", "93sdfhj45832ihgdfjk24t")
            .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        System.out.println(response.getStatusInfo());
        System.out.println(response.getMetadata());

        return 1L;
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
            .header("XSRF-TOKEN", "93sdfhj45832ihgdfjk24t")
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
}
