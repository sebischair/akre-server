package controllers.amelie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.amelie.Issue;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.StaticFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manoj on 7/7/2017.
 */
public class AssigneeController extends Controller {

    public Result getAssignee(String projectKey) {
        ArrayNode ja = Json.newArray();
        List<String> assigneeList = new ArrayList<>();
        Issue issueModel = new Issue();
        ArrayNode issues = issueModel.findAllDesignDecisionsInAProject(projectKey);
        //ArrayNode issues = issueModel.findAllIssuesInAProject(projectKey);

        issues.forEach(issue-> {
            if(issue.has(StaticFunctions.ASSIGNEE) && issue.get(StaticFunctions.ASSIGNEE) != null) {
                String assignee = issue.get(StaticFunctions.ASSIGNEE).asText("").toLowerCase();
                if (!assigneeList.contains(assignee)) {
                    assigneeList.add(assignee);
                }
            }
        });

        System.out.println(assigneeList.size());

        assigneeList.forEach(assignee -> {
            if (!StaticFunctions.containsStringValue("personName", assignee, ja)) {
                ObjectNode jo = Json.newObject();
                jo.put("personName", assignee.toLowerCase());
                jo.put("concepts", Json.newArray());
                ja.add(jo);
            }
        });

        issues.forEach(issue-> {
            if(issue.has(StaticFunctions.ASSIGNEE)) {
                String assignee = issue.get(StaticFunctions.ASSIGNEE).asText("");
                JsonNode ca = issue.get(StaticFunctions.CONCEPTS);
                JsonNode personObject = StaticFunctions.getJSONObject("personName", assignee, ja);
                JsonNode conceptArray = personObject.get(StaticFunctions.CONCEPTS);
                if(ca !=null && ca.isArray()) {
                    ca.forEach(concept -> {
                        String key = concept.asText("").replaceAll("s$", "");
                        if(key!= null) {
                            StaticFunctions.updateConceptArray(key.toLowerCase(), conceptArray);
                        }
                    });
                }
            }
        });
        StaticFunctions.removeItemsFromJSONArray(ja, StaticFunctions.getItemsToRemove(ja));

        if(ja.size() == 0) {
            return StaticFunctions.jsonResult(ok(ja.add(Json.newObject())));
        }

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
