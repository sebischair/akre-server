package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Code;
import model.Document;
import model.Project;
import org.mongodb.morphia.Key;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.pipeline.StaticRegexPipeline;
import util.StaticFunctions;

import java.sql.Timestamp;
import java.util.Date;

public class CodeController extends Controller {

    public Result createProject() {
        ObjectNode result = Json.newObject();
        String name = request().body().asJson().findValue("name").toString().replaceAll("\"", "");
        String description = "";
        if(request().body().asJson().hasNonNull("description"))
             description = request().body().asJson().findValue("description").toString().replaceAll("\"", "");
        Key<Project> project = new Project(name, description).save();
        result.put("name", name);
        result.put("description", description);
        result.put("id", project.getId().toString());
        Logger.debug("result={}", result);

        return StaticFunctions.jsonResult(ok(result));
    }



    public Result processCode() {
        try {
            ObjectNode result = Json.newObject();
            String content = request().body().asJson().findValue("content").toString().replace("\"", "");
            String progLanguage = "";
            if(request().body().asJson().hasNonNull("progLanguage"))
                progLanguage = request().body().asJson().findValue("progLanguage").toString().replace("\"", "");

            String fileName = request().body().asJson().findValue("fileName").toString().replace("\"", "");
            String projectId = request().body().asJson().findValue("projectId").toString().replace("\"", "");

            String fileId;
            if(request().body().asJson().hasNonNull("fileId")) {
                fileId = request().body().asJson().findValue("fileId").toString().replace("\"", "");
                new Code().updateCode(fileName, content, progLanguage);
            } else {
                Code code = new Code(content, progLanguage, fileName, projectId);
                Key<Code> key = code.save();
                fileId = key.getId().toString();
            }

            StaticRegexPipeline srp = new StaticRegexPipeline();
            srp.setDocument(new Document(content));
            ArrayNode annotations = srp.processDocument();
            ObjectNode meta = Json.newObject();
            meta.put("fileId", fileId);
            meta.put("timeStamp", new Timestamp(new Date().getTime()).toString());
            meta.put("projectId", projectId);

            result.put("status", "success");
            result.put("meta", meta);
            result.put("data", annotations);
            Logger.debug("result={}", annotations);
            return StaticFunctions.jsonResult(ok(result));
        } catch (Throwable t) {
            Logger.error("Exception in processCode handler", t);
            return StaticFunctions.jsonResult(badRequest(StaticFunctions.errorAsJson(t)));
        }
    }


}
