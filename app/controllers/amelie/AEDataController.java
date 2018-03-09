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

import java.util.*;
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
            if(concepts != null && concepts.isArray()) {
                concepts.forEach(concept -> {
                    String key = concept.asText("").replaceAll("s$", "");
                    if(!conceptList.contains(key)) {
                        conceptList.add(key);
                    }
                });
            }
        });

        conceptList.forEach(concept -> {
            ObjectNode res = Json.newObject();
            res.put("id", concept);
            res.put("value", getDecisionCount(concept, 2017, issues));
            ArrayNode values = Json.newArray();
            yearList.forEach(year -> {
                ObjectNode valueObject = Json.newObject();
                valueObject.put("year", year);
                valueObject.put("value", getDecisionCount(concept, year, issues));
                values.add(valueObject);
            });
            res.set("values", values);
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
                    String simpleDate = date.split(" ")[0];
                    if(simpleDate.split("-").length > 2) {
                        int resolvedYear = Integer.parseInt(simpleDate.split("-")[0]);
                        if (year >= resolvedYear) {
                            JsonNode concepts = Json.toJson(issue.get("concepts"));
                            if(concepts != null && concepts.isArray()) {
                                concepts.forEach(concept -> {
                                    String key = concept.asText("").replaceAll("s$", "");
                                    if (key.equalsIgnoreCase(ae)) {
                                        count.getAndIncrement();
                                    }
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    Logger.error("Cannot get resolved date!");
                }
            }
        });
        return count.get();
    }
}