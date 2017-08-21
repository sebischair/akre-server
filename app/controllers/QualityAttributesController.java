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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Manoj on 8/8/2017.
 */
public class QualityAttributesController extends Controller {
    @Inject
    WSClient ws;

    public Result updateTaskWithQA(String projectId) {
        System.out.println("request to update Tasks");
        HelperService hs = new HelperService(ws);
        Map<String, List<String>> qa_id_keywords = new HashMap<>();
        try {
            hs.entitiesForTypeUid(StaticFunctions.QUALITYATTRIBUTEID).thenApply(concepts -> {
                concepts.forEach(c -> {
                    String key = c.get(StaticFunctions.NAME).asText().toLowerCase().replaceAll("s$", "");
                    String id = c.get(StaticFunctions.ID).asText();
                    List<String> keywords = new ArrayList<>();
                    keywords.add(key);
                    hs.entityForUid(id).thenApply(qa -> {
                        qa.get("attributes").forEach(attr -> {
                            if (attr.get("name").asText("").equalsIgnoreCase("keyword") && attr.get("values").size() > 0) {
                                attr.get("values").forEach(a -> keywords.add(a.get("name").asText("").toLowerCase()));
                            }
                        });
                       return ok();
                    }).toCompletableFuture().join();

                    if (!qa_id_keywords.containsKey(id)) {
                        qa_id_keywords.put(id, keywords);
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
                            summary = attr.get("values").get(0).asText("");
                        } else if(attr.get("name").asText("").equalsIgnoreCase("description") && attr.get("values").size() > 0) {
                            description = attr.get("values").get(0).asText("");
                        } else if(attr.get("name").asText("").equalsIgnoreCase("design decision") && attr.get("values").size() > 0) {
                            isDesignDecision = attr.get("values").get(0).asBoolean(false);
                        } else if(attr.get("name").asText("").equalsIgnoreCase("belongs_to") && attr.get("values").size() > 0) {
                            belongsToProject = attr.get("values").get(0).get("id").asText().equalsIgnoreCase(projectId);
                        }
                    }

                    if(isDesignDecision && belongsToProject) {
                        ArrayNode qaList = getQAList(summary + " " + description, qa_id_keywords);
                        ArrayNode newAttributes = Json.newArray();
                        if (qaList.size() > 0) {
                            StaticFunctions.updateAttributesArray(newAttributes, qaList, "qualityAttributes");
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

    private ArrayNode getQAList(String text, Map<String, List<String>> qa_id_keywords) {
        ArrayNode result = Json.newArray();
        for (Map.Entry<String, List<String>> entry : qa_id_keywords.entrySet()) {
            List<String> keywords = entry.getValue();
            for(String keyword: keywords) {
                if(text.toLowerCase().contains(keyword)) {
                    result.add(entry.getKey());
                    break;
                }
            }
        }
        return result;
    }
}
