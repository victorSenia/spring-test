package com.leo.test.luxoft.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;

/**
 * @author Senchenko Victor
 */
public class Comment implements Serializable {
    @JsonIgnore
    private transient int movieId;

    private String username;

    @NotEmpty
    private String message;

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
