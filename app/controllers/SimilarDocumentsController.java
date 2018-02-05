package controllers;

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
import util.HtmlUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Manoj on 1/31/2018.
 */
public class SimilarDocumentsController extends Controller {
    @Inject WSClient ws;

    public Result updateSimilarDocuments(String projectKey) {
        Logger.debug("request to update DDs with similar documents");
        String url = Configuration.root().getString("docclustering.url", "").concat("clustering/pipeline/predict");
        Issue issueModel = new Issue();
        ArrayNode issues = issueModel.findAllDesignDecisionsInAProject(projectKey);
        issues.forEach(issue -> {
            System.out.println(issue.get("name").asText());
            String text = HtmlUtil.convertToPlaintext((issue.get("summary").asText("") + " " + issue.get("description").asText("")).toLowerCase().replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("http.*?\\s", " "));
            ObjectNode json = Json.newObject().put("textToClassify", text);
            json.put("pipeline", Json.newObject().put("name", projectKey).put("library", 1));

            ws.url(url).post(json).thenApply(response -> {
                List similarDocuments = new ArrayList();
                ArrayNode sds = (ArrayNode) response.asJson().get("result");
                for(int i=0; i<sds.size(); i++) {
                    ObjectNode sd = (ObjectNode) sds.get(i);
                    Map similarDocument = new HashMap();
                    similarDocument.put("name", sd.get("_c0").asText(""));
                    if(sd.has("_c1")) similarDocument.put("summary", sd.get("_c1").asText("").trim());
                    else similarDocument.put("summary", "");
                    if(sd.has("_c2")) similarDocument.put("description", sd.get("_c2").asText("").trim());
                    else similarDocument.put("description", "");
                    //similarDocument.set("features", sd.get("filtered"));
                    similarDocument.put("cosinesimilarity", sd.get("cosinesimilarity").asText(""));
                    similarDocument.put("jaccardsimilarity", sd.get("jaccardsimilarity").asText(""));
                    similarDocuments.add(similarDocument);
                }
                if(similarDocuments.size() > 0) {
                    BasicDBObject newConcepts = new BasicDBObject();
                    newConcepts.append("$set", new BasicDBObject().append("amelie.similarDocuments", similarDocuments));
                    issueModel.updateIssueByKey(issue.get("name").asText(), newConcepts);
                }

                return ok();
            }).toCompletableFuture().join();
        });

        Logger.debug("Similar DDs have been updated");
        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("statusCode", "200");
        return ok(result);
    }

}
