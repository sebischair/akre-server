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
import java.util.Comparator;
import java.util.List;

/**
 * Created by Manoj on 7/7/2017.
 */
public class AssigneeController extends Controller {

    public Result getAssignee(String projectKey) {
        return ok(getExpertiseMatrix(projectKey));
    }

    public ArrayNode getExpertiseMatrix(String projectKey) {
        ArrayNode ja = Json.newArray();
        List<String> assigneeList = new ArrayList<>();
        Issue issueModel = new Issue();
        ArrayNode issues = issueModel.findAllIssuesInAProject(projectKey);

        issues.forEach(issue-> {
            if(issue.has(StaticFunctions.ASSIGNEE) && issue.get(StaticFunctions.ASSIGNEE) != null) {
                String assignee = issue.get(StaticFunctions.ASSIGNEE).asText("").toLowerCase();
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

        issues.forEach(issue-> {
            if(issue.has(StaticFunctions.ASSIGNEE)) {
                String assignee = issue.get(StaticFunctions.ASSIGNEE).asText("");
                JsonNode ca = issue.get(StaticFunctions.CONCEPTS);
                JsonNode personObject = StaticFunctions.getJSONObject("personName", assignee, ja);
                JsonNode conceptArray = personObject.get(StaticFunctions.CONCEPTS);
                if(ca !=null && ca.isArray()) {
                    ca.forEach(concept -> {
                        String key = concept.asText("");
                        if(key!= null) {
                            StaticFunctions.updateConceptArray(key.toLowerCase(), conceptArray);
                        }
                    });
                }
            }
        });

        StaticFunctions.removeItemsFromJSONArray(ja, StaticFunctions.getItemsToRemove(ja));
        return transformArrayForD3(ja);
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
                //if(co.get("value").asInt() > 10)
                    newJA.add(newJO);
            });
        });
        return newJA;
    }

    public Result getUniqueAssignees(String projectKey) {
        ArrayNode ja = Json.newArray();
        List<String> assigneeList = new ArrayList<>();
        Issue issueModel = new Issue();
        ArrayNode issues = issueModel.findAllIssuesInAProject(projectKey);
        issues.forEach(issue-> {
            if(issue.has(StaticFunctions.ASSIGNEE) && issue.get(StaticFunctions.ASSIGNEE) != null) {
                String assignee = issue.get(StaticFunctions.ASSIGNEE).asText("").toLowerCase();
                if (!assigneeList.contains(assignee)) {
                    assigneeList.add(assignee);
                    ja.add(assignee);
                }
            }
        });
        return ok(ja);
    }

    public Result getExpertForATopic() {
        JsonNode request = request().body().asJson();
        ObjectNode result = Json.newObject();
        if(request.has("project-key") && request.has("topic") && request.get("topic").isTextual()) {
            ArrayNode expertArray = Json.newArray();
            String projectKey = request.get("project-key").asText("");
            String topic = request.get("topic").asText("").toLowerCase();
            ArrayNode em = getExpertiseMatrix(projectKey);
            em.forEach(eo -> {
                if(eo.get("conceptName").asText("").equalsIgnoreCase(topic) || topic.contains(eo.get("conceptName").asText(""))) {
                    ObjectNode expertObject = Json.newObject();
                    expertObject.put("name", eo.get("personName").asText(""));
                    expertObject.put("score", eo.get("value").asInt(0));
                    expertArray.add(expertObject);
                }
            });
            result.put("status", "OK");
            result.put("statusCode", "200");
            result.set("data", sortJsonArray(expertArray));
        } else {
            result.put("status", "Bad request - missing request parameters");
            result.put("statusCode", "400");
        }
        return ok(result);
    }

    public ArrayNode sortJsonArray(ArrayNode arrayNode) {
        ArrayNode sortedJsonArray = Json.newArray();
        List<JsonNode> jsonValues = new ArrayList<>();
        for (int i = 0; i < arrayNode.size(); i++) {
            jsonValues.add(arrayNode.get(i));
        }

        jsonValues.sort(new Comparator<JsonNode>() {
            private static final String KEY_NAME = "score";

            @Override
            public int compare(JsonNode a, JsonNode b) {
                try {
                    if (a.get(KEY_NAME) != null && b.get(KEY_NAME) != null) {
                        double valA = a.get(KEY_NAME).asDouble();
                        double valB = b.get(KEY_NAME).asDouble();
                        if (valB == valA) {
                            return 0;
                        } else if (valB > valA) {
                            return 1;
                        } else {
                            return -1;
                        }
                    } else if (a.get(KEY_NAME) == null) {
                        return 1;
                    } else {
                        return -1;
                    }
                } catch (Exception e) {
                    return -1;
                }
            }
        });

        for (int i = 0; i < arrayNode.size(); i++) {
            sortedJsonArray.add(jsonValues.get(i));
        }
        return sortedJsonArray;
    }
}
