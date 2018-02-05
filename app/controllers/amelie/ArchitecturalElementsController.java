package controllers.amelie;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import controllers.DocumentController;
import model.amelie.Issue;
import model.amelie.Keyword;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.HtmlUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
            System.out.println(issue.get("name").asText());
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
                System.out.println(conceptList);
                BasicDBObject newConcepts = new BasicDBObject();
                newConcepts.append("$set", new BasicDBObject().append("amelie.concepts", conceptList));
                issueModel.updateIssueByKey(issue.get("name").asText(), newConcepts);
            }
        });

        Keyword keywordModel = new Keyword();
        issues.forEach(issue -> {
            String text = (issue.get("summary").asText("") + " " + issue.get("description").asText("")).toLowerCase().replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("http.*?\\s", " ").replaceAll("\\*", "");
            text = text.replaceAll("[^a-zA-Z0-9\\s]", " ").replaceAll("^\\w{1,10}\\b", " ").replaceAll("\\r\\n|\\r|\\n", " ").replaceAll("\\$", "");
            List<String> keywordList = getKeywordsList(text, keywordModel.getAllKeywords());
            if(keywordList.size() > 0) {
                BasicDBObject newConcepts = new BasicDBObject();
                newConcepts.append("$set", new BasicDBObject().append("amelie.keywords", keywordList));
                issueModel.updateIssueByKey(issue.get("name").asText(), newConcepts);
            }
        });

        Logger.debug("AEs for tasks have been updated");
        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("statusCode", "200");
        return ok(result);
    }

    private List<String> getKeywordsList(String text, List<String> keys) {
        List<String> tokens = new ArrayList<>();
        keys.forEach(key -> {
            String k = key.toLowerCase();
            if(text.contains(k) && !tokens.contains(k)) tokens.add(k);
        });
        return tokens;
    }

    private List<String> getConceptsList(String text) {
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
}
