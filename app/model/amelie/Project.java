package model.amelie;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import db.AmelieMongoClient;
import org.bson.Document;
import org.bson.types.ObjectId;
import play.libs.Json;
import util.StaticFunctions;

/**
 * Created by Manoj on 11/22/2016.
 */
public class Project {
    private MongoCollection<Document> projectCollection;
    private MongoCollection<Document> issueCollection;

    public Project() {
        projectCollection = AmelieMongoClient.amelieDatabase.getCollection("projects");
        issueCollection = AmelieMongoClient.amelieDatabase.getCollection("issues");
    }

    public ObjectNode findByKey(String key) {
        return getProjectDetails(projectCollection.find(new BasicDBObject("key", key)).first());
    }

    public ObjectNode findById(String id) {
        return getProjectDetails(projectCollection.find(new BasicDBObject("_id", new ObjectId(id))).first());
    }

    public ArrayNode findAll() {
        ArrayNode projects = Json.newArray();
        MongoCursor<Document> cursor = projectCollection.find().iterator();
        while(cursor.hasNext()) {
            projects.add(getProjectDetails(cursor.next()));
        }
        return projects;
    }

    private ObjectNode getProjectDetails(Document obj) {
        ObjectNode project = Json.newObject();
        project.put("name", obj.getString("name"));
        String description = obj.getString("description") != null ? obj.getString("description") : "";
        project.put("description", description);
        if(description != null) { project.put("shortDescription", StaticFunctions.truncate(description)); }
        else { project.put("shortDescription", ""); }
        project.put("self", obj.getString("self"));
        String key = obj.getString("key");
        project.put("key", key);
        String projectCategory = obj.getString("projectCategory") != null ? obj.getString("projectCategory") : "";
        project.put("projectCategory", projectCategory);
        project.set("concepts", Json.toJson(obj.get("concepts")));
        project.put("preProcessed", obj.getBoolean("preProcessed"));
        if(!obj.containsKey("preProcessed") && !obj.containsKey("issueCount") || !obj.containsKey("decisionCount")) {
            int issueCount = getIssueCount(obj.getString("key"));
            int decisionCount = getDecisionCount(obj.getString("key"));

            project.put("issuesCount", issueCount);
            project.put("decisionCount", decisionCount);

            BasicDBObject newValue = new BasicDBObject();
            newValue.append("$set", new BasicDBObject().append("issueCount", issueCount));
            updateProjectByKey(key, newValue);

            newValue = new BasicDBObject();
            newValue.append("$set", new BasicDBObject().append("decisionCount", decisionCount));
            updateProjectByKey(key, newValue);

            if(issueCount > 0 || decisionCount > 0) {
                newValue = new BasicDBObject();
                newValue.append("$set", new BasicDBObject().append("preProcessed", true));
                updateProjectByKey(key, newValue);
            } else {
                newValue = new BasicDBObject();
                newValue.append("$set", new BasicDBObject().append("preProcessed", false));
                updateProjectByKey(key, newValue);
            }
        } else {
            project.put("issuesCount", obj.getInteger("issueCount"));
            project.put("decisionCount", obj.getInteger("decisionCount"));
        }

        return project;
    }

    private int getIssueCount(String projectKey) {
        return (int) issueCollection.count(new BasicDBObject("fields.project.key", projectKey));
    }

    private int getDecisionCount(String projectKey) {
        return (int) issueCollection.count(new BasicDBObject("fields.project.key", projectKey).append("amelie.designDecision", true));
    }

    public void updateProjectByKey(String key, BasicDBObject newConcepts) {
        projectCollection.updateOne(new BasicDBObject().append("key", key), newConcepts);
    }

    public void save(Document document) {
        projectCollection.insertOne(document);
    }
}
