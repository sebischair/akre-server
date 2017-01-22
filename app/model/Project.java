package model;

import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.annotations.*;

import java.util.List;

/**
 * Created by mahabaleshwar on 11/22/2016.
 */
@Entity("projects")
@Indexes(@Index(value = "name", fields = @Field("name")))
public class Project {
    @Id
    private ObjectId _id;

    @Indexed
    private String name;

    private String description;

    public Project() {}

    public Project(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Key<Project> save() {
        return MorphiaObject.datastore.save(this);
    }

    public Project findByName(String name) {
        List<? extends Project> projects = MorphiaObject.datastore.createQuery(this.getClass()).field("name").equalIgnoreCase(name).asList();
        if(projects.size() > 0) return projects.get(0);
        return null;
    }

    public Project findById(String id) {
        ObjectId objectId = new ObjectId(id);
        return MorphiaObject.datastore.get(this.getClass(), objectId);
    }

    public ObjectId get_id() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
