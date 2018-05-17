package controllers.amelie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.amelie.Issue;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.StaticFunctions;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Manoj on 7/7/2017.
 */
public class AssigneePredictionController extends Controller {

    public Result predictAssignee(String projectKey) {
        ArrayNode ja = Json.newArray();
        ArrayNode results = Json.newArray();
        List<String> conceptList = new ArrayList<>();
        List<String> assigneeList = new ArrayList<>();
        ObjectNode summaryResult = Json.newObject();
        ArrayNode testingData = Json.newArray();
        Set<String> allExpertsInDataSet = new HashSet<>();

        Issue issueModel = new Issue();
        ArrayNode issues = issueModel.findAllDesignDecisionsForPredictionInAProject(projectKey);
        List<ObjectNode> orderedIssues = issueModel.orderIssuesByResolutionDate(issues);
        summaryResult.put("Total tasks", orderedIssues.size());
        int trainingDataSetSize = (int) Math.floor(issues.size() * 0.9);
        summaryResult.put("Training dataset size", trainingDataSetSize);

        List<ObjectNode> trainingIssues = orderedIssues.subList(0, trainingDataSetSize);
        List<ObjectNode> testingIssues = orderedIssues.subList(trainingDataSetSize, orderedIssues.size());

        trainingIssues.forEach(issue -> {
            if(issue.has(StaticFunctions.ASSIGNEE)) {
                String assignee = issue.get(StaticFunctions.ASSIGNEE).asText("").toLowerCase();
                if (!assigneeList.contains(assignee)) {
                    assigneeList.add(assignee);
                    allExpertsInDataSet.add(assignee.toLowerCase());
                }
            }

            issue.get(StaticFunctions.CONCEPTS).forEach(ca -> {
                String key = ca.asText("").replaceAll("s$", "").toLowerCase();
                if (!conceptList.contains(key)) {
                    conceptList.add(key);
                }
            });
        });

        testingIssues.forEach(issue -> {
            if (issue.has(StaticFunctions.ASSIGNEE)) {
                String assignee = issue.get(StaticFunctions.ASSIGNEE).asText("").toLowerCase();
                String summary = issue.get(StaticFunctions.SUMMARY).asText("").toLowerCase().trim().replaceAll(" +", " ");
                String description = issue.get(StaticFunctions.DESCRIPTION).asText("").toLowerCase().trim().replaceAll(" +", " ");
                JsonNode concepts = issue.get(StaticFunctions.CONCEPTS);

                if (assignee != "" && assignee != "unassigned" && summary + description != "" && concepts.size() > 0) {
                    ObjectNode jo = Json.newObject();
                    jo.put(StaticFunctions.ASSIGNEE, assignee.toLowerCase());
                    jo.set(StaticFunctions.CONCEPTS, issue.get(StaticFunctions.CONCEPTS));
                    jo.put(StaticFunctions.SUMMARY, summary.toLowerCase());
                    jo.put(StaticFunctions.DESCRIPTION, description.toLowerCase());
                    jo.put("resolved", issue.get("resolved").asText(""));
                    testingData.add(jo);
                    allExpertsInDataSet.add(assignee.toLowerCase());
                }
            }
        });

        summaryResult.put("Testing dataset size", testingData.size());
        assigneeList.forEach(assignee -> {
            if (!StaticFunctions.containsStringValue(StaticFunctions.PERSONNAME, assignee, ja)) {
                ObjectNode jo = Json.newObject();
                jo.put(StaticFunctions.PERSONNAME, assignee.toLowerCase());
                jo.set(StaticFunctions.CONCEPTS, Json.newArray());
                ja.add(jo);
            }
        });

        orderedIssues.forEach(issue -> {
            if(issue.has(StaticFunctions.ASSIGNEE)) {
                String assignee = issue.get(StaticFunctions.ASSIGNEE).asText("").toLowerCase();
                JsonNode ca = issue.get(StaticFunctions.CONCEPTS);
                JsonNode personObject = StaticFunctions.getJSONObject(StaticFunctions.PERSONNAME, assignee, ja);
                JsonNode conceptArray = personObject != null ? personObject.get(StaticFunctions.CONCEPTS) : Json.newArray();
                ca.forEach(c -> StaticFunctions.updateConceptArray(c.asText("").replaceAll("s$", "").toLowerCase(), conceptArray));
            }
        });

        StaticFunctions.removeItemsFromJSONArray(ja, StaticFunctions.getItemsToRemove(ja));
        ArrayNode pcvja = Json.newArray();
        ja.forEach(jo -> {
            ObjectNode pcvjo = Json.newObject();
            pcvjo.put(StaticFunctions.PERSONNAME, jo.get(StaticFunctions.PERSONNAME).asText(""));
            ArrayNode pcvList = Json.newArray();
            for (int j = 0; j < conceptList.size(); j++) {
                pcvList.insert(j, personConceptValue(conceptList.get(j), jo));
            }
            pcvjo.set("pcvList", pcvList);
            pcvja.add(pcvjo);
        });

        ArrayNode decisionsToPredict = getRandomConceptVectors(conceptList, testingIssues);
        decisionsToPredict = matching(pcvja, decisionsToPredict, conceptList.size());
        decisionsToPredict = ordering(decisionsToPredict);

        int correctMatch = 0;
        for(int n=0; n <decisionsToPredict.size(); n++) {
            JsonNode dtp = decisionsToPredict.get(n);
            ArrayNode pa = Json.newArray();
            ObjectNode r = Json.newObject();
            String assignee = dtp.get(StaticFunctions.ASSIGNEE).asText("").toLowerCase();
            r.put("text", dtp.get(StaticFunctions.SUMMARY).asText("") + " " + dtp.get(StaticFunctions.DESCRIPTION).asText(""));
            r.put(StaticFunctions.ASSIGNEE, assignee);
            r.put("resolved", dtp.get("resolved").asText(""));
            ObjectNode jo;
            JsonNode dtp_jo;
            JsonNode dtp_pa = dtp.get("predictionArray");

            int max = dtp_pa.size() > 10 ? 10 : dtp_pa.size();
            for(int j = 0; j < max; j++) {
                dtp_jo = dtp_pa.get(j);
                jo = Json.newObject();
                String personName = dtp_jo.get(StaticFunctions.PERSONNAME).asText("").toLowerCase();
                jo.put(StaticFunctions.PERSONNAME, personName);
                int score = dtp_jo.get("score").asInt(0);

                jo.put("score", score);
                if(pa.size() < 5) pa.add(jo);
                if(personName.toLowerCase().equals(assignee.toLowerCase())) correctMatch +=1;

            }
            summaryResult.put("correctMatch", correctMatch);
            r.set("predictions", pa);
            results.add(r);
        }
        /*
            ArrayNode correctMatches = Json.newArray();
            ArrayNode catalogCoverages = Json.newArray();
            Set<String> allRecommendedExpertsInTestingSet = new HashSet<>();
            for(int k=1; k<7; k++) {
                correctMatches.add(computeCorrectMatch(decisionsToPredict, k, allRecommendedExpertsInTestingSet));
                catalogCoverages.add(allRecommendedExpertsInTestingSet.size() / allExpertsInDataSet.size());
                allRecommendedExpertsInTestingSet = new HashSet<>();
            }
            correctMatches.add(computeCorrectMatch(decisionsToPredict, 0, allRecommendedExpertsInTestingSet));
            catalogCoverages.add(allRecommendedExpertsInTestingSet.size() / allExpertsInDataSet.size());

            summaryResult.set("catalogCoverages", catalogCoverages);

            int testingDataSetForWhichPredictionsWereMade = 0;
            for(int n=0; n <decisionsToPredict.size(); n++) {
                JsonNode dtp = decisionsToPredict.get(n);
                JsonNode dtp_pa = dtp.get("predictionArray");
                if(dtp_pa.size() > 0) {
                    testingDataSetForWhichPredictionsWereMade += 1;
                }
            }
            summaryResult.put("predictionCoverage", testingDataSetForWhichPredictionsWereMade/testingData.size());
            summaryResult.set("correctMatch", correctMatches);
            results.add(summaryResult);
        */
        if(results.size() == 0) {
            return StaticFunctions.jsonResult(ok(results.add(Json.newObject())));
        }
        return ok(results);
    }

    private int computeCorrectMatch(ArrayNode decisionsToPredict, int run, Set<String> allRecommendedExpertsInTestingSet) {
        int correctMatch = 0;
        for(int n=0; n <decisionsToPredict.size(); n++) {
            JsonNode dtp = decisionsToPredict.get(n);
            ArrayNode pa = Json.newArray();
            String assignee = dtp.get(StaticFunctions.ASSIGNEE).asText("").toLowerCase();
            ObjectNode jo;
            JsonNode dtp_jo;
            JsonNode dtp_pa = dtp.get("predictionArray");
            int max = dtp_pa.size();
            if(run != 0) {
                max = dtp_pa.size() > (run * 2) ? (run * 2) : dtp_pa.size();
                //max = dtp_pa.size() > (run * 5) ? (run * 5) : dtp_pa.size();
            }
            for(int j = 0; j < max; j++) {
                dtp_jo = dtp_pa.get(j);
                jo = Json.newObject();
                String personName = dtp_jo.get(StaticFunctions.PERSONNAME).asText("").toLowerCase();
                jo.put(StaticFunctions.PERSONNAME, personName);
                int score = dtp_jo.get("score").asInt(0);
                jo.put("score", score);
                pa.add(jo);
                allRecommendedExpertsInTestingSet.add(personName);
                if(personName.toLowerCase().equals(assignee.toLowerCase())) correctMatch +=1;
            }
        }
        return correctMatch;
    }

    private ArrayNode ordering(ArrayNode decisionsToPredict) {
        decisionsToPredict.forEach(decisionsToPredictItr -> {
            JsonNode predArray = decisionsToPredictItr.get("predictionArray");
            for (int j = 0; j < predArray.size(); j++) {
                int score = 0;
                JsonNode pcvList = predArray.get(j).get("pcvList");
                for (int k = 0; k < pcvList.size(); k++) {
                    score += pcvList.get(k).asInt(0);
                }
                ((ObjectNode) predArray.get(j)).put("score", score);
            }

            ArrayNode newPredArray = sort(predArray);
            ((ObjectNode) decisionsToPredictItr).set("predictionArray", newPredArray);
        });

        return decisionsToPredict;
    }

    private ArrayNode sort(JsonNode predArray) {
        List<JsonNode> jsonValues = new ArrayList<>();
        predArray.forEach(jsonValues::add);

        Collections.sort(jsonValues, new Comparator<JsonNode>() {
            private static final String KEY_NAME = "score";

            @Override
            public int compare(JsonNode a, JsonNode b) {
                Double valA = a.get(KEY_NAME).asDouble(0);
                Double valB = b.get(KEY_NAME).asDouble(0);

                return -valA.compareTo(valB);
            }
        });

        return Json.newArray().addAll(jsonValues);
    }

    private ArrayNode matching(ArrayNode pcvja, ArrayNode decisionsToPredict, int conceptSize) {
        decisionsToPredict.forEach(decisionsToPredictItr -> {
            ArrayNode predictionArray = Json.newArray();
            JsonNode cv = decisionsToPredictItr.get("conceptVector");
            pcvja.forEach(pcvjo -> {
                boolean isApplicable = false;
                ObjectNode newpcvjo = Json.newObject();
                newpcvjo.put("personName", pcvjo.get("personName").asText(""));
                JsonNode pcvList = pcvjo.get("pcvList");
                ArrayNode newpcvList = Json.newArray();
                for (int k = 0; k < conceptSize; k++) {
                    newpcvList.insert(k, 0);
                    if (cv.get(k).asInt(0) > 0 && pcvList.get(k).asInt(0) > 0) {
                        newpcvList.insert(k, (cv.get(k).asInt(0) * pcvList.get(k).asInt(0)));
                        isApplicable = true;
                    }
                }
                if (isApplicable) {
                    newpcvjo.set("pcvList", newpcvList);
                    predictionArray.add(newpcvjo);
                }
            });

            ((ObjectNode) decisionsToPredictItr).set("predictionArray", predictionArray);
        });
        return decisionsToPredict;
    }

    private ArrayNode getRandomConceptVectors(List<String> conceptList, List<ObjectNode> testingData) {
        ArrayNode conceptVectorJSONArray = Json.newArray();
        testingData.forEach(issue -> {
            ObjectNode conceptVectorJSONObject = Json.newObject();
            conceptVectorJSONObject.put(StaticFunctions.SUMMARY, issue.get(StaticFunctions.SUMMARY).asText(""));
            conceptVectorJSONObject.put(StaticFunctions.DESCRIPTION, issue.get(StaticFunctions.DESCRIPTION).asText(""));
            conceptVectorJSONObject.put(StaticFunctions.ASSIGNEE, issue.get(StaticFunctions.ASSIGNEE).asText(""));
            conceptVectorJSONObject.put("resolved", issue.get("resolved").asText(""));

            ArrayNode conceptVector = Json.newArray();
            for(int k = 0; k < conceptList.size(); k++) {
                conceptVector.insert(k, 0);
            }
            JsonNode concepts = issue.get(StaticFunctions.CONCEPTS);
            conceptVectorJSONObject.set(StaticFunctions.CONCEPTS, concepts);
            concepts.forEach(concept -> {
                String c = concept.asText("").replaceAll("s$", "").toLowerCase();
                if(conceptList.contains(c)) {
                    int value = getConceptValue(c, issue.get(StaticFunctions.SUMMARY).asText("") + " " + issue.get(StaticFunctions.DESCRIPTION).asText(""));
                    conceptVector.insert(conceptList.indexOf(concept), value);
                }
            });

            conceptVectorJSONObject.set("conceptVector", conceptVector);
            conceptVectorJSONArray.add(conceptVectorJSONObject);
        });
        return conceptVectorJSONArray;
    }

    private int getConceptValue(String concept, String s) {
        s = s.replaceAll("\\(", "").replaceAll("\\)", "");
        int i = 0;
        Pattern p = Pattern.compile(concept.toLowerCase());
        Matcher m = p.matcher(s.toLowerCase());
        while (m.find()) {
            i++;
        }
        return i;
    }

    private int personConceptValue(String concept, JsonNode jo) {
        JsonNode co;
        JsonNode ca = jo.get(StaticFunctions.CONCEPTS);
        for (int k = 0; k < ca.size(); k++) {
            co = ca.get(k);
            if (co.get("conceptName").asText("").equalsIgnoreCase(concept)) {
                return co.get("value").asInt();
            }
        }
        return 0;
    }
}
