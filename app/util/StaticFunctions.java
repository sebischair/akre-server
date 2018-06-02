package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import play.libs.Json;
import play.mvc.Result;

import java.util.ArrayList;
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
    public static final String TOKEN = "token";
    public static final String CUSTOM = "custom";
    public static final String DESCRIPTION = "description";
    public static final String CONCEPTTYPE = "conceptType";
    public static final String EXPERTISE = "expertise";
    public static final String METHOD = "knowledgeMethod";
    public static final String TEMPLATE = "template";
    public static final String CONCEPT = "CONCEPT";
    public static final String SPOTLIGHT = "SPOTLIGHT";
    public static final String NAME = "name";

    public static final String ASSIGNEE = "assignee";
    public static final String CONCEPTS = "concepts";
    public static final String SUMMARY = "summary";

    public static final String PERSONNAME= "personName";
    public static final String KEYWORDS= "keywords";
    public static final String LABEL = "label";

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

        List<JsonNode> jsonValues = new ArrayList<>();
        for (int i = 0; i < arrayNode.size(); i++) {
            jsonValues.add(arrayNode.get(i));
        }

        jsonValues.sort(new Comparator<JsonNode>() {
            private static final String KEY_NAME = "averageScore";

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

    public static boolean containsStringValue(String key, String value, ArrayNode ja) {
        for (JsonNode jo : ja) {
            if (jo.get(key).asText("").equals(value.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static JsonNode getJSONObject(String key, String value, ArrayNode ja) {
        for (JsonNode jo : ja) {
            if (jo.get(key).asText("").equals(value.toLowerCase())) {
                return jo;
            }
        }
        return null;
    }

    public static void updateConceptArray(String conceptName, JsonNode conceptArray) {
        if (conceptArray.size() == 0) {
            addConceptToConceptArray(conceptName, conceptArray);
        } else {
            boolean isUpdated = false;
            for (JsonNode conceptObject : conceptArray) {
                if (conceptObject.get("conceptName").asText("").equals(conceptName.toLowerCase())) {
                    int value = conceptObject.get("value").asInt(0);
                    ((ObjectNode) conceptObject).put("value", value + 1);
                    isUpdated = true;
                }
            }
            if (!isUpdated) {
                addConceptToConceptArray(conceptName, conceptArray);
            }
        }
    }

    private static void addConceptToConceptArray(String conceptName, JsonNode conceptArray) {
        ObjectNode jo = Json.newObject();
        jo.put("conceptName", conceptName);
        jo.put("value", 1);
        ((ArrayNode) conceptArray).add(jo);
    }

    public static List<String> getItemsToRemove(ArrayNode ja) {
        List<String> itemsToRemove = new ArrayList<>();
        itemsToRemove.add("unassigned");
        ja.forEach(jo -> {
            if (jo.get(CONCEPTS).size() == 0) {
                itemsToRemove.add(jo.get("personName").asText(""));
            }
        });
        return itemsToRemove;
    }

    public static void removeItemsFromJSONArray(ArrayNode ja, List<String> itemsToRemove) {
        for (String pName : itemsToRemove) {
            for (int i = 0; i < ja.size(); i++) {
                JsonNode jo = ja.get(i);
                if (jo.get("personName").asText("").equalsIgnoreCase(pName)) {
                    ja.remove(i);
                    break;
                }
            }
        }
    }

    public static ArrayNode getArrayNodeFromJsonNode(JsonNode obj, String attr) {
        ArrayNode arrayNode = Json.newArray();
        if(obj.get(attr) == null) {
            return arrayNode;
        }
        if(obj.has(attr) && obj.get(attr) != null && !obj.get(attr).isArray()) {
            arrayNode.add(obj.get(attr).asText(""));
        } else {
            arrayNode = (ArrayNode) obj.get(attr);
        }
        return arrayNode;
    }

    public static String truncate(String text){
        int limit = 150;
        if (text.length() > limit)
            return text.substring(0, limit) + " \u2026";
        else
            return text;
    }


    public static String cleanText(String str) {
        String result;
        if (str == null) return str;
        Document document = Jsoup.parse(str);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));// makes html() preserve linebreaks and spacing
        document.select("br").append("\\n");
        document.select("p").prepend("\\n\\n");
        result = document.html().replaceAll("\\\\n", "\n");
        result = Jsoup.clean(result, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
        result = removeUrl(result);
        result = removeExtendedChars(result);
        return result;
    }

    public static String removeUrl(String str) {
        String regex = "\\b(https?|ftp|file|telnet|http|Unsure)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        str = str.replaceAll(regex, "");
        return str;
    }

    public static String removeExtendedChars(String str) {
        return str.replaceAll("[^\\x00-\\x7F]", " ").replaceAll(" +", " ").replaceAll("[^a-zA-Z\\s]", " ");
    }
}