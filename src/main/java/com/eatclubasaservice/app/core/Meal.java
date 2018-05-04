package com.eatclubasaservice.app.core;

import com.eatclubasaservice.app.api.MealRepresentation;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "meals")
@NamedQueries({
        @NamedQuery(
                name = "com.eatclubasaservice.app.core.Meal.findAll",
                query = " SELECT m FROM Meal m "
        )
})
public class Meal {

    @Id
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    public Meal() {

    }

    public Meal(long id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public MealRepresentation getMealRepresentation() {
        return new MealRepresentation(this.id, this.name, this.imageUrl);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!Meal.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final Meal other = (Meal) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.id).toHashCode();
    }
}
