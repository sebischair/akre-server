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
        List<Integer> yearList = Arrays.asList(2017);
        ArrayNode results = Json.newArray();
        Issue issueModel = new Issue();
        ArrayNode issues = issueModel.getDesignDecisionsForAEView(projectKey);

        Map<String, Integer> cv = new HashMap<>();

        issues.forEach(issue -> {
            JsonNode concepts = issue.get("concepts");
            if(concepts != null && concepts.isArray()) {
                concepts.forEach(concept -> {
                    String key = concept.asText("");
                    if(!cv.containsKey(key)) {
                        conceptList.add(key);
                        cv.put(key, 1);
                    } else {
                        Integer value = cv.get(key);
                        cv.replace(key, value+1);
                    }
                });
            }
        });

        cv.forEach((k, v) -> {
            if(v > 50) {
                ObjectNode res = Json.newObject();
                res.put("id", k);
                res.put("value", v);
                results.add(res);
            }
        });

        return StaticFunctions.jsonResult(ok(results));
    }

    private int getDecisionCount(String ae, int year, ArrayNode issues) {
        AtomicInteger count = new AtomicInteger();
        issues.forEach(issue -> {
            String date = issue.get("resolved").asText("");
            if(date.contains(".") && date.contains(" ")) {
                try{
                    String simpleDate = date.split(" ")[0];
                    if(simpleDate.split("\\.").length > 2) {
                        int resolvedYear = Integer.parseInt(simpleDate.split("\\.")[2]);
                        //if (year >= resolvedYear) {
                            JsonNode concepts = Json.toJson(issue.get("concepts"));
                            if(concepts != null && concepts.isArray()) {
                                concepts.forEach(concept -> {
                                    String key = concept.asText("");
                                    if (key.equalsIgnoreCase(ae)) {
                                        count.getAndIncrement();
                                    }
                                });
                            }
                        //}
                    }
                } catch (Exception e) {
                    Logger.error("Cannot get resolved date!");
                }
            }
        });
        return count.get();
    }
}