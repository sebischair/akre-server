package controllers.amelie;

import akka.stream.Materializer;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import model.amelie.Project;
import play.Logger;
import play.core.j.JavaResultExtractor;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * Created by Manoj on 6/1/2018.
 */
public class PreProcessProjectController extends Controller {
    @Inject
    Materializer materializer;

    @Inject private LabelDesignDecisionsController labelDesignDecisionsController;
    @Inject private QualityAttributesController qaController;
    @Inject private ArchitecturalElementsController aeController;
    @Inject private SimilarDocumentsController similarDocumentsController;

    public Result preProcess(String projectKey) {
        Logger.debug("request to process all issues in a project");
        Result response = labelDesignDecisionsController.labelDesignDecisions(projectKey);
        JsonNode responseAsJson = getJsonFromResult(response);
        if(responseAsJson.get("statusCode").asText("").equals("200")) {
            JsonNode qaResponse = getJsonFromResult(qaController.updateTaskWithQA(projectKey));
            JsonNode aeResponse = getJsonFromResult(aeController.updateTaskWithAE(projectKey));
            JsonNode sdResponse = getJsonFromResult(similarDocumentsController.updateSimilarDocuments(projectKey));
            if(qaResponse.get("statusCode").asText("").equals("200") && aeResponse.get("statusCode").asText("").equals("200") && sdResponse.get("statusCode").asText("").equals("200")) {
                return updateProjectProcessState(projectKey);
            } else {
                ArrayNode r = Json.newArray();
                r.add(qaResponse);
                r.add(aeResponse);
                r.add(sdResponse);
                return ok(r);
            }
        }
        return response;
    }

    private JsonNode getJsonFromResult(Result response) {
        ByteString body = JavaResultExtractor.getBody(response, 1, materializer);
        return play.libs.Json.parse(body.utf8String());
    }

    private Result updateProjectProcessState(String projectKey) {
        Project p = new Project();
        BasicDBObject preProcessedObject = new BasicDBObject();
        preProcessedObject.append("$set", new BasicDBObject().append("preProcessed", true));
        p.updateProjectByKey(projectKey, preProcessedObject);

        p.updateDecisionCount(projectKey);

        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("message", "Project has been preprocessed!");
        result.put("statusCode", "200");
        return ok(result);
    }
}
