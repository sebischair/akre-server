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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mahabaleshwar on 7/7/2017.
 */
public class PredictionController extends Controller {
    @Inject
    WSClient ws;

    public Result predictAssignee() {
        ArrayNode ja = Json.newArray();
        ArrayNode results = Json.newArray();
        HelperService hs = new HelperService(ws);
        List<String> conceptList = new ArrayList<>();
        List<String> assigneeList = new ArrayList<>();

        hs.executeMxl(StaticFunctions.WORKSPACEID, "getConceptsOfDesignDecisions()").thenApply(tasks -> {
            tasks.get(StaticFunctions.VALUE).forEach(task -> {
                String assignee = task.get(StaticFunctions.ASSIGNEE).asText("").toLowerCase();
                if (!assigneeList.contains(assignee)) {
                    assigneeList.add(assignee);
                }

                task.get(StaticFunctions.CONCEPTS).forEach(ca -> {
                    String key = ca.asText("").replaceAll("s$", "").toLowerCase();
                    if (!conceptList.contains(key)) {
                        conceptList.add(key);
                    }
                });
            });

            assigneeList.forEach(assignee -> {
                if (!StaticFunctions.containsStringValue("personName", assignee, ja)) {
                    ObjectNode jo = Json.newObject();
                    jo.put("personName", assignee.toLowerCase());
                    jo.set("concepts", Json.newArray());
                    ja.add(jo);
                }
            });

            tasks.get(StaticFunctions.VALUE).forEach(task -> {
                String assignee = task.get(StaticFunctions.ASSIGNEE).asText("");
                JsonNode ca = task.get(StaticFunctions.CONCEPTS);
                JsonNode personObject = StaticFunctions.getJSONObject("personName", assignee, ja);
                JsonNode conceptArray = personObject != null ? personObject.get(StaticFunctions.CONCEPTS) : Json.newArray();
                ca.forEach(c -> StaticFunctions.updateConceptArray(c.asText("").replaceAll("s$", "").toLowerCase(), conceptArray));
            });

            StaticFunctions.removeItemsFromJSONArray(ja, StaticFunctions.getItemsToRemove(ja));
            ArrayNode pcvja = Json.newArray();
            ja.forEach(jo -> {
                ObjectNode pcvjo = Json.newObject();
                pcvjo.put("personName", jo.get("personName").asText(""));
                ArrayNode pcvList = Json.newArray();
                for (int j = 0; j < conceptList.size(); j++) {
                    pcvList.insert(j, personConceptValue(conceptList.get(j), jo));
                }
                pcvjo.set("pcvList", pcvList);
                pcvja.add(pcvjo);
            });

            ArrayNode decisionsToPredict = getRandomConceptVectors(conceptList, hs);
            decisionsToPredict = matching(pcvja, decisionsToPredict, conceptList.size());
            decisionsToPredict = ordering(decisionsToPredict);

            decisionsToPredict.forEach(dtp -> {
                ArrayNode pa = Json.newArray();
                ObjectNode r = Json.newObject();
                r.put("text", dtp.get(StaticFunctions.SUMMARY).asText("") + " " + dtp.get(StaticFunctions.DESCRIPTION).asText(""));
                r.put(StaticFunctions.ASSIGNEE, dtp.get(StaticFunctions.ASSIGNEE).asText(""));
                ObjectNode jo;
                JsonNode dtp_jo;
                JsonNode dtp_pa = dtp.get("predictionArray");

                int count = 0;
                for (int j = 0; j < dtp_pa.size(); j++) {
                    dtp_jo = dtp_pa.get(j);
                    jo = Json.newObject();
                    jo.put("personName", dtp_jo.get("personName").asText(""));
                    jo.put("score", dtp_jo.get("score").asInt(0));
                    pa.add(jo);
                    count++;
                    if (count > 10) break;
                }

                System.out.println(pa);
                r.set("predictions", pa);
                results.add(r);
            });
            System.out.println(results);
            return ok(results);
        }).toCompletableFuture().join();

        return ok(results);
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

    private ArrayNode getRandomConceptVectors(List<String> conceptList, HelperService hs) {
        ArrayNode conceptVectorJSONArray = Json.newArray();
        hs.entitiesForTypeUid(StaticFunctions.TASKID).thenApply(scTasks -> {
            for (int i = 0; i < 10; i++) {
                ObjectNode conceptVectorJSONObject = Json.newObject();
                hs.entityForUid(scTasks.get(i).get(StaticFunctions.ID).asText()).thenApply(project -> {
                    boolean add = false;
                    if (project.has(StaticFunctions.ATTRIBUTES)) {
                        JsonNode attributesArray = project.get(StaticFunctions.ATTRIBUTES);
                        ArrayNode conceptVector = Json.newArray();
                        for (int k = 0; k < conceptList.size(); k++) {
                            conceptVector.insert(k, 0);
                        }

                        attributesArray.forEach(attribute -> {
                            if (attribute.get(StaticFunctions.NAME).asText("").equalsIgnoreCase(StaticFunctions.SUMMARY) && attribute.get(StaticFunctions.VALUES).size() > 0) {
                                conceptVectorJSONObject.put(StaticFunctions.SUMMARY, attribute.get(StaticFunctions.VALUES).get(0).toString().toLowerCase());
                            }
                            if (attribute.get(StaticFunctions.NAME).asText("").equalsIgnoreCase(StaticFunctions.DESCRIPTION) && attribute.get(StaticFunctions.VALUES).size() > 0) {
                                conceptVectorJSONObject.put(StaticFunctions.DESCRIPTION, attribute.get(StaticFunctions.VALUES).get(0).toString());
                            }
                            if (attribute.get(StaticFunctions.NAME).asText("").equalsIgnoreCase(StaticFunctions.ASSIGNEE) && attribute.get(StaticFunctions.VALUES).size() > 0) {
                                conceptVectorJSONObject.put(StaticFunctions.ASSIGNEE, attribute.get(StaticFunctions.VALUES).get(0).toString());
                            }
                        });

                        for (int j = 0; j < attributesArray.size(); j++) {
                            JsonNode attribute = attributesArray.get(j);
                            if (attribute.get(StaticFunctions.NAME).asText("").equalsIgnoreCase(StaticFunctions.CONCEPTS) && attribute.get(StaticFunctions.VALUES).size() > 0) {
                                add = true;
                                JsonNode concepts = attribute.get(StaticFunctions.VALUES);
                                concepts.forEach(conceptItr -> {
                                    String concept = conceptItr.get(StaticFunctions.NAME).asText("").replaceAll("s$", "").toLowerCase();
                                    if (conceptList.contains(concept)) {
                                        int value = getConceptValue(concept, conceptVectorJSONObject.get(StaticFunctions.DESCRIPTION).asText("") + " " + conceptVectorJSONObject.get(StaticFunctions.SUMMARY).asText(""));
                                        conceptVector.insert(conceptList.indexOf(concept), value);
                                    }
                                });
                            }
                        }

                        if (add) {
                            conceptVectorJSONObject.set("conceptVector", conceptVector);
                            conceptVectorJSONArray.add(conceptVectorJSONObject);
                        }
                    }
                    return ok();
                }).toCompletableFuture().join();
            }
            return ok();
        }).toCompletableFuture().join();

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
