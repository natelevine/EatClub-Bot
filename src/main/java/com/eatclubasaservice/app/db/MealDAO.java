package com.eatclubasaservice.app.db;

import com.eatclubasaservice.app.core.Meal;
import com.google.common.collect.Sets;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;


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

    public Set<Meal> fromIdSet(Set<Long> mealIds) {
        Query<Meal> mealsQuery = query("FROM Meal m WHERE m.id IN :mealIds");
        List<Meal> meals = mealsQuery.setParameter("mealIds", mealIds).list();
        return meals.isEmpty() ? Sets.newHashSet() : Sets.newHashSet(meals);
    }
}
