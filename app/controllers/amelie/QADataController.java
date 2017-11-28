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
 * Created by Manoj on 5/18/2017.
 */
public class QADataController extends Controller {

    public Result getQAData(String projectName) {
        List<String> qaList = new ArrayList();
        List<Integer> yearList = Arrays.asList(2013, 2014, 2015, 2016, 2017);
        ArrayNode results = Json.newArray();
        Issue issueModel = new Issue();
        ArrayNode issues = issueModel.getDesignDecisionsForQAView(projectName);
        issues.forEach(issue -> {
            JsonNode qualityAttributes = issue.get("qualityAttributes");
            qualityAttributes.forEach(qa -> {
                String key = qa.asText("");
                if(!qaList.contains(key)) {
                    qaList.add(key);
                }
            });
        });

        qaList.forEach(qa -> {
            ObjectNode res = Json.newObject();
            res.put("id", qa);
            res.set("value", getDecisionCount(qa, 2017, issues));
            ArrayNode values = Json.newArray();
            for(int i=0; i<yearList.size(); i++) {
                ObjectNode v = Json.newObject();
                v.put("year", yearList.get(i));
                v.set("value", getDecisionCount(qa, yearList.get(i), issues));
                values.add(v);
            }
            res.set("values", values);
            results.add(res);
        });

        return StaticFunctions.jsonResult(ok(results));
    }

    private ArrayNode getDecisionCount(String qualityAttribute, int year, ArrayNode issues) {
        ArrayNode ja = Json.newArray();
        List<String> dcs = Arrays.asList("Structural decision", "Behavioral decision", "Non-existence - ban decision");

        AtomicInteger countA = new AtomicInteger();
        AtomicInteger countB = new AtomicInteger();
        AtomicInteger countC = new AtomicInteger();

        issues.forEach(issue -> {
            String date = issue.get("resolved").asText("");
            if(date.contains("-")) {
                try{
                    int resolvedYear = Integer.parseInt(date.split("-")[0]);
                    if(year >= resolvedYear) {
                        JsonNode qas = issue.get("qualityAttributes");
                        String decisionCategory = issue.get("decisionCategory").asText();
                        qas.forEach(qa -> {
                            String key = qa.asText("");
                            if(key.equalsIgnoreCase(qualityAttribute)) {
                                if(dcs.indexOf(decisionCategory) == 0)
                                    countA.getAndIncrement();
                                else if(dcs.indexOf(decisionCategory) == 1)
                                    countB.getAndIncrement();
                                else if(dcs.indexOf(decisionCategory) == 2)
                                    countC.getAndIncrement();
                            }
                        });
                    }
                } catch (Exception e) {
                    Logger.error("Cannot get resolved date!");
                }
            }
        });

        ja.add(countA.intValue());
        ja.add(countB.intValue());
        ja.add(countC.intValue());

        return ja;
    }
}
