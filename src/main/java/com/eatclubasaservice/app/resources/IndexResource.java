package com.eatclubasaservice.app.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.codahale.metrics.annotation.Timed;
import com.eatclubasaservice.app.api.MealList;
import com.google.common.collect.Lists;


@Path("/index")
@Produces(MediaType.APPLICATION_JSON)
public class IndexResource {

    public IndexResource() {

    }

    @GET
    @Timed
    public MealList getAllKnownMeals() {
        // Read all known meals from the db
        // instantiate and send back a list

        return new MealList(Lists.newArrayList());
    }

    @POST
    @Timed
    public void setPreferenceList() {

    }

    @DELETE
    @Timed
    public void unsubscribe() {

    }
}