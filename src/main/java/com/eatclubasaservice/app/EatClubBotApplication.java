package com.eatclubasaservice.app;

import com.eatclubasaservice.app.core.Meal;
import com.eatclubasaservice.app.core.Preference;
import com.eatclubasaservice.app.core.User;
import com.eatclubasaservice.app.db.MealDAO;
import com.eatclubasaservice.app.db.PreferenceDAO;
import com.eatclubasaservice.app.db.UserDAO;
import com.eatclubasaservice.app.resources.IndexResource;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class EatClubBotApplication extends Application<EatClubBotConfiguration> {

    private final HibernateBundle<EatClubBotConfiguration> hibernate = new HibernateBundle<EatClubBotConfiguration>(User.class, Preference.class, Meal.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(EatClubBotConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    public static void main(final String[] args) throws Exception {
        new EatClubBotApplication().run(args);
    }


    @Override
    public String getName() {
        return "EatClubBot";
    }

    @Override
    public void initialize(final Bootstrap<EatClubBotConfiguration> bootstrap) {
        bootstrap.addBundle(hibernate);
    }

    @Override
    public void run(final EatClubBotConfiguration configuration,
                    final Environment environment) {

        // DAOs
        final UserDAO userDAO = new UserDAO(hibernate.getSessionFactory());
        final PreferenceDAO preferenceDAO = new PreferenceDAO(hibernate.getSessionFactory());
        final MealDAO mealDAO = new MealDAO(hibernate.getSessionFactory());

        // Resources
        final IndexResource indexResource = new IndexResource(userDAO, preferenceDAO, mealDAO);

        // Registration
        environment.jersey().register(indexResource);
    }

}
