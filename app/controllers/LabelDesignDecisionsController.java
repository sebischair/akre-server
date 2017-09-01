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
                System.out.println(id);
                String summary = "";
                String description = "";
                boolean belongsToProject = false;
                boolean isLabeledAsDesignDecision = false;
                boolean isDecisionCategoryLabeled = false;

                ArrayNode attributes = (ArrayNode) issue.get(StaticFunctions.ATTRIBUTES);
                for(int j=0; j<attributes.size(); j++) {
                    JsonNode attr = attributes.get(j);
                    if(attr.get(StaticFunctions.NAME).asText("").equalsIgnoreCase(StaticFunctions.SUMMARY) && attr.get(StaticFunctions.VALUES).size() > 0) {
                        summary = attr.get(StaticFunctions.VALUES).get(0).asText("");
                    } else if(attr.get(StaticFunctions.NAME).asText("").equalsIgnoreCase(StaticFunctions.DESCRIPTION) && attr.get(StaticFunctions.VALUES).size() > 0) {
                        description = attr.get(StaticFunctions.VALUES).get(0).asText("");
                    } else if(attr.get(StaticFunctions.NAME).asText("").equalsIgnoreCase(StaticFunctions.BELONGSTO) && attr.get(StaticFunctions.VALUES).size() > 0) {
                        belongsToProject = attr.get(StaticFunctions.VALUES).get(0).get(StaticFunctions.ID).asText().equalsIgnoreCase(projectId);
                    } else if(attr.get(StaticFunctions.NAME).asText("").equalsIgnoreCase(StaticFunctions.DESIGNDECISION) && attr.get(StaticFunctions.VALUES).size() > 0) {
                        isLabeledAsDesignDecision = true;
                    } else if(attr.get(StaticFunctions.NAME).asText("").equalsIgnoreCase(StaticFunctions.DECISIONCATEGORY) && attr.get(StaticFunctions.VALUES).size() > 0) {
                        isDecisionCategoryLabeled = true;
                    }
                }

                ArrayNode newAttributes = Json.newArray();

                if(belongsToProject && !isLabeledAsDesignDecision) {
                    String label = getDesignDecisionLabel(summary + " " + description).get(StaticFunctions.LABEL).toString();

                    ObjectNode attribute = Json.newObject();
                    ArrayNode valueNodes = Json.newArray();
                    attribute.put(StaticFunctions.NAME, StaticFunctions.DESIGNDECISION);
                    if (label.equals("1")) {
                        valueNodes.add(true);
                    } else {
                        isDecisionCategoryLabeled = true;
                        valueNodes.add(false);
                    }
                    attribute.set(StaticFunctions.VALUES, valueNodes);
                    newAttributes.add(attribute);
                }

                if(belongsToProject && !isDecisionCategoryLabeled) {
                    String label = getDecisionCategoryLabel(summary + " " + description, dcMap).get(StaticFunctions.LABEL).asText();
                    ObjectNode attribute = Json.newObject();
                    ArrayNode valueNodes = Json.newArray();
                    attribute.put(StaticFunctions.NAME, StaticFunctions.DECISIONCATEGORY);

                    ObjectNode valueObject = Json.newObject();
                    valueObject.put(StaticFunctions.ID, label);
                    valueNodes.add(valueObject);

                    attribute.set(StaticFunctions.VALUES, valueNodes);
                    newAttributes.add(attribute);
                }

                if(newAttributes.size() > 0) {
                    ObjectNode editEntity = Json.newObject();
                    editEntity.set(StaticFunctions.ATTRIBUTES, newAttributes);
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
