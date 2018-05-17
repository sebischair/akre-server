package controllers.amelie;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import model.amelie.Issue;
import play.Configuration;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import services.HelperService;
import util.StaticFunctions;

/**
 * Created by Manoj on 8/20/2017.
 */
public class LabelDesignDecisionsController extends Controller {
    @Inject
    WSClient ws;

    HelperService hs;
    Configuration configuration;

    private void setHS(HelperService hs) {
        this.hs = hs;
    }

    private void setConf(Configuration conf) {
        this.configuration = conf;
    }

    public Result labelDesignDecisions(String projectKey) {
        setHS(new HelperService(ws));
        setConf(Configuration.root());

        Issue issueModel = new Issue();
        ArrayNode issues = issueModel.findAllIssuesInAProject(projectKey);
        issues.forEach(issue -> {
            String summary = issue.get("summary").asText("");
            String description = issue.get("description").asText("");
            String isDesignDecisionLabel = getDesignDecisionLabel(summary + " " + description).get(StaticFunctions.LABEL).toString();
            boolean isDesignDecision = false;
            if (isDesignDecisionLabel.toLowerCase().contains("yes")) {
                isDesignDecision = true;
            }
            BasicDBObject decisionObject = new BasicDBObject();
            decisionObject.append("$set", new BasicDBObject().append("amelie.designDecision", isDesignDecision));
            issueModel.updateIssueByKey(issue.get("name").asText(), decisionObject);
            if(isDesignDecision) {
                String decisionCategoryLabel = getDecisionCategoryLabel(summary + " " + description).get(StaticFunctions.LABEL).asText();
                if(decisionCategoryLabel.split("Class predicted: ").length > 1) {
                    String dcl = decisionCategoryLabel.split("Class predicted: ")[1];
                    BasicDBObject categoryObject = new BasicDBObject();
                    categoryObject.append("$set", new BasicDBObject().append("amelie.decisionCategory", dcl));
                    issueModel.updateIssueByKey(issue.get("name").asText(), categoryObject);
                }
            }
        });

        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("message", "Design decisions are labeled!");
        result.put("statusCode", "200");
        return ok(result);
    }

    private ObjectNode getDesignDecisionLabel(String textToClassify) {
        ObjectNode body = Json.newObject();
        body.put("pipelineName", "DesignDecisions");
        body.put("textToClassify", textToClassify);
        ObjectNode result = Json.newObject();
        hs.postWSRequest(configuration.getString("predict.url"), body).thenApply(response -> {
            result.put("label", response.get("result").asText());
            return ok();
        }).toCompletableFuture().join();
        return result;
    }

    private ObjectNode getDecisionCategoryLabel(String textToClassify) {
        ObjectNode body = Json.newObject();
        body.put("pipelineName", "DecisionCategory");
        body.put("textToClassify", textToClassify);
        ObjectNode result = Json.newObject();
        hs.postWSRequest(configuration.getString("predict.url"), body).thenApply(response -> {
            result.put("label", response.get("result").asText());
            return ok();
        }).toCompletableFuture().join();
        return result;
    }
}
