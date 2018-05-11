package com.eatclubasaservice.app.jobs;

import com.eatclubasaservice.app.EatClubBotApplication;
import com.eatclubasaservice.app.Services.EatClubAPIService;
import com.eatclubasaservice.app.Utils.EatClubResponseUtils;
import com.eatclubasaservice.app.core.Meal;
import com.eatclubasaservice.app.core.User;
import com.eatclubasaservice.app.db.UserDAO;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.annotations.On;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.NewCookie;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// Should be using system time (UTC)
@On("1 45 20 ? * *")
public class DailyOrderJob extends Job {

    final static Logger LOGGER = LoggerFactory.getLogger(DailyOrderJob.class);

    final EatClubAPIService eatClubAPIService;

    public DailyOrderJob(EatClubAPIService eatClubAPIService) {
        this.eatClubAPIService = eatClubAPIService;
    }

    @Override
    public void doJob(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        for (User user : getUsers()) {

            LOGGER.info("Starting ordering process for user: %s", user.getEmail());
            Map<String, NewCookie> cookies = eatClubAPIService.login(user.getEmail(), user.getPassword());
            Optional<JsonArray> existingOrders = eatClubAPIService.getUsersExistingOrders(cookies);

            Set<Long> existingOrderIds = Sets.newHashSet();
            if (existingOrders.isPresent()) {
                existingOrderIds = EatClubResponseUtils.parseOrderIdsFromFutureOrderArray(existingOrders.get());
                LOGGER.info("User has %d existing orders", existingOrderIds.size());
            }

            // shortcut, no need to order
            if (existingOrderIds.size() == 5) {
                LOGGER.info("User already has 5 orders, skipping ordering for today.");
                continue;
            }

            String cookieString = EatClubResponseUtils.getCookieStringFromMap(cookies);
            // TODO: these are not actually today's menu items, they're for slot 5
            Optional<JsonObject> todaysMenuItems = eatClubAPIService.getDailyMenuItems(5, cookieString);

            // If slot 5 is a holiday, weekend, or no LendUp meal available
            if (!todaysMenuItems.isPresent()) {
                LOGGER.info("No LendUp meal available for today, exiting job.");
                return;
            }

            Set<String> existingOrderDateStrings = EatClubResponseUtils.parseOrderDatesFromFutureOrdersArray(existingOrders.get());
            Set<Meal> todaysMeals = EatClubResponseUtils.parseDailyMeals(todaysMenuItems.get());

            JsonObject mealDaysMap = eatClubAPIService.getAvailableMealDays(cookies);
            Set<String> stringDaysSet = mealDaysMap.keySet();
            // there should only be 5 at a time
            List<String> sortedMealDateStrings = Lists.newArrayList(stringDaysSet);
            Collections.sort(sortedMealDateStrings);

            for (String mealDateString : sortedMealDateStrings) {
                LOGGER.info("Available meal date: %s", mealDateString);
            }
            if (existingOrderDateStrings.contains(sortedMealDateStrings.get(4))) {
                // user already has an order for this date
                LOGGER.info("User already has an order for slot 5, skipping.");
                continue;
            }

            Set<Meal> existingOrderMeals = Sets.newHashSet();
            for (Long id : existingOrderIds) {
                // we only care about the id for equality
                existingOrderMeals.add(new Meal(id, "dummyname", "dummyUrl"));
            }

            Optional<Meal> mealToOrder = EatClubResponseUtils.getMostSuitableMeal(user.getMealPreferences(), existingOrderMeals, todaysMeals);
            if (!mealToOrder.isPresent()) {
                // TODO: Notify the user here... we're not ordering
                LOGGER.info("No suitable meal to order, skipping");
                continue;
            }
            LOGGER.info("Found suitable meal to order: %d", mealToOrder.get().getId());
            // Get Order Id for Cart
            // these are hardcoded to always be for slot 5 (4 in the zero indexed sorted list)
            Long orderId = eatClubAPIService.getOrderIdForDate(LocalDate.parse(sortedMealDateStrings.get(4)), cookies, 5);
            eatClubAPIService.putOrderIntoCart(orderId, mealToOrder.get().getId(), cookies, 5);
            eatClubAPIService.checkout(cookies, 5);
            LOGGER.info("Successfully placed order and checked out!");
        }

    }

    private List<User> getUsers() {
        SessionFactory sessionFactory = EatClubBotApplication.getSessionFactory();
        UserDAO userDAO = new UserDAO(sessionFactory);

        Session session = sessionFactory.openSession();
        ManagedSessionContext.bind(session);
        Transaction transaction = session.beginTransaction();

        List<User> allUsers;
        try {
            allUsers = userDAO.findAll();
            transaction.commit();
            session.close();
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException(e);
        } finally {
            session.close();
            ManagedSessionContext.unbind(sessionFactory);
        }
        return allUsers;
    }

}
