package model.amelie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import db.AmelieMongoClient;
import org.bson.Document;
import org.bson.types.ObjectId;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

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

    public Document findByName(String name) {
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("name", name);
        return projectCollection.find(whereQuery).first();
    }

    public Document findById(String id) {
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("_id", new ObjectId(id));
        return projectCollection.find(whereQuery).first();
    }

    public ArrayNode findAll() {
        ArrayNode projects = Json.newArray();
        MongoCursor<Document> cursor = projectCollection.find().iterator();
        while(cursor.hasNext()) {
            Document obj = cursor.next();
            ObjectNode project = Json.newObject();
            project.put("id", obj.getObjectId("_id").toHexString());
            project.put("name", obj.getString("name"));
            project.put("description", obj.getString("description"));
            project.put("self", obj.getString("self"));
            project.put("key", obj.getString("key"));
            project.put("projectCategory", obj.getString("projectCategory"));
            project.set("concepts", Json.toJson(obj.get("concepts")));
            project.put("issueCount", getIssueCount(obj.getString("name")));
            project.put("decisionCount", getDecisionCount(obj.getString("name")));
            projects.add(project);
        }
        return projects;
    }

    private long getIssueCount(String projectName) {
        return issueCollection.count(new BasicDBObject("belongsTo", projectName));
    }

    private long getDecisionCount(String projectName) {
        return issueCollection.count(new BasicDBObject("belongsTo", projectName).append("designDecision", true));
    }

    public void save(Document document) {
        projectCollection.insertOne(document);
    }
}
