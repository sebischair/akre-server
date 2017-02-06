package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.CustomCodeAnnotation;
import model.Range;
import model.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.StaticFunctions;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by mahabaleshwar on 1/22/2017.
 */
public class CCAnnotatorController extends Controller {
    public Result create() {
        ObjectNode result = Json.newObject();
        JsonNode jsonObject = request().body().asJson();
        CustomCodeAnnotation cca = new CustomCodeAnnotation();

        String projectId = jsonObject.findValue("projectId").toString().replace("\"", "");
        cca.setProjectId(projectId);
        String fileId = jsonObject.findValue("fileId").toString().replace("\"", "");
        cca.setFileId(fileId);

        if(jsonObject.has("range")) {
            cca.setRange(new Range(jsonObject.findValue("range")));
        }

        if(jsonObject.has("token")) {
            String token = jsonObject.findValue("token").toString().replace("\"", "");
            cca.setToken(token);
        }

        if(jsonObject.has("lines")) {
            ArrayNode ls = (ArrayNode) jsonObject.get("lines");
            ArrayList<String> lines = new ArrayList<String>();
            for(JsonNode l: ls) {
                lines.add(l.asText());
            }
            cca.setLines(lines);
        }

        if(jsonObject.has("path")) {
            String path = jsonObject.findValue("path").toString().replace("\"", "");
            cca.setPath(path);
        }

        if(jsonObject.has("progLanguage")) {
            String progLanguage = jsonObject.findValue("progLanguage").toString().replace("\"", "");
            cca.setPath(progLanguage);
        }

        if(jsonObject.has("description")) {
            String description = jsonObject.findValue("description").toString().replace("\"", "");
            cca.setDescription(description);
        }

        if(jsonObject.has("user")) {
            JsonNode user = jsonObject.get("user");
            cca.setUser(new User(user.get("name").asText(), user.get("mail").asText()));
        }

        cca.setCreatedAt(new Date());

        if(jsonObject.has("tags")) {
            ArrayNode ts = (ArrayNode) jsonObject.findValue("tags");
            ArrayList<String> tags = new ArrayList<String>();
            for(JsonNode t: ts) {
                tags.add(t.asText());
            }
            cca.setTags(tags);
        }

        cca.save();
        return StaticFunctions.jsonResult(ok(new CustomCodeAnnotation().searalize(cca)));
    }

    public Result getAll() {
        return StaticFunctions.jsonResult(ok(CustomCodeAnnotation.getAll()));
    }

    public Result getAllForProjectId(String projectId) {
        return StaticFunctions.jsonResult(ok(CustomCodeAnnotation.getAllAnnotationsForProject(projectId)));
    }

    public Result getAllForFileId(String fileId) {
        return StaticFunctions.jsonResult(ok(CustomCodeAnnotation.getAllAnnotationsForFile(fileId)));
    }

    public Result delete(String id) {
        ObjectNode result = Json.newObject();
        boolean status = new CustomCodeAnnotation().delete(id);
        if(status){
            result.put("status", 200);
            result.put("message", "Document successfully deleted");
        }else{
            result.put("status", 500);
            result.put("message", "Unable to delete the document");
        }
        return ok(result);
    }
}
