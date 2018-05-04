package com.eatclubasaservice.app.db;


import com.eatclubasaservice.app.core.User;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class UserDAO extends AbstractDAO<User> {

    public UserDAO(SessionFactory factory) {
        super(factory);
    }

    public User findById(Long id) {
        return get(id);
    }

    public Optional<User> findByEmail(String email) {
        Query<User> emailQuery = query("FROM User u WHERE u.email = :email");
        List<User> users = emailQuery.setParameter("email", email).list();
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));

    }

    public long create(User user) {
        return persist(user).getId();
    }

    public void delete(User user) {
        this.currentSession().delete(user);
    }

//    public List<User> findAllWithPreferences() {
//        return list(namedQuery("com.eatclubasaservice.app.core.User.findAllWithPreferences"));
//    }

}
