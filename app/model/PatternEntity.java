package model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import play.libs.Json;
import util.StaticFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mahabaleshwar on 11/30/2016.
 */
@Entity("patternEntities")
@Indexes(@Index(value = "id", fields = @Field("id")))
public class PatternEntity {
    @Id
    @Indexed
    private ObjectId id;

    private String projectId;

    @Embedded
    private List<Regex> regex;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public List<Regex> getRegex() {
        return regex;
    }

    public void setRegex(List<Regex> regex) {
        this.regex = regex;
    }

    public PatternEntity() {}

    public PatternEntity(String projectId, List<Regex> regex)  {
        this.projectId =projectId;
        this.regex = regex;
    }

    public Key<PatternEntity> save() {
        return MorphiaObject.datastore.save(this);
    }

    public PatternEntity findByProjectId(String projectId) {
        try {
            List<? extends PatternEntity> pattern = MorphiaObject.datastore.createQuery(this.getClass()).field(StaticFunctions.PROJECTID).equalIgnoreCase(projectId).asList();
            if (pattern.size() > 0) {
                return pattern.get(0);
            } else {
                createDefaultPattern(projectId);
                return findByProjectId(projectId);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createDefaultPattern(String projectId) {
        List<Regex> rl = new ArrayList<Regex>();
        rl.add(new Regex("Code Smell", "Hardcoded system name detected", "\\bcu[0-9]+[a-zA-Z_0-9]+\\b", "any", new ArrayList<String>()));
        rl.add(new Regex("Code Smell", "Hardcoded wait function detected", "wait\\([0-9]+\\)", "any", new ArrayList<String>()));
        new PatternEntity(projectId, rl).save();
    }

    public boolean updatePattern(String projectId, List<Regex> regex) {
        try {
            Query<PatternEntity> query = (Query<PatternEntity>) MorphiaObject.datastore.createQuery(this.getClass()).field(StaticFunctions.PROJECTID).equalIgnoreCase(projectId);
            UpdateOperations<PatternEntity> ops = (UpdateOperations<PatternEntity>) MorphiaObject.datastore.createUpdateOperations(this.getClass()).set("regex", regex);
            MorphiaObject.datastore.update(query, ops);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public ObjectNode getJson() {
        ObjectNode jsonObject = Json.newObject();
        jsonObject.put(StaticFunctions.PROJECTID, this.projectId);
        jsonObject.put(StaticFunctions.PATTERN, getRegexAsJsonArray());
        return jsonObject;
    }

    public ArrayNode getRegexAsJsonArray() {
        ArrayNode jsonArray = Json.newArray();
        if(getRegex() != null) {
            for (Regex software : getRegex()) {
                ObjectNode jsonObject = Json.newObject();
                jsonObject.put(StaticFunctions.NAME, software.getName());
                jsonObject.put(StaticFunctions.DESCRIPTION, software.getDescription());
                jsonObject.put(StaticFunctions.REGEX.toLowerCase(), software.getRegex());
                jsonObject.put(StaticFunctions.PROGLANGUAGE, software.getProgLanguage());
                jsonObject.put("tags", StaticFunctions.getJsonFromList(software.getTags()));
                jsonArray.add(jsonObject);
            }
        }
        return jsonArray;
    }
}
