package controllers.amelie;

import com.aylien.textapi.responses.Concept;
import com.aylien.textapi.responses.Entity;
import com.aylien.textapi.responses.SurfaceForm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import model.amelie.Issue;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.TextAnalysisClient;
import util.StaticFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manoj on 10/16/2017.
 */
public class KeywordExtractorController extends Controller {

    public Result updateConceptsForDesignDecisions(String projectName) {
        TextAnalysisClient tac = new TextAnalysisClient();

        Issue issueModel = new Issue();
        ArrayNode issues = issueModel.findAllDesignDecisionsInAProject(projectName);
        issues.forEach(issue -> {
            JsonNode ca = issue.get(StaticFunctions.CONCEPTS);
            if(ca.size() == 0) {
                String summary = issue.get(StaticFunctions.SUMMARY).asText("").trim().replaceAll(" +", " ").toLowerCase();
                String description = issue.get(StaticFunctions.DESCRIPTION).asText("").trim().replaceAll(" +", " ").toLowerCase();
                List<String> conceptList = new ArrayList<>();
                List<Concept> concepts = tac.extractConcepts(summary + " " + description);
                for (Concept concept : concepts) {
                    SurfaceForm[] sfs = concept.getSurfaceForms();
                    for(int k=0; k<sfs.length; k++) {
                        String sf = sfs[k].getString();
                        if (!conceptList.contains(sf)) {
                            conceptList.add(sf);
                        }
                    }
                }
                if(conceptList.size() > 0) {
                    BasicDBObject newConcepts = new BasicDBObject();
                    newConcepts.append("$set", new BasicDBObject().append("concepts", conceptList));
                    issueModel.updateIssueById(issue.get("id").asText(), newConcepts);
                }
            }
        });

        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("statusCode", "200");
        return ok(result);
    }

    public Result updateKeywordsForDesignDecisions(String projectName) {
        TextAnalysisClient tac = new TextAnalysisClient();
        Issue issueModel = new Issue();
        ArrayNode issues = issueModel.findAllDesignDecisionsInAProject(projectName);
        issues.forEach(issue -> {
            JsonNode ca = issue.get(StaticFunctions.KEYWORDS);
            if(ca.size() == 0) {
                String summary = issue.get(StaticFunctions.SUMMARY).asText("").trim().replaceAll(" +", " ").toLowerCase();
                String description = issue.get(StaticFunctions.DESCRIPTION).asText("").trim().replaceAll(" +", " ").toLowerCase();
                List<String> keywordsList = new ArrayList<>();
                List<Entity> entities = tac.extractKeywords(summary + " " + description);

                for (Entity entity : entities) {
                    for (String sf : entity.getSurfaceForms()) {
                        if (!keywordsList.contains(sf)) {
                            keywordsList.add(sf);
                        }
                    }
                }

                if(keywordsList.size() > 0) {
                    BasicDBObject newConcepts = new BasicDBObject();
                    newConcepts.append("$set", new BasicDBObject().append("keywords", keywordsList));
                    issueModel.updateIssueById(issue.get("id").asText(), newConcepts);
                }
            }
        });

        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("statusCode", "200");
        return ok(result);
    }
}
