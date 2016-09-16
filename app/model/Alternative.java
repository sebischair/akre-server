package model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import play.libs.Json;
import util.StaticFunctions;

import java.util.List;

/**
 * Created by mahabaleshwar on 8/31/2016.
 */
@Entity("alternatives")
public class Alternative {
    @Id
    private ObjectId id;

    @Indexed
    private String conceptName;

    @Embedded
    private List<Software> software;

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public List<Software> getSoftware() {
        return software;
    }

    public void setSoftware(List<Software> software) {
        this.software = software;
    }

    public void save() {
        MorphiaObject.datastore.save(this);
    }

    public Alternative findByName(String conceptName) {
        List<? extends Alternative> alternatives = MorphiaObject.datastore.createQuery(this.getClass()).field("conceptName").equalIgnoreCase(conceptName).asList();
        if(alternatives.size() > 0) return alternatives.get(0);
        return null;
    }

    public ArrayNode getAlternativesAsJsonArray() {
        ArrayNode jsonArray = Json.newArray();
        for (Software software : getSoftware()) {
            ObjectNode jsonObject = Json.newObject();
            jsonObject.put(StaticFunctions.URI, software.getUri());
            jsonObject.put(StaticFunctions.TITLE, software.getTitle());
            jsonObject.put(StaticFunctions.DESCRIPTION, software.getDescription());
            jsonObject.put(StaticFunctions.SCORE, software.getScore());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public boolean updateAlternativeScore(String conceptName, String softwareName, String description, double score) {
        try {
            softwareName = softwareName.replaceAll("\\(", "\\\\(").replaceAll("\\)","\\\\)").replaceAll("\\+", "\\\\+");
            Query<Alternative> query = (Query<Alternative>) MorphiaObject.datastore.createQuery(this.getClass()).field("conceptName").equalIgnoreCase(conceptName).field("software.title").equalIgnoreCase(softwareName);
            UpdateOperations<Alternative> ops = (UpdateOperations<Alternative>) MorphiaObject.datastore.createUpdateOperations(this.getClass()).set("software.$.score", score).set("software.$.description", description);
            MorphiaObject.datastore.update(query, ops);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasSoftware(String conceptName, String softwareName) {
        List<? extends Alternative> genres = MorphiaObject.datastore.createQuery(this.getClass()).field("conceptName").equalIgnoreCase(conceptName).field("software.title").equalIgnoreCase(softwareName).asList();
        if(genres.size() > 0) return true;
        return false;
    }

    public boolean addSoftware(String conceptName, Software software) {
        try {
            Query<Alternative> query = (Query<Alternative>) MorphiaObject.datastore.createQuery(this.getClass()).field("conceptName").equalIgnoreCase(conceptName);
            UpdateOperations<Alternative> ops = (UpdateOperations<Alternative>) MorphiaObject.datastore.createUpdateOperations(this.getClass()).add("software", software, false);
            MorphiaObject.datastore.update(query, ops);
            return true;
        } catch(Exception e) {
            e.printStackTrace();;
        }
        return false;
    }
}
