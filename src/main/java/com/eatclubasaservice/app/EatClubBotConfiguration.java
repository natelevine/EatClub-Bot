package com.eatclubasaservice.app;

import de.spinscale.dropwizard.jobs.JobConfiguration;
import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.DatabaseConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.*;

public class EatClubBotConfiguration extends Configuration implements JobConfiguration {

    @Valid
    @NotNull
    @JsonProperty("database")
    private DataSourceFactory database = new DataSourceFactory();

    public DataSourceFactory getDataSourceFactory() {

        try {
            DatabaseConfiguration databaseConfiguration = DBConfig.create(System.getenv("DATABASE_URL"));
            database = (DataSourceFactory) databaseConfiguration.getDataSourceFactory(null);
        } catch (IllegalArgumentException e) {
            // not in Heroku, so no env var
        } finally {
            return database;
        }
    }

}
