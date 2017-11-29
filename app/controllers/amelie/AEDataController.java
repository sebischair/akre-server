package controllers.amelie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.amelie.Issue;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.StaticFunctions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Manoj on 4/11/2017.
 */
public class AEDataController extends Controller {

    public Result getAEData(String projectKey) {
        List<String> conceptList = new ArrayList();
        List<Integer> yearList = Arrays.asList(2013, 2014, 2015, 2016, 2017);
        ArrayNode results = Json.newArray();
        Issue issueModel = new Issue();
        ArrayNode issues = issueModel.getDesignDecisionsForAEView(projectKey);
        issues.forEach(issue -> {
            JsonNode concepts = issue.get("concepts");
            concepts.forEach(concept -> {
                String key = concept.asText("").replaceAll("s$", "");
                if(!conceptList.contains(key)) {
                    conceptList.add(key);
                }
            });
        });

        conceptList.forEach(concept -> {
            ObjectNode res = Json.newObject();
            res.put("id", concept);
            res.put("value", getDecisionCount(concept, 2017, issues));
            int[] values = new int[yearList.size()];
            for(int i=0; i<yearList.size(); i++) {
                values[i] = getDecisionCount(concept, yearList.get(i), issues);
            }
            res.set("values", Json.toJson(values));
            results.add(res);
        });

        return StaticFunctions.jsonResult(ok(results));
    }

    private int getDecisionCount(String ae, int year, ArrayNode issues) {
        AtomicInteger count = new AtomicInteger();
        issues.forEach(issue -> {
            String date = issue.get("resolved").asText("");
            if(date.contains("-")) {
                try{
                    int resolvedYear = Integer.parseInt(date.split("-")[0]);
                    if(year >= resolvedYear) {
                        JsonNode concepts = Json.toJson(issue.get("concepts"));
                        concepts.forEach(concept -> {
                            String key = concept.asText("").replaceAll("s$", "");
                            if(key.equalsIgnoreCase(ae)) {
                                count.getAndIncrement();
                            }
                        });
                    }
                } catch (Exception e) {
                    Logger.error("Cannot get resolved date!");
                }
            }
        });
        return count.get();
    }
}