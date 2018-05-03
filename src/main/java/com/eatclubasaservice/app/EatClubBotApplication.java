package com.eatclubasaservice.app;

import com.eatclubasaservice.app.resources.IndexResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class EatClubBotApplication extends Application<EatClubBotConfiguration> {

    public static void main(final String[] args) throws Exception {
        new EatClubBotApplication().run(args);
    }

    @Override
    public String getName() {
        return "EatClubBot";
    }

    @Override
    public void initialize(final Bootstrap<EatClubBotConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final EatClubBotConfiguration configuration,
                    final Environment environment) {

        final IndexResource indexResource = new IndexResource();
        environment.jersey().register(indexResource);
    }

}
