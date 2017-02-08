package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.*;
import org.mongodb.morphia.Key;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.pipeline.StaticRegexPipeline;
import util.StaticFunctions;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CodeController extends Controller {

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
            srp.setProjectId(projectId);

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

    public Result pattern(String projectId) {
        ObjectNode result = Json.newObject();
        PatternEntity p = new PatternEntity().findByProjectId(projectId);
        if(p != null) {
            result.put("status", "200");
            result.put("data", p.getJson());
        } else {
            result.put("status", "500");
            result.put("data", "Cannot retrieve patterns");
        }
        return StaticFunctions.jsonResult(ok(result));
    }

    public Result updatePattern() {
        String projectId = request().body().asJson().findValue(StaticFunctions.PROJECTID).toString().replaceAll("\"", "");
        JsonNode jsonObject = request().body().asJson();
        List<JsonNode> patterns = jsonObject.findValues(StaticFunctions.PATTERN);
        List<Regex> regex = new ArrayList<>();
        for(JsonNode p: patterns.get(0)) {
            ArrayList<String> lines = new ArrayList<String>();
            if(p.has(StaticFunctions.TAGS)) {
                ArrayNode ls = (ArrayNode) p.get(StaticFunctions.TAGS);
                for(JsonNode l: ls) {
                    lines.add(l.asText());
                }
            }

            regex.add(new Regex(p.get(StaticFunctions.NAME).asText("").replaceAll("\"", ""),
                    p.get(StaticFunctions.DESCRIPTION).asText("").replaceAll("\"", ""),
                    p.get(StaticFunctions.REGEX.toLowerCase()).asText("").replaceAll("\"", ""),
                    p.get(StaticFunctions.PROGLANGUAGE).asText("").replaceAll("\"", ""),
                    lines));
        }

        new PatternEntity().updatePattern(projectId, regex);
        return pattern(projectId);
    }
}
