package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import model.Document;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import services.pipeline.StaticRegexPipeline;
import util.StaticFunctions;

public class CodeController extends Controller {

    @Inject WSClient ws;

    public Result processCode() {
        try {
            ObjectNode result = Json.newObject();
            String content = request().body().asJson().findValue("content").toString().replace("\"", "");
            String language = request().body().asJson().findValue("progLanguage").toString();

            StaticRegexPipeline srp = new StaticRegexPipeline();
            srp.setDocument(new Document(content));
            ArrayNode annotations = srp.processDocument();

            result.put("status", "success");
            result.put("data", annotations);
            Logger.debug("result={}", annotations);
            return StaticFunctions.jsonResult(ok(result));
        } catch (Throwable t) {
            Logger.error("Exception in processCode handler", t);
            return StaticFunctions.jsonResult(badRequest(StaticFunctions.errorAsJson(t)));
        }
    }


}
