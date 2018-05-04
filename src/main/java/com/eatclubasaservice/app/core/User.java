package com.eatclubasaservice.app.core;

import com.eatclubasaservice.app.EatClubBotApplication;
import com.eatclubasaservice.app.db.PreferenceDAO;
import com.google.common.collect.Lists;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "users")
@NamedQueries({
                @NamedQuery(
                    name = "com.eatclubasaservice.app.core.User.findAll",
                    query = " SELECT u FROM User u "
                )
              })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @OneToMany(cascade = CascadeType.ALL, targetEntity = Preference.class, mappedBy = "user")
    @OrderBy("rank")
    List<Preference> mealPreferences;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Preference> getMealPreferences() {
        if (this.mealPreferences == null) {
            this.mealPreferences = Lists.newArrayList();
        }
        return this.mealPreferences;
    }

    public void setMealPreferences(List<Preference> mealPreferences) {
        this.mealPreferences = mealPreferences;
    }

    public void deleteAllPrefs() {
        PreferenceDAO preferenceDAO = new PreferenceDAO(EatClubBotApplication.getSessionFactory());
        for (Preference userPreference : this.mealPreferences) {
            preferenceDAO.delete(userPreference);
        }
    }

    public User() {

    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
