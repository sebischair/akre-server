package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Project;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.StaticFunctions;

import java.util.List;

/**
 * Created by mahabaleshwar on 1/22/2017.
 */
public class ProjectController extends Controller {
    public Result create() {
        ObjectNode result = Json.newObject();
        String name = request().body().asJson().findValue(StaticFunctions.NAME).toString().replaceAll("\"", "");
        String description = "";
        if(request().body().asJson().hasNonNull(StaticFunctions.DESCRIPTION))
            description = request().body().asJson().findValue(StaticFunctions.DESCRIPTION).toString().replaceAll("\"", "");
        Key<Project> project = new Project(name, description).save();
        result.put(StaticFunctions.NAME, name);
        result.put(StaticFunctions.DESCRIPTION, description);
        result.put("id", project.getId().toString());
        Logger.debug("result={}", result);
        return StaticFunctions.jsonResult(ok(result));
    }

    public Result getAll() {
        Query<Project> query = MorphiaObject.datastore.createQuery(Project.class);
        List<Project> projects= query.asList();
        ArrayNode result = Json.newArray();
        for(Project project: projects) {
            ObjectNode obj = Json.newObject();
            obj.put("id", project.get_id().toString());
            obj.put("name", project.getName());
            obj.put("description", project.getDescription());
            result.add(obj);
        }
        return StaticFunctions.jsonResult(ok(result));
    }

}
