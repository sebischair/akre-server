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

import java.util.ArrayList;
import java.util.List;

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
            issues.add(getIssueDetails(cursor.next()));
        }
        return issues;
    }

    public ArrayNode findAllDesignDecisionsInAProject(String projectName) {
        ArrayNode issues = Json.newArray();
        MongoCursor<Document> cursor = issueCollection.find(new BasicDBObject("designDecision", true).append("belongsTo", projectName)).iterator();
        while(cursor.hasNext()) {
            issues.add(getIssueDetails(cursor.next()));
        }
        return issues;
    }

    private ObjectNode getIssueDetails(Document obj) {
        ObjectNode issue = Json.newObject();
        issue.put("id", obj.getObjectId("_id").toHexString());
        issue.put("name", obj.getString("name"));
        issue.put("belongsTo", obj.getString("belongsTo"));
        issue.put("summary", obj.getString("summary"));
        issue.put("description", obj.getString("description"));
        issue.put("designDecision", obj.getBoolean("designDecision"));
        issue.put("decisionCategory", obj.getString("decisionCategory"));
        issue.set("concepts", StaticFunctions.getArrayNodeFromJsonNode(obj, "concepts"));
        issue.set("keywords", StaticFunctions.getArrayNodeFromJsonNode(obj, "keywords"));
        issue.set("qualityAttributes", StaticFunctions.getArrayNodeFromJsonNode(obj, "qualityAttributes"));
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
        return issue;
    }

    public ArrayNode getDesignDecisionsForAEView(String projectName) {
        ArrayNode issues = Json.newArray();
        MongoCursor<Document> cursor = issueCollection.find(new BasicDBObject("designDecision", true).append("belongsTo", projectName)).iterator();
        while(cursor.hasNext()) {
            Document obj = cursor.next();
            ObjectNode issue = Json.newObject();
            issue.put("name", obj.getString("name"));
            issue.set("concepts", StaticFunctions.getArrayNodeFromJsonNode(obj, "concepts"));
            issue.put("resolved", obj.getString("resolved"));
            issues.add(issue);
        }
        return issues;
    }

    public ArrayNode getDesignDecisionsForQAView(String projectName) {
        ArrayNode issues = Json.newArray();
        MongoCursor<Document> cursor = issueCollection.find(new BasicDBObject("designDecision", true).append("belongsTo", projectName)).iterator();
        while(cursor.hasNext()) {
            Document obj = cursor.next();
            ObjectNode issue = Json.newObject();
            issue.put("name", obj.getString("name"));
            issue.put("decisionCategory", obj.getString("decisionCategory"));
            issue.set("qualityAttributes", StaticFunctions.getArrayNodeFromJsonNode(obj, "qualityAttributes"));
            issue.put("resolved", obj.getString("resolved"));
            issues.add(issue);
        }
        return issues;
    }

    public ArrayNode getDesignDecisionsRelatedToQA(String projectName, String qualityAttribute, String decisionCategory) {
        ArrayNode issues = Json.newArray();
        String[] qaArray = new String[1];
        qaArray[0] = qualityAttribute;
        BasicDBObject whereQuery = new BasicDBObject("designDecision", true)
                .append("belongsTo", projectName)
                .append("decisionCategory", decisionCategory)
                .append("qualityAttributes", new BasicDBObject("$in", qaArray));
        MongoCursor<Document> cursor = issueCollection.find(whereQuery).iterator();
        while(cursor.hasNext()) {
            issues.add(getIssueDetails(cursor.next()));
        }
        return issues;
    }

    public ArrayNode getDesignDecisionsRelatedToAE(String projectName, String conceptName) {
        ArrayNode issues = Json.newArray();
        BasicDBObject whereQuery = new BasicDBObject("designDecision", true)
                .append("belongsTo", projectName)
                .append("concepts", conceptName);
        MongoCursor<Document> cursor = issueCollection.find(whereQuery).iterator();
        while(cursor.hasNext()) {
            issues.add(getIssueDetails(cursor.next()));
        }
        return issues;
    }

    public void updateIssueById(String id, BasicDBObject newConcepts) {
        issueCollection.updateOne(new BasicDBObject().append("_id", new ObjectId(id)), newConcepts);
    }

    public List<String> getConceptList(String projectName) {
        List<String> conceptList = new ArrayList();
        MongoCursor<Document> cursor = issueCollection.find(new BasicDBObject("belongsTo", projectName).append("designDecision", true)).iterator();
        while(cursor.hasNext()) {
            Document obj = cursor.next();
            ArrayNode conceptsNode = StaticFunctions.getArrayNodeFromJsonNode(obj, "concepts");
            conceptsNode.forEach(node -> {
                if(!conceptList.contains(node.asText("").toLowerCase())) {
                    conceptList.add(node.asText("").toLowerCase());
                }
            });
        }
        return conceptList;
    }

    public ArrayNode getConceptsOfDesignDecisions(String projectName) {
        ArrayNode result = Json.newArray();
        MongoCursor<Document> cursor = issueCollection.find(new BasicDBObject("belongsTo", projectName).append("designDecision", true)).iterator();
        while(cursor.hasNext()) {
            Document obj = cursor.next();
            ObjectNode temp = Json.newObject();
            ArrayNode concepts = Json.newArray();
            concepts.addAll(StaticFunctions.getArrayNodeFromJsonNode(obj, "concepts"));
            concepts.addAll(StaticFunctions.getArrayNodeFromJsonNode(obj, "qualityAttributes"));
            temp.set("concepts", concepts);
            temp.put("assignee", obj.getString("assignee"));
            result.add(temp);
        }
        return result;
    }
}
