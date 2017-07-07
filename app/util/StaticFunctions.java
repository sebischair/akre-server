package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.mvc.Result;
import scala.util.parsing.json.JSONArray;
import scala.util.parsing.json.JSONObject;

import java.util.*;

/**
 * Created by mahabaleshwar on 8/29/2016.
 */
public class StaticFunctions {
    public static final String URI = "uri";
    public static final String TITLE = "title";
    public static final String SCORE = "averageScore";
    public static final String BEGIN = "begin";
    public static final String END = "end";
    public static final String TYPE = "type";
    public static final String TOKEN = "token";
    public static final String CUSTOM = "custom";
    public static final String DESCRIPTION = "description";
    public static final String CONCEPTTYPE = "conceptType";
    public static final String DBPEDIA = "dbpedia";
    public static final String EXPERTISE = "expertise";
    public static final String METHOD = "knowledgeMethod";
    public static final String TEMPLATE = "template";
    public static final String CONCEPT = "CONCEPT";
    public static final String SPOTLIGHT = "SPOTLIGHT";
    public static final String REGEX = "REGEX";
    public static final String NAME = "name";
    public static final String PROGLANGUAGE = "progLanguage";
    public static final String PATTERN = "pattern";
    public static final String PROJECTID = "projectId";
    //List of SC IDs
    public static final String WORKSPACEID = "1iksmphpafkxq";
    public static final String TASKID = "1ercja6vg99i0";
    public static final String SCCONCEPTSID = "7wpcre3lzkbo";


    public static final String ASSIGNEE = "assignee";
    public static final String CONCEPTS = "concepts";
    public static final String TAGS = "tags";

    public static Result jsonResult(Result httpResponse) {
        return httpResponse.as("application/json; charset=utf-8");
    }

    public static ObjectNode errorAsJson(Throwable error) {
        ObjectNode result = Json.newObject();
        result.put("status", "error");
        result.put("message", error.toString());
        return result;
    }

    public static ArrayNode sortJsonArray(ArrayNode arrayNode) {
        ArrayNode sortedJsonArray = Json.newArray();

        List<JsonNode> jsonValues = new ArrayList<JsonNode>();
        for (int i = 0; i < arrayNode.size(); i++) {
            jsonValues.add(arrayNode.get(i));
        }

        Collections.sort(jsonValues, new Comparator<JsonNode>() {
            private static final String KEY_NAME = "averageScore";

            @Override
            public int compare(JsonNode a, JsonNode b) {
                try {
                    if (a.get(KEY_NAME) != null && b.get(KEY_NAME) != null) {
                        double valA = a.get(KEY_NAME).asDouble();
                        double valB = b.get(KEY_NAME).asDouble();
                        if(valB == valA) {
                            return 0;
                        } else if(valB > valA) {
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

    public static ArrayNode getJsonFromList(ArrayList<String> lines) {
        ArrayNode l = Json.newArray();
        if(lines != null)
            for(String line: lines)
                l.add(line);
        return l;
    }

    public static boolean containsStringValue(String key, String value, ArrayNode ja) {
        JsonNode jsonNode;
        Iterator<JsonNode> it = ja.iterator();
        while(it.hasNext()) {
            jsonNode = it.next();
            if(jsonNode.get(key).asText("").equals(value.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static JsonNode getJSONObject(String key, String value, ArrayNode ja) {
        JsonNode jsonNode;
        Iterator<JsonNode> it = ja.iterator();
        while(it.hasNext()) {
            jsonNode = it.next();
            if(jsonNode.get(key).asText("").equals(value.toLowerCase())) {
                return jsonNode;
            }
        }
        return null;
    }

    public static void updateConceptArray(String conceptName, JsonNode conceptArray) {
        if(conceptArray.size() == 0) {
            addConceptToConceptArray(conceptName, conceptArray);
        } else {
            JsonNode jsonNode;
            boolean isUpdated = false;
            Iterator<JsonNode> it = conceptArray.iterator();
            while(it.hasNext()) {
                jsonNode = it.next();
                if(jsonNode.get("conceptName").asText("").equals(conceptName.toLowerCase())) {
                    int value = jsonNode.get("value").asInt(0);
                    ((ObjectNode) jsonNode).put("value", value+1);
                    isUpdated = true;
                }
            }
            if(!isUpdated) {
                addConceptToConceptArray(conceptName, conceptArray);
            }
        }
    }

    public static void addConceptToConceptArray(String conceptName, JsonNode conceptArray) {
        ObjectNode jo = Json.newObject();
        jo.put("conceptName", conceptName);
        jo.put("value", 1);
        ((ArrayNode) conceptArray).add(jo);
    }


    public static List<String> getItemsToRemove(ArrayNode ja) {
        List<String> itemsToRemove = new ArrayList<>();
        itemsToRemove.add("unassigned");
        ja.forEach(jo -> {
            if(jo.get(CONCEPTS).size() == 0) {
                itemsToRemove.add(jo.get("personName").asText(""));
            }
        });
        return itemsToRemove;
    }

    public static void removeItemsFromJSONArray(ArrayNode ja, List<String> itemsToRemove) {
        for(String pName : itemsToRemove) {
            for(int i=0; i<ja.size(); i++) {
                JsonNode jo = ja.get(i);
                if(jo.get("personName").asText("").equalsIgnoreCase(pName)) {
                    ja.remove(i);
                    break;
                }
            }
        }
    }
}