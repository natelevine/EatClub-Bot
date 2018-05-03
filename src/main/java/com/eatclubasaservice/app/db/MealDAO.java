package com.eatclubasaservice.app.db;

import com.eatclubasaservice.app.api.Meal;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;


public class MealDAO extends AbstractDAO<Meal> {

    public MealDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Meal findById(Long id) {
        return get(id);
    }

    public long create(Meal meal) {
        return persist(meal).getId();
    }

    public List<Meal> findAll() {
        return list(namedQuery("com.eatclubasaservice.app.core.Meal.findAll"));
    }

}
