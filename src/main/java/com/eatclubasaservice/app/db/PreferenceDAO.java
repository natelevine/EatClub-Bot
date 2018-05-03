package com.eatclubasaservice.app.db;

import com.eatclubasaservice.app.core.Preference;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

public class PreferenceDAO extends AbstractDAO<Preference> {

    public PreferenceDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public long create(Preference preference) {
        return persist(preference).getId();
    }
}
