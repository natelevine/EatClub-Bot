package com.eatclubasaservice.app.db;


import com.eatclubasaservice.app.core.User;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;

public class UserDAO extends AbstractDAO<User> {

    public UserDAO(SessionFactory factory) {
        super(factory);
    }

    public User findById(Long id) {
        return get(id);
    }

    public long create(User user) {
        return persist(user).getId();
    }

//    public List<User> findAllWithPreferences() {
//        return list(namedQuery("com.eatclubasaservice.app.core.User.findAllWithPreferences"));
//    }

}
