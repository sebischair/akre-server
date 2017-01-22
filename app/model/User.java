package model;

import org.mongodb.morphia.annotations.Embedded;

/**
 * Created by mahabaleshwar on 1/22/2017.
 */
@Embedded
public class User {
    private String name;
    private String mail;
    public User() {}
    public User(String name, String mail) {
        this.name = name;
        this.mail = mail;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}
