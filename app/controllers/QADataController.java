package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import services.HelperService;
import util.StaticFunctions;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Manoj on 5/18/2017.
 */
public class QADataController extends Controller {
    @Inject
    WSClient ws;

    public Result getQAData(String projectId) {
        ArrayNode ja = Json.newArray();
        HelperService hs = new HelperService(ws);
        hs.entitiesForTypeUid(StaticFunctions.QUALITYATTRIBUTEID).thenCompose(qualityAttributes -> hs.executeMxl(StaticFunctions.WORKSPACEID, "QADDCount(\"" + projectId + "\")").thenApply(values -> {
            for (int i = 0; i < qualityAttributes.size(); i++) {
                ObjectNode jo = Json.newObject();
                jo.put("id", qualityAttributes.get(i).get("name").asText(""));
                jo.put("value", values.get("value").get(i).asInt(0));
                ja.add(jo);
            }
            return ok();
        })).toCompletableFuture().join();
        return ok(ja);
    }

    public Result getConQAData(String projectId) {
        ArrayNode ja = Json.newArray();
        HelperService hs = new HelperService(ws);
        String[] qa = {"Portability", "Maintainability", "Usability", "Reliability", "Efficiency", "Functionality"};
        //String[] qaId = {"5ljd45ugytp", "4joak01wzwf9", "19wffpavczg4a", "186l7sbqvpozc", "1wjn9ho9rxvgo", "1toyur9yf4udg"};
        List qualityAttributes = Arrays.asList(qa);
        hs.executeMxl(StaticFunctions.WORKSPACEID, "ConsolidatedQADDCount(\"" + projectId + "\")").thenApply(values -> {
            for (int i = 0; i < qualityAttributes.size(); i++) {
                ObjectNode jo = Json.newObject();
                jo.put("id", qualityAttributes.get(i).toString());
                jo.put("value", values.get("value").get(i).asInt());
                ja.add(jo);
            }
            return ok();
        }).toCompletableFuture().join();
        return ok(ja);
    }
}
