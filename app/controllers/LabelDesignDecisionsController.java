package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import play.Configuration;
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
 * Created by Manoj on 8/20/2017.
 */
public class LabelDesignDecisionsController extends Controller {
    @Inject
    WSClient ws;

    HelperService hs;
    Configuration configuration;

    private void setHS(HelperService hs) {
        this.hs = hs;
    }

    private void setConf(Configuration conf) {
        this.configuration = conf;
    }

    public Result labelDesignDecisions(String projectId) {
        System.out.println("request to update design decisions");
        setHS(new HelperService(ws));
        setConf(Configuration.root());

        List<String> issueIds = new ArrayList<>();
        hs.entitiesForTypeUid(StaticFunctions.TASKID).thenApply(issues -> {
            issues.forEach(issue -> issueIds.add(issue.get(StaticFunctions.ID).asText()));
            return ok();
        }).toCompletableFuture().join();

        Map<String, String> dcMap = new HashMap<>();
        hs.entitiesForTypeUid(StaticFunctions.DECISIONCATEGORYID).thenApply(dcs -> {
            dcs.forEach(dc -> dcMap.put(dc.get(StaticFunctions.NAME).asText().toLowerCase(), dc.get(StaticFunctions.ID).asText()));
            return ok();
        }).toCompletableFuture().join();

        System.out.println(dcMap);

        issueIds.forEach(id -> {
            hs.entityForUid(id).thenApply(issue -> {
                String summary = "";
                String description = "";
                boolean belongsToProject = false;
                boolean isLabeledAsDesignDecision = false;
                boolean isDecisionCategoryLabeled = false;

                ArrayNode attributes = (ArrayNode) issue.get("attributes");
                for(int j=0; j<attributes.size(); j++) {
                    JsonNode attr = attributes.get(j);
                    if(attr.get("name").asText("").equalsIgnoreCase("summary") && attr.get("values").size() > 0) {
                        summary = attr.get("values").get(0).asText("");
                    } else if(attr.get("name").asText("").equalsIgnoreCase("description") && attr.get("values").size() > 0) {
                        description = attr.get("values").get(0).asText("");
                    } else if(attr.get("name").asText("").equalsIgnoreCase("belongs_to") && attr.get("values").size() > 0) {
                        belongsToProject = attr.get("values").get(0).get("id").asText().equalsIgnoreCase(projectId);
                    } else if(attr.get("name").asText("").equalsIgnoreCase("design decision") && attr.get("values").size() > 0) {
                        isLabeledAsDesignDecision = false;
                    } else if(attr.get("name").asText("").equalsIgnoreCase("decisionCategory") && attr.get("values").size() > 0) {
                        isDecisionCategoryLabeled = true;
                    }
                }

                ArrayNode newAttributes = Json.newArray();

                if(belongsToProject) {
                    System.out.println(id);
                    //System.out.println(summary + " " + description);

                    String label = getDesignDecisionLabel(summary + " " + description).get("label").asText();
                    System.out.println(label);

                    ObjectNode attribute = Json.newObject();
                    ArrayNode valueNodes = Json.newArray();
                    attribute.put(StaticFunctions.NAME, "design decision");
                    if (label.equalsIgnoreCase("1")) {
                        valueNodes.add(true);
                    } else {
                        isDecisionCategoryLabeled = true;
                        valueNodes.add(false);
                    }
                    attribute.set(StaticFunctions.VALUES, valueNodes);
                    newAttributes.add(attribute);
                    System.out.println(newAttributes);
                    System.out.println(".................");
                }

                if(belongsToProject) {
                    String label = getDecisionCategoryLabel(summary + " " + description, dcMap).get("label").asText();
                    ObjectNode attribute = Json.newObject();
                    ArrayNode valueNodes = Json.newArray();
                    attribute.put(StaticFunctions.NAME, "decisionCategory");

                    ObjectNode valueObject = Json.newObject();
                    valueObject.put("id", label);
                    valueNodes.add(valueObject);

                    attribute.set(StaticFunctions.VALUES, valueNodes);
                    newAttributes.add(attribute);
                }

                if(newAttributes.size() > 0) {
                    ObjectNode editEntity = Json.newObject();
                    editEntity.set("attributes", newAttributes);
                    System.out.println(editEntity);
                    hs.editEntity(editEntity, id);
                }

                return ok();
            }).toCompletableFuture().join();
        });

        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.put("message", "Design decisions are labeled!");

        return ok(result);
    }

    private ObjectNode getDesignDecisionLabel(String textToClassify) {
        ObjectNode body = Json.newObject();
        body.put("pipelineName", "DesignDecisions");
        body.put("textToClassify", textToClassify);
        ObjectNode result = Json.newObject();
        hs.postWSRequest(configuration.getString("predict.url"), body).thenApply(response -> {
            result.put("label", response.get("result").asText());
            return ok();
        }).toCompletableFuture().join();
        return result;
    }

    private ObjectNode getDecisionCategoryLabel(String textToClassify, Map<String, String> dcMap) {
        ObjectNode body = Json.newObject();
        body.put("pipelineName", "DecisionCategory");
        body.put("textToClassify", textToClassify);
        ObjectNode result = Json.newObject();
        hs.postWSRequest(configuration.getString("predict.url"), body).thenApply(response -> {
            result.put("label", dcMap.get(response.get("result").asText().toLowerCase()));
            return ok();
        }).toCompletableFuture().join();
        return result;
    }
}
