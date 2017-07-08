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
import java.util.List;

/**
 * Created by mahabaleshwar on 7/7/2017.
 */
public class AssigneeController extends Controller {
    @Inject
    WSClient ws;

    public Result getAssignee() {
        ArrayNode ja = Json.newArray();
        HelperService hs = new HelperService(ws);
        List<String> assigneeList = new ArrayList<>();

        hs.executeMxl(StaticFunctions.WORKSPACEID, "getConceptsOfDesignDecisions()").thenApply(tasks -> {
            tasks.get(StaticFunctions.VALUE).forEach(task -> {
                if (task.has(StaticFunctions.ASSIGNEE) && task.get(StaticFunctions.ASSIGNEE) != null) {
                    String assignee = task.get(StaticFunctions.ASSIGNEE).asText("").toLowerCase();
                    if (!assigneeList.contains(assignee)) {
                        assigneeList.add(assignee);
                    }
                }
            });

            assigneeList.forEach(assignee -> {
                if (!StaticFunctions.containsStringValue("personName", assignee, ja)) {
                    ObjectNode jo = Json.newObject();
                    jo.put("personName", assignee.toLowerCase());
                    jo.put("concepts", Json.newArray());
                    ja.add(jo);
                }
            });

            tasks.get(StaticFunctions.VALUE).forEach(task -> {
                String assignee = task.get(StaticFunctions.ASSIGNEE).asText("");
                JsonNode ca = task.get(StaticFunctions.CONCEPTS);
                JsonNode personObject = StaticFunctions.getJSONObject("personName", assignee, ja);
                JsonNode conceptArray = personObject.get(StaticFunctions.CONCEPTS);
                ca.forEach(concept -> {
                    String key = concept.asText("").replaceAll("s$", "");
                    StaticFunctions.updateConceptArray(key.toLowerCase(), conceptArray);
                });
            });
            StaticFunctions.removeItemsFromJSONArray(ja, StaticFunctions.getItemsToRemove(ja));

            return ok();
        }).toCompletableFuture().join();

        return ok(transformArrayForD3(ja));
    }

    private ArrayNode transformArrayForD3(ArrayNode ja) {
        ArrayNode newJA = Json.newArray();
        ja.forEach(jo -> {
            JsonNode ca = jo.get(StaticFunctions.CONCEPTS);
            ca.forEach(co -> {
                ObjectNode newJO = Json.newObject();
                newJO.put("personName", jo.get("personName"));
                newJO.put("conceptName", co.get("conceptName"));
                newJO.put("value", co.get("value"));
                newJA.add(newJO);
            });
        });
        return newJA;
    }
}
