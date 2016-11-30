package model;

import org.mongodb.morphia.annotations.Embedded;

/**
 * Created by mahabaleshwar on 11/30/2016.
 */
@Embedded
public class Regex {
    private String name;
    private String description;
    private String regex;
    private String progLanguage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getProgLanguage() {
        return progLanguage;
    }

    public void setProgLanguage(String progLanguage) {
        this.progLanguage = progLanguage;
    }

    public Regex() { }

    public Regex(String name, String description, String regex, String progLanguage) {
        this.name = name;
        this.description = description;
        this.regex = regex;
        this.progLanguage = progLanguage;
    }
}
