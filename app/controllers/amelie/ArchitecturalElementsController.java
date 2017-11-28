package controllers.amelie;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import model.amelie.Issue;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import services.HelperService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manoj on 8/8/2017.
 */
public class ArchitecturalElementsController extends Controller {
    @Inject
    WSClient ws;

    public Result updateTaskWithAE(String projectName) {
        Logger.debug("request to update Tasks with AEs");
        HelperService hs = new HelperService(ws);

        Issue issueModel = new Issue();
        ArrayNode issues = issueModel.findAllDesignDecisionsInAProject(projectName);
        issues.forEach(issue -> {
            List<String> conceptList = getConceptsList(issue.get("summary").asText("") + " " + issue.get("description").asText(""), hs);
            if(conceptList.size() > 0) {
                BasicDBObject newConcepts = new BasicDBObject();
                newConcepts.append("$set", new BasicDBObject().append("concepts", conceptList));
                issueModel.updateIssueById(issue.get("id").asText(), newConcepts);
            }
        });

        Logger.debug("AEs for tasks have been updated");
        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("statusCode", "200");
        return ok(result);
    }

    private List<String> getConceptsList(String text, HelperService hs) {
        List<String> tokens = new ArrayList<>();
        ObjectNode request = Json.newObject();
        request.put("content", text);

        Configuration config = Configuration.root();
        hs.postWSRequest(config.getString("akrec.url") + "processDocument", request).thenApply(response -> {
            ArrayNode annotations = (ArrayNode) response.get("data");
            annotations.forEach(annotation -> {
                String conceptName = annotation.get("token").asText("").toLowerCase();
                if(!tokens.contains(conceptName)) {
                    tokens.add(conceptName);
                }
            });
            return ok();
        }).toCompletableFuture().join();

        return tokens;
    }
}
