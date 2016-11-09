package model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import play.libs.Json;
import util.StaticFunctions;

import java.util.List;

/**
 * Created by mahabaleshwar on 8/29/2016.
 */
@Entity("genres")
@Indexes(@Index(value = "genreName", fields = @Field("genreName")))
public class Genre {
    @Id
    private ObjectId id;

    @Indexed
    private String genreName;

    @Embedded
    private List<Software> software;

    public List<Software> getSoftware() {
        return software;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setSoftware(List<Software> software) {
        this.software = software;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public void save() {
        MorphiaObject.datastore.save(this);
    }

    public Genre findByName(String name) {
        name = name.replaceAll("\\(", "\\\\(").replaceAll("\\)","\\\\)").replaceAll("\\+", "\\\\+");
        List<? extends Genre> genres = MorphiaObject.datastore.createQuery(this.getClass()).field("genreName").equalIgnoreCase(name).asList();
        if(genres.size() > 0) return genres.get(0);
        return null;
    }

    public ArrayNode getSoftwareAsJsonArray() {
        ArrayNode jsonArray = Json.newArray();
        if(getSoftware() != null) {
            for (Software software : getSoftware()) {
                if(software.getScore() >= 0) {
                    ObjectNode jsonObject = Json.newObject();
                    jsonObject.put(StaticFunctions.URI, software.getUri());
                    jsonObject.put(StaticFunctions.TITLE, software.getTitle());
                    jsonObject.put(StaticFunctions.DESCRIPTION, software.getDescription());
                    jsonObject.put(StaticFunctions.SCORE, software.getScore());
                    jsonArray.add(jsonObject);
                }
            }
        }
        return jsonArray;
    }

    public boolean updateSoftwareScore(String genreName, String softwareName, String description, double score) {
        try {
            softwareName = softwareName.replaceAll("\\(", "\\\\(").replaceAll("\\)","\\\\)").replaceAll("\\+", "\\\\+");
            Query<Genre> query = (Query<Genre>) MorphiaObject.datastore.createQuery(this.getClass()).field("genreName").equalIgnoreCase(genreName).field("software.title").equalIgnoreCase(softwareName);
            UpdateOperations<Genre> ops = (UpdateOperations<Genre>) MorphiaObject.datastore.createUpdateOperations(this.getClass()).set("software.$.score", score).set("software.$.description", description);
            MorphiaObject.datastore.update(query, ops);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasSoftware(String genreName, String softwareName) {
        List<? extends Genre> genres = MorphiaObject.datastore.createQuery(this.getClass()).field("genreName").equalIgnoreCase(genreName).field("software.title").equalIgnoreCase(softwareName).asList();
        if(genres.size() > 0) return true;
        return false;
    }

    public boolean addSoftware(String genreName, Software software) {
        try {
            Query<Genre> query = (Query<Genre>) MorphiaObject.datastore.createQuery(this.getClass()).field("genreName").equalIgnoreCase(genreName);
            UpdateOperations<Genre> ops = (UpdateOperations<Genre>) MorphiaObject.datastore.createUpdateOperations(this.getClass()).add("software", software, false);
            MorphiaObject.datastore.update(query, ops);
            return true;
        } catch(Exception e) {
            e.printStackTrace();;
        }
        return false;
    }
}