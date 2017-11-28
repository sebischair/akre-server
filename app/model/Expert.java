package model;

import db.DefaultMongoClient;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mahabaleshwar on 9/14/2016.
 */
@Entity("experts")
@Indexes(@Index(value = "name", fields = @Field("name")))
public class Expert {
    @Id
    private ObjectId _id;

    @Indexed
    private String name;

    private String dept;

    private String emailid;

    private String imageURL;

    private String initials;

    private List<String> expertise;

    public List<String> findAllExpertiseTokens() {
        List<Expert> experts = (List<Expert>) DefaultMongoClient.datastore.createQuery(this.getClass()).asList();
        Set<String> expertise = new HashSet<String>();
        for(Expert expert: experts) {
            expertise.addAll(expert.getExpertise());
        }
        return new ArrayList(expertise);
    }

    public List<String> getExpertise() {
        return expertise;
    }
}
