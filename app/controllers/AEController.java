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

import java.util.*;

/**
 * Created by Manoj on 4/11/2017.
 */
public class AEController extends Controller {
    @Inject
    WSClient ws;

    public Result getAE() {
        HelperService hs = new HelperService(ws);
        Map<String, Integer> conceptMap = new HashMap<String, Integer>();
        hs.entitiesForTypeUid(StaticFunctions.SCCONCEPTSID).thenApply(concepts -> {
            concepts.forEach(c -> {
                String key = c.get(StaticFunctions.NAME).asText().replaceAll("s$", "");
                if (!conceptMap.containsKey(key)) {
                    conceptMap.put(key, 0);
                }
            });
            return ok();
        }).toCompletableFuture().join();

        hs.executeMxl(StaticFunctions.WORKSPACEID, "getConceptsOfDesignDecisions()").thenApply(tasks -> {
            tasks.get("value").forEach(task -> {
                JsonNode ca = task.get(StaticFunctions.CONCEPTS);
                ca.forEach(c -> {
                    String key = c.asText().replaceAll("s$", "");
                    if (conceptMap.containsKey(key)) {
                        int value = conceptMap.get(key) + 1;
                        conceptMap.replace(key, value);
                    }
                });
            });
            return ok();
        }).toCompletableFuture().join();

        List<String> itemsToRemove = new ArrayList<String>();
        Iterator it = conceptMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if ((Integer) pair.getValue() == 0) {
                itemsToRemove.add(pair.getKey().toString());
            }
        }

        itemsToRemove.forEach(conceptMap::remove);

        ArrayNode ja = Json.newArray();
        it = conceptMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            ObjectNode jo = Json.newObject();
            jo.put("id", pair.getKey().toString());
            jo.put("value", pair.getValue().toString());
            ja.add(jo);
        }

        return ok(ja);
    }
}