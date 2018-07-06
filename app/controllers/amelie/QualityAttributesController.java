package controllers.amelie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import model.amelie.Issue;
import model.amelie.QualityAttribute;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manoj on 8/8/2017.
 */
public class QualityAttributesController extends Controller {

    public Result updateTaskWithQA(String projectKey) {
        System.out.println("request to update Tasks with QAs");
        Issue issueModel = new Issue();
        //ArrayNode issues = issueModel.findAllDesignDecisionsInAProject(projectKey);
        ArrayNode issues = issueModel.findAllIssuesInAProject(projectKey);
        ArrayNode qas = new QualityAttribute().getAllQAs();
        issues.forEach(issue -> {
            List<String> qaList = getQAList(issue.get("summary").asText("") + " " + issue.get("description").asText(""), qas);
            if(qaList.size() > 0) {
                BasicDBObject newConcepts = new BasicDBObject();
                newConcepts.append("$set", new BasicDBObject().append("amelie.qualityAttributes", qaList));
                issueModel.updateIssueByKey(issue.get("name").asText(), newConcepts);
            }
        });

        System.out.println("QAs for tasks have been updated");
        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("statusCode", "200");
        return ok(result);
    }

    public List<String> getQAList(String text, ArrayNode qas) {
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

    public Result getAllQAs() {
        QualityAttribute qaModel = new QualityAttribute();
        ArrayNode qas = qaModel.getAllQAs();
        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("statusCode", "200");
        result.set("data", qas);
        return ok(result);
    }

    public Result getAllIssuesForQAs() {
        JsonNode request = request().body().asJson();
        ObjectNode result = Json.newObject();
        if(request!= null && request.has("project-key") && request.has("quality-attributes") && request.get("quality-attributes").isArray()) {
            ArrayNode qas = new QualityAttribute().getAllQAs();
            String projectKey = request.get("project-key").asText("");
            ArrayNode requestedQualityAttributes = (ArrayNode) request.get("quality-attributes");
            Issue issueModel = new Issue();
            ArrayNode issues = issueModel.findAllIssuesInAProjectWithQAs(projectKey);
            ArrayNode resultIssue = Json.newArray();

            for(int j=0; j<requestedQualityAttributes.size(); j++) {
                JsonNode requestedQualityAttribute = requestedQualityAttributes.get(j);
                for(int i=0; i<issues.size(); i++) {
                    JsonNode issue = issues.get(i);
                    boolean add = false;
                    if(issue.has("qualityAttributes")) {
                        if(requestedQualityAttribute.has("name")) {
                            ArrayNode qualityAttribute = (ArrayNode) issue.get("qualityAttributes");
                            for(int l=0; l<qualityAttribute.size(); l++) {
                                if(requestedQualityAttribute.get("name").asText("").equalsIgnoreCase(qualityAttribute.get(l).asText(""))) {
                                    resultIssue.add(issue);
                                    add = true;
                                    break;
                                }
                            }
                        }

                        if(!add && requestedQualityAttribute.has("keywords") && requestedQualityAttribute.get("keywords").isArray()) {
                            ArrayNode requestedKeywords = (ArrayNode) requestedQualityAttribute.get("keywords");
                            for(int m=0; m<requestedKeywords.size(); m++) {
                                String requestedKeyword = requestedKeywords.get(m).asText("");
                                for(int k=0; k<qas.size(); k++) {
                                    JsonNode qa = qas.get(k);
                                    ArrayNode actualKeywords = (ArrayNode) qa.get("keywords");
                                    for(int n=0; n<actualKeywords.size(); n++) {
                                        if(requestedKeyword.equalsIgnoreCase(actualKeywords.get(n).asText(""))) {
                                            String actualQA = qa.get("name").asText();
                                            ArrayNode issuesQualityAttribute = (ArrayNode) issue.get("qualityAttributes");
                                            for(int l=0; l<issuesQualityAttribute.size(); l++) {
                                                if(actualQA.equalsIgnoreCase(issuesQualityAttribute.get(l).asText(""))) {
                                                    resultIssue.add(issue);
                                                    add = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if(add) break;
                                    }
                                    if(add) break;
                                }
                                if(add) break;
                            }
                        }
                    }
                }
            }
            result.put("status", "OK");
            result.put("statusCode", "200");
            result.set("data", resultIssue);
        } else {
            result.put("status", "Bad request - missing request parameters");
            result.put("statusCode", "400");
        }
        return ok(result);
    }
}