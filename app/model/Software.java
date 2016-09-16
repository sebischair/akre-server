package model;

import org.mongodb.morphia.annotations.Embedded;

/**
 * Created by mahabaleshwar on 8/29/2016.
 */
@Embedded
public class Software {
    private String title;
    private String description;
    private double score;
    private String uri;

    public Software() { }

    public Software(String uri, String title, String description, double score) {
        this.title = title;
        this.description = description;
        this.score = score;
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
