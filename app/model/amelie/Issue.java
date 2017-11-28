package model.amelie;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import db.AmelieMongoClient;
import org.bson.Document;
import play.libs.Json;

/**
 * Created by Manoj on 11/28/2017.
 */
public class Issue {
    private MongoCollection<Document> issueCollection;

    public Issue() {
        issueCollection = AmelieMongoClient.amelieDatabase.getCollection("issues");
    }

    public ArrayNode findAllDesignDecisions() {
        ArrayNode issues = Json.newArray();
        MongoCursor<Document> cursor = issueCollection.find(new BasicDBObject("designDecision", true)).iterator();
        while(cursor.hasNext()) {
            Document obj = cursor.next();
            ObjectNode issue = Json.newObject();
            issue.put("id", obj.getObjectId("_id").toHexString());
            issue.put("name", obj.getString("name"));
            issue.put("belongsTo", obj.getString("belongsTo"));
            issue.put("summary", obj.getString("summary"));
            issue.put("description", obj.getString("description"));
            issue.put("designDecision", obj.getBoolean("designDecision"));
            issue.put("decisionCategory", obj.getString("decisionCategory"));
            issue.set("concepts", Json.toJson(obj.get("concepts")));
            issue.set("keywords", Json.toJson(obj.get("keywords")));
            issue.set("qualityAttributes", Json.toJson(obj.get("qualityAttributes")));
            issue.put("issueType", obj.getString("issueType"));
            issue.put("issueLinks", obj.getString("issueLinks"));
            issue.put("status", obj.getString("status"));
            issue.put("resolution", obj.getString("resolution"));
            issue.put("priority", obj.getString("priority"));
            issue.put("assignee", obj.getString("assignee"));
            issue.put("reporter", obj.getString("reporter"));
            issue.put("created", obj.getString("created"));
            issue.put("resolved", obj.getString("resolved"));
            issue.put("updated", obj.getString("updated"));
            issues.add(issue);
        }
        return issues;
    }

    public ArrayNode getDesignDecisionsForAEView(String projectName) {
        ArrayNode issues = Json.newArray();
        MongoCursor<Document> cursor = issueCollection.find(new BasicDBObject("designDecision", true).append("belongsTo", projectName)).iterator();
        while(cursor.hasNext()) {
            Document obj = cursor.next();
            ObjectNode issue = Json.newObject();
            issue.put("name", obj.getString("name"));
            issue.set("concepts", Json.toJson(obj.get("concepts")));
            issue.put("resolved", obj.getString("resolved"));
            issues.add(issue);
        }
        return issues;
    }

}
