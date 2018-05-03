package com.eatclubasaservice.app.core;

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
//                @NamedQuery(
//                    name = "com.eatclubasaservice.app.core.User.findAllWithPreferences",
//                    query = " SELECT u " +
//                            "   FROM User u " +
//                            "   JOIN " +
//                            "  FETCH Preference p " +
//                            "  WHERE p.user_id = u.id "
//                )
              })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
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
        return this.mealPreferences;
    }

    public void setMealPreferences(List<Preference> mealPreferences) {
        this.mealPreferences = mealPreferences;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
