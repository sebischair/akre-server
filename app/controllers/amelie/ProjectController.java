package controllers.amelie;

import com.fasterxml.jackson.databind.node.ObjectNode;
import model.amelie.Issue;
import model.amelie.Project;
import org.bson.Document;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.StaticFunctions;

/**
 * Created by Manoj on 1/22/2017.
 */
public class ProjectController extends Controller {
    public Result create() {
        ObjectNode result = Json.newObject();
        String name = request().body().asJson().findValue(StaticFunctions.NAME).toString().replaceAll("\"", "");
        String key = request().body().asJson().findValue("key").toString().replaceAll("\"", "");
        String description = "";
        if(request().body().asJson().hasNonNull(StaticFunctions.DESCRIPTION))
            description = request().body().asJson().findValue(StaticFunctions.DESCRIPTION).toString().replaceAll("\"", "");
        Project p = new Project();
        Document d = new Document("name", name).append("description", description).append("key", key);
        p.save(d);
        result.put(StaticFunctions.NAME, name);
        result.put(StaticFunctions.DESCRIPTION, description);
        result.put("id", d.getObjectId("_id").toHexString());
        Logger.debug("result={}", result);
        return StaticFunctions.jsonResult(ok(result));
    }

    public Result getAll() {
        return StaticFunctions.jsonResult(ok(new Project().findAll()));
    }

    public Result getProjectByKey(String key) {
        return StaticFunctions.jsonResult(ok(new Project().findByKey(key)));
    }

    public Result getAllDesignDecisions(String projectKey) {
        return StaticFunctions.jsonResult(ok(new Issue().findAllDesignDecisionsInAProject(projectKey)));
    }

    public Result getDesignDecision(String issueKey) {
        return StaticFunctions.jsonResult(ok(new Issue().getDesignDecisionByKey(issueKey)));
    }
}
