package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
}
