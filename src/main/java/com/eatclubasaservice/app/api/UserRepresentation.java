package com.eatclubasaservice.app.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserRepresentation {

    private String email;

    private String password;

    public UserRepresentation(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @JsonProperty
    public String getEmail() {
        return email;
    }

    @JsonProperty
    public String getPassword() {
        return password;
    }
}
