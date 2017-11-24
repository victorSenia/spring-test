package com.leo.test.luxoft.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;

/**
 * @author Senchenko Victor
 */
public class Movie implements Serializable {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int id;

    @NotEmpty
    private String title;

    @NotEmpty
    private String description;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private transient Comment[] comments;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Comment[] getComments() {
        return comments;
    }

    public void setComments(Comment[] comments) {
        this.comments = comments;
    }
}
