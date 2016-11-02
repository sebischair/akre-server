package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import model.Document;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import util.StaticFunctions;

public class CodeController extends Controller {

    @Inject WSClient ws;

    public Result processCode() {

        try {
            ObjectNode result = Json.newObject();
            String content = request().body().asJson().findValue("content").toString().replace("\"", "");
            String language = request().body().asJson().findValue("progLanguage").toString();
            //String content = "The Yummy Inc online application will be deployed onto a J2EE application server Websphere Application Server version 6, as it is already the application server use for internal applications. J2EE security model will be reused. Data persistence will be addressed using a relational database.";
            Document d = new Document(content);

            //        DefaultPipeline dp = new DefaultPipeline();
            //        dp.setDocument(d);
            //        ArrayNode annotations = dp.processDocument();

            result.put("status", "success");
            result.put("data", content);
            Logger.debug("result={}", result);
            return StaticFunctions.jsonResult(ok(result));
        } catch (Throwable t) {
            Logger.error("Exception in processCode handler", t);
            return StaticFunctions.jsonResult(badRequest(StaticFunctions.errorAsJson(t)));
        }
    }


}
