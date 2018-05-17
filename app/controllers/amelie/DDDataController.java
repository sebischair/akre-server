package controllers.amelie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import model.amelie.Issue;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.StaticFunctions;

/**
 * Created by Manoj on 11/28/2017.
 */
public class DDDataController  extends Controller {

    public Result getDDData() {
        JsonNode request = request().body().asJson();
        String projectKey = request.findValue("projectKey").toString().replaceAll("\"", "");
        String viz = request.findValue("viz").toString().replaceAll("\"", "");
        String attrName = request.findValue("attrName").toString().replaceAll("\"", "");
        String segmentName = "";
        if(request.has("segmentName")) {
            segmentName = request.findValue("segmentName").toString().replaceAll("\"", "");
        }

        ArrayNode results = Json.newArray();
        Issue issueModel = new Issue();
        if(viz.equalsIgnoreCase("qa")) {
            results = issueModel.getDesignDecisionsRelatedToQA(projectKey, attrName, segmentName);
        } else if(viz.equalsIgnoreCase("ae")) {
            results = issueModel.getDesignDecisionsRelatedToAE(projectKey, attrName);
        } else {
            results = issueModel.findAllDesignDecisionsInAProject(projectKey);
        }
        if(results.size() == 0) {
            return StaticFunctions.jsonResult(ok(results.add(Json.newObject())));
        }
        return StaticFunctions.jsonResult(ok(results));
    }
}