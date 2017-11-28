package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import services.HelperService;
import util.StaticFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Manoj on 8/8/2017.
 */
public class ArchitecturalElementsController extends Controller {
    @Inject
    WSClient ws;

    public Result updateTaskWithAE(String projectId) {
        System.out.println("request to update Tasks with AEs");
        HelperService hs = new HelperService(ws);

        Map<String, String> concepts_id_map = new HashMap<>();
        try {
            hs.entitiesForTypeUid(StaticFunctions.SCCONCEPTSID).thenApply(concepts -> {
                concepts.forEach(c -> {
                    String key = c.get(StaticFunctions.NAME).asText().toLowerCase();
                    String id = c.get(StaticFunctions.ID).asText();
                    if (!concepts_id_map.containsKey(key)) {
                        concepts_id_map.put(key, id);
                    }
                });
                return ok();
            }).toCompletableFuture().join();

            List<String> issueIds = new ArrayList<>();
            hs.entitiesForTypeUid(StaticFunctions.TASKID).thenApply(issues -> {
                issues.forEach(issue -> issueIds.add(issue.get(StaticFunctions.ID).asText()));
                return ok();
            }).toCompletableFuture().join();

            issueIds.forEach(id -> {
                hs.entityForUid(id).thenApply(issue -> {
                    String summary = "";
                    String description = "";
                    boolean belongsToProject = false;
                    boolean isDesignDecision = false;

                    ArrayNode attributes = (ArrayNode) issue.get(StaticFunctions.ATTRIBUTES);
                    for(int j=0; j<attributes.size(); j++) {
                        JsonNode attr = attributes.get(j);
                        if(attr.get(StaticFunctions.NAME).asText("").equalsIgnoreCase(StaticFunctions.SUMMARY) && attr.get(StaticFunctions.VALUES).size() > 0) {
                            summary = attr.get(StaticFunctions.VALUES).get(0).asText("");
                        } else if(attr.get(StaticFunctions.NAME).asText("").equalsIgnoreCase(StaticFunctions.DESCRIPTION) && attr.get(StaticFunctions.VALUES).size() > 0) {
                            description = attr.get(StaticFunctions.VALUES).get(0).asText("");
                        } else if(attr.get(StaticFunctions.NAME).asText("").equalsIgnoreCase(StaticFunctions.DESIGNDECISION) && attr.get(StaticFunctions.VALUES).size() > 0) {
                            isDesignDecision = attr.get(StaticFunctions.VALUES).get(0).asBoolean(false);
                        } else if(attr.get(StaticFunctions.NAME).asText("").equalsIgnoreCase(StaticFunctions.BELONGSTO) && attr.get(StaticFunctions.VALUES).size() > 0) {
                            belongsToProject = attr.get(StaticFunctions.VALUES).get(0).get(StaticFunctions.ID).asText().equalsIgnoreCase(projectId);
                        }
                    }

                    if(isDesignDecision && belongsToProject) {
                        ArrayNode conceptList = getConceptsList(summary + " " + description, concepts_id_map, hs);
                        ArrayNode newAttributes = Json.newArray();
                        if (conceptList.size() > 0) {
                            StaticFunctions.updateAttributesArray(newAttributes, conceptList, StaticFunctions.CONCEPTS);
                            ObjectNode editEntity = Json.newObject();
                            editEntity.set(StaticFunctions.ATTRIBUTES, newAttributes);
                            hs.editEntity(editEntity, id);
                        }
                    }
                    return ok();
                }).toCompletableFuture().join();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("AEs for tasks have been updated");
        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        return ok(result);
    }

    private ArrayNode getConceptsList(String text, Map<String, String> concepts_id_map, HelperService hs) {
        ArrayNode result = Json.newArray();
        ObjectNode request = Json.newObject();
        request.put("content", text);

        Configuration config = Configuration.root();
        hs.postWSRequest(config.getString("akrec.url") + "processDocument", request).thenApply(response -> {
            ArrayNode annotations = (ArrayNode) response.get("data");
            List<String> tokens = new ArrayList<>();
            annotations.forEach(annotation -> {
                String conceptName = annotation.get("token").asText("").toLowerCase();
                if(!tokens.contains(conceptName)) {
                    tokens.add(conceptName);
                    if (concepts_id_map.containsKey(conceptName) && !result.has(concepts_id_map.get(conceptName))) {
                        result.add(concepts_id_map.get(conceptName));
                    } else {
                        //create Concept
                        Logger.info("Creating a new Concept..");
                        ObjectNode entity = Json.newObject();

                        ObjectNode wsObject = Json.newObject();
                        wsObject.put(StaticFunctions.ID, StaticFunctions.WORKSPACEID);
                        entity.set(StaticFunctions.WORKSPACE, wsObject);
                        ObjectNode typeObject = Json.newObject();
                        typeObject.put(StaticFunctions.ID, StaticFunctions.SCCONCEPTSID);
                        entity.set(StaticFunctions.ENTITYTYPE, typeObject);
                        entity.put(StaticFunctions.NAME, conceptName);

                        ArrayNode attributes = Json.newArray();
                        ObjectNode uriAttribute = Json.newObject();
                        uriAttribute.put(StaticFunctions.NAME, StaticFunctions.URI);
                        ArrayNode uriValues = Json.newArray();
                        uriAttribute.put(StaticFunctions.VALUES, uriValues.add(annotation.get("URI").asText("")));
                        attributes.add(uriAttribute);

                        ObjectNode ctAttribute = Json.newObject();
                        ctAttribute.put(StaticFunctions.NAME, StaticFunctions.CONCEPTTYPE);
                        ArrayNode ctValues = Json.newArray();
                        ctAttribute.put(StaticFunctions.VALUES, ctValues.add(annotation.get(StaticFunctions.CONCEPTTYPE).asText("")));
                        attributes.add(ctAttribute);
                        entity.set(StaticFunctions.ATTRIBUTES, attributes);

                        hs.createEntity(entity).thenApply(res -> {
                            concepts_id_map.put(conceptName, res.get(StaticFunctions.ID).asText(""));
                            if(!result.has(concepts_id_map.get(res.get(StaticFunctions.ID).asText("").toLowerCase()))) result.add(res.get(StaticFunctions.ID).asText("").toLowerCase());
                            return ok();
                        }).toCompletableFuture().join();
                    }
                }
            });
            return ok();
        }).toCompletableFuture().join();

        return result;
    }
}
