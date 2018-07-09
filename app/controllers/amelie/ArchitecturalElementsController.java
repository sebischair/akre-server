package controllers.amelie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import controllers.DocumentController;
import model.amelie.Issue;
import model.amelie.Keyword;
import org.bson.Document;
import play.Logger;
import play.Play;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.*;
import java.util.*;

/**
 * Created by Manoj on 8/8/2017.
 */
public class ArchitecturalElementsController extends Controller {
    @Inject
    private DocumentController docController;

    public Result updateTaskWithAE(String projectKey) {
        Logger.debug("request to update Tasks with AEs");

        Issue issueModel = new Issue();
        //ArrayNode issues = issueModel.findAllDesignDecisionsInAProject(projectKey);
        ArrayNode issues = issueModel.findAllIssuesInAProject(projectKey);
        issues.forEach(issue -> {
            String text = (issue.get("summary").asText("") + " " + issue.get("description").asText("")).toLowerCase().replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("http.*?\\s", " ").replaceAll("\\*", "");
            text = text.replaceAll("^\\w{1,20}\\b", " ").replaceAll("\\r\\n|\\r|\\n", " ").replaceAll("\\$", "");
            List<String> conceptList = getConceptsList(text);

            for (Iterator<String> iter = conceptList.listIterator(); iter.hasNext(); ) {
                String a = iter.next();
                if(a.equalsIgnoreCase("-")) {
                    iter.remove();
                }
            }

            if(conceptList.size() > 0) {
                BasicDBObject newConcepts = new BasicDBObject();
                newConcepts.append("$set", new BasicDBObject().append("amelie.concepts", conceptList));
                issueModel.updateIssueByKey(issue.get("name").asText(), newConcepts);
            }
        });

        issues.forEach(issue -> {
            String text = (issue.get("summary").asText("") + " " + issue.get("description").asText("")).toLowerCase().replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("http.*?\\s", " ").replaceAll("\\*", "");
            text = text.replaceAll("\\r\\n|\\r|\\n", " ").replaceAll("\\$", "");
            List<String> keywordList = getKeywordsList(text);
            if(keywordList.size() > 0) {
                BasicDBObject newConcepts = new BasicDBObject();
                newConcepts.append("$set", new BasicDBObject().append("amelie.concepts", keywordList));
                issueModel.updateIssueByKey(issue.get("name").asText(), newConcepts);
            }
        });

        Logger.debug("AEs for tasks have been updated");
        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("statusCode", "200");
        return ok(result);
    }

    public List<String> getKeywordsList(String text) {
        Keyword keywordModel = new Keyword();
        List<String> keys = keywordModel.getAllKeywords();
        List<String> tokens = new ArrayList<>();
        keys.forEach(key -> {
            String k = key.toLowerCase();
            if(text.contains(k) && !tokens.contains(k)) tokens.add(k);
        });
        return tokens;
    }

    public List<String> getConceptsList(String text) {
        List<String> tokens = new ArrayList<>();
        try{
            ArrayNode annotations = Json.newArray();
            docController.dbpediaDocAnnotations(annotations, text.toLowerCase());
            annotations.forEach(annotation -> {
                String conceptName = annotation.get("token").asText("").toLowerCase();
                if(!tokens.contains(conceptName)) {
                    tokens.add(conceptName);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tokens;
    }

    public Result fixTasksWithKeywords(String projectKey) {
        Logger.debug("request to update Tasks with keywords");
        Issue issueModel = new Issue();
        ArrayNode issues = issueModel.findAllIssuesInAProject(projectKey);
        Keyword keyword = new Keyword();
        ArrayList<String> allKeywords = keyword.getAllKeywords();
        allKeywords.addAll(keyword.getAllENKeywords());

        issues.forEach(issue -> {
            String text = (issue.get("summary").asText("") + issue.get("description").asText("")).toLowerCase();
            JsonNode existingKeys = issue.get("keywords");
            List keys = new ArrayList<String>();
            if(existingKeys != null && existingKeys.isArray()) {
                for(int i = 0; i< existingKeys.size(); i++) {
                    String k = existingKeys.get(i).asText();
                    if(!k.isEmpty() && !allKeywords.contains(k) && k.length() > 0) {
                        allKeywords.add(k);
                    }
                }
            }

            for(int i = 0; i< allKeywords.size(); i++) {
                String k = allKeywords.get(i);
                if(text.contains(k) && !k.isEmpty()) {
                    keys.add(k);
                }
            }

            if(keys.size() > 0) {
                System.out.println("Updating issue: " + issue.get("name").asText());
                BasicDBObject newConcepts = new BasicDBObject();
                newConcepts.append("$set", new BasicDBObject().append("amelie.keywords", keys));
                issueModel.updateIssueByKey(issue.get("name").asText(), newConcepts);
            }
        });

        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("statusCode", "200");
        return ok(result);
    }

    public Result fixTasks(String projectKey) {
        Logger.debug("request to update Tasks with AEs");

        Issue issueModel = new Issue();
        //ArrayNode issues = issueModel.findAllDesignDecisionsInAProject(projectKey);
        ArrayNode issues = issueModel.findAllIssuesInAProject(projectKey);

        issues.forEach(issue -> {
            if(issue.has("concepts") && issue.get("concepts") != null) {
                List<String> allConcepts = new ArrayList<>();
                if(issue.get("concepts").isArray()) {
                    ArrayNode concepts = (ArrayNode) issue.get("concepts");
                    concepts.forEach(concept -> {
                        if(concept.isArray()) {
                            concept.forEach(c -> {
                                if(!allConcepts.contains(c.asText(""))) {
                                    allConcepts.add(c.asText(""));
                                }
                            });
                        } else {
                            if(!allConcepts.contains(concept.asText(""))) {
                                allConcepts.add(concept.asText(""));
                            }
                        }
                    });

                    BasicDBObject newConcepts = new BasicDBObject();
                    newConcepts.append("$set", new BasicDBObject().append("amelie.concepts", allConcepts));
                    issueModel.updateIssueByKey(issue.get("name").asText(), newConcepts);
                }
            }
        });

        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("statusCode", "200");
        return ok(result);
    }

    public Result removeConceptsThatOccourOnlyOnce(String projectKey) {
        Issue issueModel = new Issue();
        ArrayNode issues = issueModel.findAllIssuesInAProject(projectKey);

        Map<String, Integer> cv = new HashMap<>();
        List<String> conceptList = new ArrayList();
        issues.forEach(issue -> {
            JsonNode concepts = issue.get("concepts");
            if(concepts != null && concepts.isArray()) {
                concepts.forEach(concept -> {
                    String key = concept.asText("").toLowerCase();
                    if(!cv.containsKey(key)) {
                        conceptList.add(key);
                        cv.put(key, 1);
                    } else {
                        Integer value = cv.get(key);
                        cv.replace(key, value+1);
                    }
                });
            }
        });
        MongoCollection<Document> issueCollection = issueModel.getIssueCollection();
        cv.forEach((k, v) -> {
            if(v == 3) {
                try{
                    System.out.println(k);
                    BasicDBObject searchObject = new BasicDBObject();
                    searchObject.append("amelie.concepts", k);

                    BasicDBObject newConcepts = new BasicDBObject();
                    newConcepts.append("$pull", new BasicDBObject().append("amelie.concepts", k));
                    issueCollection.updateMany(searchObject, newConcepts);
                } catch (Exception e) {
                    Logger.error("Not an array: " + k);
                }
            }
        });

        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("statusCode", "200");
        return ok(result);
    }

    public Result updateKeywordList() {
        Keyword keyword = new Keyword();
        ArrayList<String> allKeywords = keyword.getAllKeywords();
        try {
            FileInputStream fs = new FileInputStream(new File(Play.application().path().getAbsolutePath() + "/upload/customList.json"));
            JsonNode jsonArray = Json.parse(fs);
            jsonArray.forEach(jsonObject -> {
                String topicName = jsonObject.get("topic").asText("").toLowerCase();
                if(!allKeywords.contains(topicName)) {
                    System.out.println("Add topic");
                    keyword.addKeyWord(topicName);
                    allKeywords.add(topicName);
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("statusCode", "200");
        return ok(result);
    }

}
