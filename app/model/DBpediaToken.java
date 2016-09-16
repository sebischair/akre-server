package model;

import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.List;

/**
 * Created by mahabaleshwar on 8/11/2016.
 */
@Entity("dbpediaTypes")
@Indexes(@Index(value = "name", fields = @Field("name")))
public class DBpediaToken {
    @Id
    private ObjectId id;

    @Indexed
    private String name;

    private String type;

    private int score;

    public DBpediaToken() {}

    public DBpediaToken(String name, String type, int score) {
        this.name = name;
        this.type = type;
        this.score = score;
    }

    public void save() {
        MorphiaObject.datastore.save(this);
    }

    public List<DBpediaToken> findAllCustomTokens() {
        return (List<DBpediaToken>) MorphiaObject.datastore.createQuery(this.getClass()).field("type").equalIgnoreCase("custom").field("score").greaterThanOrEq(0).asList();
    }

    public DBpediaToken findByName(String name) {
        List<? extends DBpediaToken> tokens = MorphiaObject.datastore.createQuery(this.getClass()).field("name").equalIgnoreCase(name).asList();
        if(tokens.size() > 0) return tokens.get(0);
        return null;
    }

    public boolean updateTokenScore(String tokenName, int value) {
        try {
            Query<DBpediaToken> query = (Query<DBpediaToken>) MorphiaObject.datastore.createQuery(this.getClass()).field("name").equalIgnoreCase(tokenName);
            UpdateOperations<DBpediaToken> ops = (UpdateOperations<DBpediaToken>) MorphiaObject.datastore.createUpdateOperations(this.getClass()).set("score", value);
            MorphiaObject.datastore.update(query, ops);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getScore() {
        return score;
    }
}