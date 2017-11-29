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
import util.StaticFunctions;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
        MongoCursor<Document> cursor = issueCollection.find(new BasicDBObject("amelie.designDecision", true)).iterator();
        while(cursor.hasNext()) {
            issues.add(getIssueDetails(Json.toJson(cursor.next())));
        }
        return issues;
    }

    public ArrayNode findAllDesignDecisionsInAProject(String projectKey) {
        ArrayNode issues = Json.newArray();
        MongoCursor<Document> cursor = issueCollection.find(new BasicDBObject("fields.project.key", projectKey).append("amelie.designDecision", true)).iterator();
        while(cursor.hasNext()) {
            issues.add(getIssueDetails(Json.toJson(cursor.next())));
        }
        return issues;
    }

    private ObjectNode getIssueDetails(JsonNode obj) {
        ObjectNode issue = Json.newObject();
        issue.put("name", obj.get("name"));
        if(obj.has("fields")) {
            JsonNode fields = obj.get("fields");
            issue.put("summary", fields.get("summary").asText(""));
            issue.put("description", fields.get("description").asText(""));
            issue.put("created", fields.get("created").asText(""));
            issue.put("resolved", fields.get("resolutiondate").asText(""));

            if(fields.has("project"))
                issue.put("belongsTo", fields.get("project").get("key").asText(""));
            if(fields.has("issuetype"))
                issue.put("issueType", fields.get("issuetype").get("name").asText(""));
            if(fields.has("status"))
                issue.put("status", fields.get("status").get("name").asText(""));
            if(fields.has("resolution"))
                issue.put("resolution", fields.get("resolution").get("name").asText(""));
            if(fields.has("priority"))
                issue.put("priority", fields.get("priority").get("name").asText(""));
            if(fields.has("assignee") && fields.get("assignee").has("name"))
                issue.put("assignee", fields.get("assignee").get("name").asText(""));
            if(fields.has("reporter"))
                issue.put("reporter", fields.get("reporter").get("name").asText(""));
        }
        if(obj.has("amelie")) {
            JsonNode amelie = obj.get("amelie");
            issue.put("designDecision", amelie.get("designDecision"));
            issue.put("decisionCategory", amelie.get("decisionCategory"));
            issue.set("concepts", amelie.get("concepts"));
            issue.set("keywords", amelie.get("keywords"));
            issue.set("qualityAttributes", amelie.get("qualityAttributes"));
        }
        return issue;
    }

    public ArrayNode getDesignDecisionsForAEView(String projectKey) {
        ArrayNode issues = Json.newArray();
        MongoCursor<Document> cursor = issueCollection.find(new BasicDBObject("fields.project.key", projectKey).append("amelie.designDecision", true)).iterator();
        while(cursor.hasNext()) {
            JsonNode obj = Json.toJson(cursor.next());
            ObjectNode issue = Json.newObject();
            issue.put("name", obj.get("name").asText(""));
            if(obj.has("amelie"))
                issue.set("concepts", obj.get("amelie").get("concepts"));
            if(obj.has("fields"))
                issue.put("resolved", obj.get("fields").get("resolutiondate").asText(""));
            issues.add(issue);
        }
        return issues;
    }

    public ArrayNode getDesignDecisionsForQAView(String projectKey) {
        ArrayNode issues = Json.newArray();
        MongoCursor<Document> cursor = issueCollection.find(new BasicDBObject("fields.project.key", projectKey).append("amelie.designDecision", true)).iterator();
        while(cursor.hasNext()) {
            JsonNode obj = Json.toJson(cursor.next());
            ObjectNode issue = Json.newObject();
            issue.put("name", obj.get("name").asText(""));
            if(obj.has("amelie")) {
                JsonNode amelie = obj.get("amelie");
                issue.set("concepts", amelie.get("concepts"));
                issue.put("decisionCategory", amelie.get("decisionCategory"));
                issue.set("qualityAttributes", amelie.get("qualityAttributes"));
            }
            if(obj.has("fields"))
                issue.put("resolved", obj.get("fields").get("resolutiondate").asText(""));
            issues.add(issue);
        }
        return issues;
    }

    public ArrayNode getDesignDecisionsRelatedToQA(String projectKey, String qualityAttribute, String decisionCategory) {
        ArrayNode issues = Json.newArray();
        String[] qaArray = new String[1];
        qaArray[0] = qualityAttribute;
        BasicDBObject whereQuery = new BasicDBObject("amelie.designDecision", true)
                .append("fields.project.key", projectKey)
                .append("amelie.decisionCategory", decisionCategory)
                .append("amelie.qualityAttributes", new BasicDBObject("$in", qaArray));
        MongoCursor<Document> cursor = issueCollection.find(whereQuery).iterator();
        while(cursor.hasNext()) {
            issues.add(getIssueDetails(Json.toJson(cursor.next())));
        }
        return issues;
    }

    public ArrayNode getDesignDecisionsRelatedToAE(String projectKey, String conceptName) {
        ArrayNode issues = Json.newArray();
        BasicDBObject whereQuery = new BasicDBObject("designDecision", true)
                .append("fields.project.key", projectKey)
                .append("amelie.concepts", conceptName);
        MongoCursor<Document> cursor = issueCollection.find(whereQuery).iterator();
        while(cursor.hasNext()) {
            issues.add(getIssueDetails(Json.toJson(cursor.next())));
        }
        return issues;
    }

    public void updateIssueByKey(String key, BasicDBObject newConcepts) {
        issueCollection.updateOne(new BasicDBObject().append("name", key), newConcepts);
    }

    public List<ObjectNode> orderIssuesByResolutionDate(ArrayNode issues) {
        List<ObjectNode> jsonValues = new ArrayList<>();
        issues.forEach(issue -> jsonValues.add((ObjectNode) issue));

        Collections.sort(jsonValues, new Comparator<ObjectNode>() {
            private static final String KEY_NAME = "resolved";
            @Override
            public int compare(ObjectNode a, ObjectNode b) {
                String valA = a.get(KEY_NAME).asText("");
                String valB = b.get(KEY_NAME).asText("");

                if(valA != "" && valB != "") {
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    try {
                        Date dateA = format.parse(valA);
                        Date dateB = format.parse(valB);
                        return dateA.compareTo(dateB);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                return -1;
            }
        });
        return jsonValues;
    }
}
