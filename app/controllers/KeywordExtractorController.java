package controllers;

import com.aylien.textapi.responses.Concept;
import com.aylien.textapi.responses.Entity;
import com.aylien.textapi.responses.SurfaceForm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONObject;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import services.HelperService;
import services.TextAnalysisClient;
import util.StaticFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Manoj on 10/16/2017.
 */
public class KeywordExtractorController extends Controller {
    @Inject
    WSClient ws;

    public Map<String, String> getAllExistingConcepts(HelperService hs, String id) {
        Map<String, String> conceptIdMap = new HashMap<>();
        hs.entitiesForTypeUid(id).thenApply(concepts -> {
            concepts.forEach(concept -> {
                if(!conceptIdMap.containsKey(concept.get("name").asText(""))) {
                    conceptIdMap.put(concept.get("name").asText("").toLowerCase(), concept.get("id").asText(""));
                }
            });
            return ok();
        }).toCompletableFuture().join();
        return conceptIdMap;
    }

    public Result updateConceptsForDesignDecisions() {
        HelperService hs = new HelperService(ws);
        TextAnalysisClient tac = new TextAnalysisClient();
        Map<String, String> conceptIdMap = getAllExistingConcepts(hs, StaticFunctions.SCCONCEPTSID);

        hs.executeMxl(StaticFunctions.WORKSPACEID, "getAllDesignDecisions()").thenApply(tasks -> {
            tasks.get("value").forEach(task -> {
                JsonNode ca = task.get(StaticFunctions.CONCEPTS);
                if(ca.size() == 0) {
                    String summary = task.get(StaticFunctions.SUMMARY).asText("").trim().replaceAll(" +", " ").toLowerCase();
                    String description = task.get(StaticFunctions.DESCRIPTION).asText("").trim().replaceAll(" +", " ").toLowerCase();
                    List<String> conceptsList = new ArrayList<>();

                    List<Concept> concepts = tac.extractConcepts(summary + " " + description);

                    ArrayNode newConceptValues = Json.newArray();
                    for (Concept concept : concepts) {
                        ArrayNode attrA = Json.newArray();
                        ObjectNode uriAttribute = Json.newObject();
                        uriAttribute.put(StaticFunctions.NAME, StaticFunctions.URI);
                        ArrayNode newUriValues = Json.newArray();
                        newUriValues.add(concept.getUri());
                        uriAttribute.set(StaticFunctions.VALUES, newUriValues);
                        attrA.add(uriAttribute);

                        SurfaceForm[] sfs = concept.getSurfaceForms();
                        for(int k=0; k<sfs.length; k++) {
                            String sf = sfs[k].getString();
                            if (!conceptsList.contains(sf)) {
                                String id = createEntityIfNotNull(sf, attrA, StaticFunctions.SCCONCEPTSID, hs, conceptIdMap).get(0).asText("");
                                if(id != null) {
                                    ObjectNode jo = Json.newObject();
                                    jo.put(StaticFunctions.ID, id);
                                    newConceptValues.add(jo);
                                    conceptsList.add(sf);
                                }
                            }
                        }
                    }
                    basicStringMatch(summary + " " + description, conceptIdMap, conceptsList, newConceptValues);

                    ArrayNode attributesTempArray = Json.newArray();
                    ObjectNode newAttribute = Json.newObject();
                    if(newConceptValues.size() > 0) {
                        newAttribute.put(StaticFunctions.NAME, StaticFunctions.CONCEPTS);
                        newAttribute.set(StaticFunctions.VALUES, newConceptValues);
                        attributesTempArray.add(newAttribute);

                        System.out.println(task.get(StaticFunctions.ID).asText(""));
                        ObjectNode editEntity = Json.newObject();
                        editEntity.set(StaticFunctions.ATTRIBUTES, attributesTempArray);
                        hs.editEntity(editEntity, task.get(StaticFunctions.ID).asText("")).toCompletableFuture().join();
                    }
                }
            });
            return ok();
        }).toCompletableFuture().join();

        return ok();
    }

    private void basicStringMatch(String text, Map<String, String> conceptIdMap, List<String> conceptsList, ArrayNode newConceptValues) {
        for(Map.Entry<String, String> entry : conceptIdMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if(!conceptsList.contains(key) && text.toLowerCase().contains(key.toLowerCase())) {
                ObjectNode jo = Json.newObject();
                jo.put(StaticFunctions.ID, value);
                newConceptValues.add(jo);
            }
        }
    }

    private ArrayNode createEntityIfNotNull(String sf, ArrayNode attrA, String typeId, HelperService hs, Map<String, String> conceptIdMap) {
        ArrayNode result = Json.newArray();
        if(conceptIdMap.containsKey(sf.toLowerCase())) {
            result.add(conceptIdMap.get(sf));
        } else {
            ObjectNode newEntity = Json.newObject();
            newEntity.put(StaticFunctions.NAME, sf);

            ObjectNode ws = Json.newObject();
            ws.put(StaticFunctions.ID, StaticFunctions.WORKSPACEID);
            newEntity.set(StaticFunctions.WORKSPACE, ws);

            ObjectNode type = Json.newObject();
            type.put(StaticFunctions.ID, typeId);
            newEntity.set(StaticFunctions.ENTITYTYPE, type);

            if(attrA != null) {
                newEntity.set(StaticFunctions.ATTRIBUTES, attrA);
            }

            hs.createEntity(newEntity).thenApply(res -> {
                conceptIdMap.put(sf, res.get(StaticFunctions.ID).asText(""));
                result.add(res.get(StaticFunctions.ID).asText(""));
                return ok();
            }).toCompletableFuture().join();
        }
        return result;
    }


    public Result updateKeywordsForDesignDecisions() {
        HelperService hs = new HelperService(ws);
        TextAnalysisClient tac = new TextAnalysisClient();
        Map<String, String> conceptIdMap = getAllExistingConcepts(hs, StaticFunctions.KEYWORDSID);

        hs.executeMxl(StaticFunctions.WORKSPACEID, "getAllDesignDecisions()").thenApply(tasks -> {
            tasks.get("value").forEach(task -> {
                JsonNode ca = task.get(StaticFunctions.KEYWORDS);
                System.out.println(ca);
                if(ca.size() == 0) {
                    String summary = task.get(StaticFunctions.SUMMARY).asText("").trim().replaceAll(" +", " ").toLowerCase();
                    String description = task.get(StaticFunctions.DESCRIPTION).asText("").trim().replaceAll(" +", " ").toLowerCase();
                    List<String> keywordsList = new ArrayList<>();

                    List<Entity> entities = tac.extractKeywords(summary + " " + description);
                    System.out.println(entities);

                    ArrayNode newKeywordValues = Json.newArray();
                    for (Entity entity : entities) {
                        for (String sf : entity.getSurfaceForms()) {
                            if (!keywordsList.contains(sf)) {
                                String id = createEntityIfNotNull(sf, null, StaticFunctions.KEYWORDSID, hs, conceptIdMap).get(0).asText("");
                                if(id!=null) {
                                    ObjectNode jo = Json.newObject();
                                    jo.put(StaticFunctions.ID, id);
                                    newKeywordValues.add(jo);
                                    keywordsList.add(sf);
                                }
                            }
                        }
                    }

                    basicStringMatch(summary + " " + description, conceptIdMap, keywordsList, newKeywordValues);

                    ArrayNode attributesTempArray = Json.newArray();
                    if(newKeywordValues.size() > 0) {
                        ObjectNode newAttribute = Json.newObject();
                        newAttribute.put(StaticFunctions.NAME, StaticFunctions.KEYWORDS);
                        newAttribute.set(StaticFunctions.VALUES, newKeywordValues);
                        attributesTempArray.add(newAttribute);
                        System.out.println(task.get(StaticFunctions.ID).asText(""));
                        ObjectNode editEntity = Json.newObject();
                        editEntity.put(StaticFunctions.ATTRIBUTES, attributesTempArray);
                        hs.editEntity(editEntity, task.get("id").asText(""));
                    }
                }
            });
            return ok();
        }).toCompletableFuture().join();

        return ok();
    }

}
