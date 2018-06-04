package controllers.amelie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import model.amelie.Issue;
import model.amelie.QualityAttribute;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.StaticFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manoj on 8/8/2017.
 */
public class QualityAttributesController extends Controller {

    public Result updateTaskWithQA(String projectKey) {
        Logger.info("request to update Tasks with QAs");
        Issue issueModel = new Issue();
        ArrayNode issues = issueModel.findAllDesignDecisionsInAProject(projectKey);
        // ArrayNode issues = issueModel.findAllIssuesInAProject(projectKey);
        QualityAttribute qaModel = new QualityAttribute();
        ArrayNode qas = qaModel.getAllQAs();
        issues.forEach(issue -> {
            List<String> qaList = getQAList(StaticFunctions.cleanText(issue.get("summary").asText("") + " " + issue.get("description").asText("")), qas);
            if(qaList.size() > 0) {
                BasicDBObject newConcepts = new BasicDBObject();
                newConcepts.append("$set", new BasicDBObject().append("amelie.qualityAttributes", qaList));
                issueModel.updateIssueByKey(issue.get("name").asText(), newConcepts);
            }
        });

        Logger.info("QAs for tasks have been updated");
        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("statusCode", "200");
        return ok(result);
    }

    private List<String> getQAList(String text, ArrayNode qas) {
        List<String> result = new ArrayList<>();
        qas.forEach(qa -> {
            JsonNode keywords = qa.get("keywords");
            String qaName = qa.get("name").asText();
            keywords.forEach(keyword -> {
                if(text.toLowerCase().contains(keyword.asText())) {
                    if(!result.contains(qaName)) {
                        result.add(qaName);
                    }
                }
            });
        });
        return result;
    }
}