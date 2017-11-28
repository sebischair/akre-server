package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
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
        System.out.println("request to update Tasks");
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

                    ArrayNode attributes = (ArrayNode) issue.get("attributes");
                    for(int j=0; j<attributes.size(); j++) {
                        JsonNode attr = attributes.get(j);
                        if(attr.get("name").asText("").equalsIgnoreCase("summary") && attr.get("values").size() > 0) {
                            summary = attr.get("values").get(0).asText("").trim().replaceAll(" +", " ");
                        } else if(attr.get("name").asText("").equalsIgnoreCase("description") && attr.get("values").size() > 0) {
                            description = attr.get("values").get(0).asText("").trim().replaceAll(" +", " ");
                        } else if(attr.get("name").asText("").equalsIgnoreCase("design decision") && attr.get("values").size() > 0) {
                            isDesignDecision = attr.get("values").get(0).asBoolean(false);
                        } else if(attr.get("name").asText("").equalsIgnoreCase("belongs_to") && attr.get("values").size() > 0) {
                            belongsToProject = attr.get("values").get(0).get("id").asText().equalsIgnoreCase(projectId);
                        }
                    }

                    if(isDesignDecision && belongsToProject) {
                        ArrayNode conceptList = getConceptsList(summary + " " + description, concepts_id_map, hs);
                        ArrayNode newAttributes = Json.newArray();
                        if (conceptList.size() > 0) {
                            StaticFunctions.updateAttributesArray(newAttributes, conceptList, "concepts");
                            ObjectNode editEntity = Json.newObject();
                            editEntity.set("attributes", newAttributes);
                            hs.editEntity(editEntity, id);
                        }
                    }
                    return ok();
                }).toCompletableFuture().join();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ok("OK");
    }

    private ArrayNode getConceptsList(String text, Map<String, String> concepts_id_map, HelperService hs) {
        ArrayNode result = Json.newArray();
        ObjectNode request = Json.newObject();
        request.put("content", text);

        hs.postWSRequest("http://localhost:9000/processDocument", request).thenApply(response -> {
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
                        wsObject.put("id", StaticFunctions.WORKSPACEID);
                        entity.set("workspace", wsObject);
                        ObjectNode typeObject = Json.newObject();
                        typeObject.put("id", StaticFunctions.SCCONCEPTSID);
                        entity.set("entityType", typeObject);
                        entity.put("name", conceptName);

                        ArrayNode attributes = Json.newArray();
                        ObjectNode uriAttribute = Json.newObject();
                        uriAttribute.put("name", "uri");
                        ArrayNode uriValues = Json.newArray();
                        uriAttribute.put("values", uriValues.add(annotation.get("URI").asText("")));
                        attributes.add(uriAttribute);

                        ObjectNode ctAttribute = Json.newObject();
                        ctAttribute.put("name", "conceptType");
                        ArrayNode ctValues = Json.newArray();
                        ctAttribute.put("values", ctValues.add(annotation.get("conceptType").asText("")));
                        attributes.add(ctAttribute);
                        entity.set("attributes", attributes);

                        hs.createEntity(entity).thenApply(res -> {
                            concepts_id_map.put(conceptName, res.get("id").asText(""));
                            if(!result.has(concepts_id_map.get(res.get("id").asText("").toLowerCase()))) result.add(res.get("id").asText("").toLowerCase());
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
