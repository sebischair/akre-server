package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import services.HelperService;
import util.StaticFunctions;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mahabaleshwar on 7/7/2017.
 */
public class PredictionController extends Controller {
    @Inject
    WSClient ws;

    public Result predictAssignee(String projectId) {
        ArrayNode ja = Json.newArray();
        ArrayNode results = Json.newArray();
        HelperService hs = new HelperService(ws);
        List<String> conceptList = new ArrayList<>();
        List<String> assigneeList = new ArrayList<>();
        ObjectNode summaryResult = Json.newObject();
        ArrayNode testingData = Json.newArray();
        Set<String> allExpertsInDataSet = new HashSet<>();

        hs.executeMxl(StaticFunctions.WORKSPACEID, "getConceptsOfDesignDecisions(\""+ projectId +"\")").thenApply(tasks -> {
            ArrayNode dataset = (ArrayNode) tasks.get(StaticFunctions.VALUE);

            List<ObjectNode> jsonValues = new ArrayList<>();
            for(int i=0; i<dataset.size(); i++) {
                jsonValues.add((ObjectNode) dataset.get(i));
            }

            Collections.sort(jsonValues, new Comparator<ObjectNode>() {
                private static final String KEY_NAME = "resolved";
                @Override
                public int compare(ObjectNode a, ObjectNode b) {
                    String valA = a.get(KEY_NAME).asText("");
                    String valB = b.get(KEY_NAME).asText("");

                    if(valA != "" && valB != "") {
                        DateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
                        try {
                            Date dateA = format.parse(valA);
                            Date dateB = format.parse(valB);
                            return dateA.compareTo(dateB);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    return -1;
                }
            });

            summaryResult.put("Total tasks", jsonValues.size());
            int trainingDataSetSize = (int) Math.floor(jsonValues.size() * 0.9);
            summaryResult.put("Training dataset size", trainingDataSetSize);

            for(int i=0; i<trainingDataSetSize; i++) {
                JsonNode task = jsonValues.get(i);
                String assignee = task.get(StaticFunctions.ASSIGNEE).asText("").toLowerCase();
                if (!assigneeList.contains(assignee)) {
                    assigneeList.add(assignee);
                    allExpertsInDataSet.add(assignee.toLowerCase());
                }

                task.get(StaticFunctions.CONCEPTS).forEach(ca -> {
                    String key = ca.asText("").replaceAll("s$", "").toLowerCase();
                    if (!conceptList.contains(key)) {
                        conceptList.add(key);
                    }
                });
            }

            for(int i=trainingDataSetSize; i<jsonValues.size(); i++) {
                JsonNode task = jsonValues.get(i);
                String assignee = task.get(StaticFunctions.ASSIGNEE).asText("").toLowerCase();
                String summary = task.get(StaticFunctions.SUMMARY).asText("").toLowerCase().trim().replaceAll(" +", " ");
                String description = task.get(StaticFunctions.DESCRIPTION).asText("").toLowerCase().trim().replaceAll(" +", " ");
                JsonNode concepts = task.get(StaticFunctions.CONCEPTS);

                if(assignee != "" && assignee != "unassigned" && summary + description != "" && concepts.size() > 0) {
                    ObjectNode jo = Json.newObject();
                    jo.put(StaticFunctions.ASSIGNEE, assignee.toLowerCase());
                    jo.set(StaticFunctions.CONCEPTS, task.get(StaticFunctions.CONCEPTS));
                    jo.put(StaticFunctions.SUMMARY, summary.toLowerCase());
                    jo.put(StaticFunctions.DESCRIPTION, description.toLowerCase());
                    jo.put("resolved", task.get("resolved").asText(""));
                    testingData.add(jo);
                    allExpertsInDataSet.add(assignee.toLowerCase());
                }
            }
            summaryResult.put("Testing dataset size", testingData.size());
            assigneeList.forEach(assignee -> {
                if (!StaticFunctions.containsStringValue(StaticFunctions.PERSONNAME, assignee, ja)) {
                    ObjectNode jo = Json.newObject();
                    jo.put(StaticFunctions.PERSONNAME, assignee.toLowerCase());
                    jo.set(StaticFunctions.CONCEPTS, Json.newArray());
                    ja.add(jo);
                }
            });

            tasks.get(StaticFunctions.VALUE).forEach(task -> {
                String assignee = task.get(StaticFunctions.ASSIGNEE).asText("").toLowerCase();
                JsonNode ca = task.get(StaticFunctions.CONCEPTS);
                JsonNode personObject = StaticFunctions.getJSONObject(StaticFunctions.PERSONNAME, assignee, ja);
                JsonNode conceptArray = personObject != null ? personObject.get(StaticFunctions.CONCEPTS) : Json.newArray();
                ca.forEach(c -> StaticFunctions.updateConceptArray(c.asText("").replaceAll("s$", "").toLowerCase(), conceptArray));
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

            ArrayNode decisionsToPredict = getRandomConceptVectors(conceptList, testingData, hs, projectId);
            decisionsToPredict = matching(pcvja, decisionsToPredict, conceptList.size());
            decisionsToPredict = ordering(decisionsToPredict);
            /*
            int correctMatch = 0;
            for(int n=0; n <decisionsToPredict.size(); n++) {
                JsonNode dtp = decisionsToPredict.get(n);
                ArrayNode pa = Json.newArray();
                ObjectNode r = Json.newObject();
                String assignee = dtp.get(StaticFunctions.ASSIGNEE).asText("").toLowerCase();
                r.put("text", dtp.get(StaticFunctions.SUMMARY).asText("") + " " + dtp.get(StaticFunctions.DESCRIPTION).asText(""));
                r.put(StaticFunctions.ASSIGNEE, assignee);
                //r.set(StaticFunctions.CONCEPTS, dtp.get(StaticFunctions.CONCEPTS));
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
                    pa.add(jo);
                    if(personName.toLowerCase().equals(assignee.toLowerCase())) correctMatch +=1;

                }
                summaryResult.put("correctMatch", correctMatch);
                //r.set("predictions", pa);
                results.add(r);
            }*/
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
            return ok(results);
        }).toCompletableFuture().join();

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
                max = dtp_pa.size() > (run * 5) ? (run * 5) : dtp_pa.size();
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

    private ArrayNode getRandomConceptVectors(List<String> conceptList, ArrayNode testingData, HelperService hs, String projectId) {
        ArrayNode conceptVectorJSONArray = Json.newArray();
        for(int i=0; i<testingData.size(); i++) {
            JsonNode data =  testingData.get(i);
            ObjectNode conceptVectorJSONObject = Json.newObject();

            conceptVectorJSONObject.put(StaticFunctions.SUMMARY, data.get(StaticFunctions.SUMMARY).asText(""));
            conceptVectorJSONObject.put(StaticFunctions.DESCRIPTION, data.get(StaticFunctions.DESCRIPTION).asText(""));
            conceptVectorJSONObject.put(StaticFunctions.ASSIGNEE, data.get(StaticFunctions.ASSIGNEE).asText(""));
            conceptVectorJSONObject.put("resolved", data.get("resolved").asText(""));

            ArrayNode conceptVector = Json.newArray();
            for (int k = 0; k < conceptList.size(); k++) {
                conceptVector.insert(k, 0);
            }
            JsonNode concepts = data.get(StaticFunctions.CONCEPTS);
            conceptVectorJSONObject.set(StaticFunctions.CONCEPTS, concepts);
            concepts.forEach(concept -> {
                String c = concept.asText("").replaceAll("s$", "").toLowerCase();
                if (conceptList.contains(c)) {
                    int value = getConceptValue(c, data.get(StaticFunctions.SUMMARY).asText("") + " " + data.get(StaticFunctions.DESCRIPTION).asText(""));
                    conceptVector.insert(conceptList.indexOf(concept), value);
                }
            });

            conceptVectorJSONObject.set("conceptVector", conceptVector);
            conceptVectorJSONArray.add(conceptVectorJSONObject);
        }
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
