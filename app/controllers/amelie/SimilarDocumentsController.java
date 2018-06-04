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
import util.StaticFunctions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Manoj on 1/31/2018.
 */
public class SimilarDocumentsController extends Controller {
    @Inject WSClient ws;
    HelperService hs;

    private ObjectNode createRequestObject(String projectKey) {
        ObjectNode clusterRequest = Json.newObject();
        ObjectNode clusterRequestBody = Json.newObject().put("mongoProjectKey", projectKey);
        clusterRequestBody.put("name", projectKey);
        clusterRequestBody.put("href", projectKey);
        clusterRequestBody.put("scLink", false);
        clusterRequestBody.put("dataset", projectKey);
        clusterRequestBody.put("transformer", Json.newObject().put("id", "spark-word2vec"));
        clusterRequestBody.put("library", Json.newObject().put("id", 1));

        ObjectNode algorithmRequest = Json.newObject();
        algorithmRequest.put("id", "spark-kmeans");
        algorithmRequest.put("name", "spark-kmeans");

        ArrayNode options = Json.newArray();
        ObjectNode kvalue = Json.newObject();
        kvalue.put("name", "K-value");
        kvalue.put("value", 30);
        options.add(kvalue);

        ObjectNode iterations = Json.newObject();
        iterations.put("name", "iterations");
        iterations.put("value", 10);
        options.add(iterations);

        algorithmRequest.put("options", options);

        clusterRequestBody.put("algorithm", algorithmRequest);
        clusterRequest.put("pipeline", clusterRequestBody);
        return clusterRequest;
    }

    private boolean checkIfPipelineExists(String projectKey) {
        String existingPipeline = Configuration.root().getString("docclustering.url", "").concat("clustering/pipeline/" + projectKey);
        ObjectNode result = Json.newObject();
        Logger.debug("check if pipeline exists");
        hs.getWSResponse(existingPipeline).thenApply(res -> {
            if(res.hasNonNull("_id")) {
                result.put("pipelineExists", true);
            } else {
                result.put("pipelineExists", false);
            }
            return ok();
        }).toCompletableFuture().join();
        return result.get("pipelineExists").asBoolean();
    }

    public Result updateSimilarDocuments(String projectKey) {
        Logger.debug("request to update DDs with similar documents");
        hs = new HelperService(ws);
        String clusterURL = Configuration.root().getString("docclustering.url", "").concat("clustering/pipeline/create");
        //String updateSimilarDocumentsURL = Configuration.root().getString("docclustering.url", "").concat("pipeline/updateSimilarDocuments/" + projectKey);
        if(!checkIfPipelineExists(projectKey)) {
            hs.postWSRequest(clusterURL, createRequestObject(projectKey)).thenApply(response -> ok()).toCompletableFuture().join();
        }
        /*hs.getWSResponse(updateSimilarDocumentsURL).thenApply(response -> ok()).toCompletableFuture().join();
        Logger.debug("Similar DDs have been updated");*/
        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("statusCode", "200");
        return ok(result);
    }

    public Result updateSimilarDocumentsForDD(String issueKey) {
        ObjectNode result = Json.newObject();
        Issue issueModel = new Issue();
        try {
            ObjectNode issue = issueModel.getDesignDecisionByKey(issueKey);
            if(issue.has("similarDocuments")) {
                result.put("similarDDs", issue.get("similarDocuments"));
            } else {
                hs = new HelperService(ws);
                String similarDocumentsURL = Configuration.root().getString("docclustering.url", "").concat("similarDecisions?issueKey="+issueKey);
                hs.getWSResponse(similarDocumentsURL).thenApply(response -> {
                    result.put("status", response.get("status"));
                    result.put("statusCode", response.get("statusCode"));
                    return ok();
                }).toCompletableFuture().join();
                issue = issueModel.getDesignDecisionByKey(issueKey);
                result.put("similarDDs", issue.get("similarDocuments"));
            }
        } catch (Exception e) {
            result.put("statusCode", "400");
            e.printStackTrace();
        }
        return ok(result);
    }
}
