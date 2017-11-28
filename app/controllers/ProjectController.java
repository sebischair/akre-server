package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
        String description = "";
        if(request().body().asJson().hasNonNull(StaticFunctions.DESCRIPTION))
            description = request().body().asJson().findValue(StaticFunctions.DESCRIPTION).toString().replaceAll("\"", "");
        Project p = new Project();
        Document d = new Document("name", name).append("description", description);
        p.save(d);
        result.put(StaticFunctions.NAME, name);
        result.put(StaticFunctions.DESCRIPTION, description);
        result.put("id", p.findByName(name).getString("_id"));
        Logger.debug("result={}", result);
        return StaticFunctions.jsonResult(ok(result));
    }

    public Result getAll() {
        Project project = new Project();
        return StaticFunctions.jsonResult(ok(project.findAll()));
    }
}
