package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import model.Document;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import services.pipeline.DefaultPipeline;
import util.StaticFunctions;

import java.util.Iterator;
import java.util.concurrent.CompletionStage;

/**
 * Created by mahabaleshwar on 6/23/2016.
 */
public class DocumentController extends Controller {

    @Inject WSClient ws;

    private JsonNode metaInformation = Json.newObject();

    public Result processDocument() {
        ObjectNode result = Json.newObject();

        String content = request().body().asJson().findValue("content").toString().replace("\"", "");
        //String content = "The Yummy Inc online application will be deployed onto a J2EE application server Websphere Application Server version 6, as it is already the application server use for internal applications. J2EE security model will be reused. Data persistence will be addressed using a relational database.";
        Document d = new Document(content);

        DefaultPipeline dp = new DefaultPipeline();
        dp.setDocument(d);
        ArrayNode annotations = dp.processDocument();

        result.put("status", "OK");
        result.put("data", annotations);

        return ok(result);
    }

    public Result getMetaInformation() {
        String uri = request().body().asJson().findValue(StaticFunctions.URI).toString().replace("\"", "");
        //String url = "http://dbpedia.org/data/Relational_database.json";
        //String key = "http://dbpedia.org/resource/Relational_database"
        CompletionStage<JsonNode> jsonResponse = getResponse(uri.replace("resource", "data") + ".json");
        jsonResponse.thenApply(wsResponse -> {
            metaInformation = getResource(wsResponse, uri);
            return ok();
        }).toCompletableFuture().join();
        return ok(metaInformation);
    }

    public JsonNode getResource(JsonNode wsResponse, String key) {
        if(wsResponse.has(key))
            return wsResponse.get(key);
        if(wsResponse.isObject()) {
            for(Iterator iterator = wsResponse.fieldNames(); iterator.hasNext();) {
                String k = (String) iterator.next();
                return getResource(wsResponse.get(k), key);
            }
        }
        return null;
    }

    public CompletionStage<JsonNode> getResponse(String url) {
        return ws.url(url).get().thenApply(WSResponse:: asJson);
    }
}
