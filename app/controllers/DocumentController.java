package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import model.Document;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import services.pipeline.ConceptAnnotatorPipeline;
import util.StaticFunctions;

import java.util.Iterator;
import java.util.concurrent.CompletionStage;

public class DocumentController extends Controller {

    @Inject
    WSClient ws;

    private String metaInformation;
    private final String CONTENT = "content";
    private final String SPACY_SERVER_ERROR = "[{}] server error: {}";
    private final String SPACY_HOST_MISSING = "spacy.host is not configured. Check application.conf";

    private final String ANNOTATION_TYPE = "annotationType";
    private final String TAGS = "tags";

    public void getAnnotations(String type, ArrayNode annotations, JsonNode request){
        String content = request.findValue(CONTENT).toString().replace("\"", "");
        switch (type){
            case "uncertainty":
                String url = Configuration.root().getString("spacy.host", null);
                if (url==null){
                    Logger.info(SPACY_HOST_MISSING);
                    break;
                }
                ObjectNode json = Json.newObject().put("text", content);
                if (request.has(TAGS)){
                    ArrayNode tags   = (ArrayNode) request.findValue(TAGS);
                    json.set("tags", tags);
                    ws.url(url).post(json).thenApply(response -> {
                        annotations.addAll((ArrayNode) response.asJson().get("data"));
                        return ok();
                    }).toCompletableFuture().exceptionally(th -> {
                        // will be executed when there is an exception.
                        Logger.error(SPACY_SERVER_ERROR,url,th.getMessage());
                        return null;
                    }).join();
                }
                break;
            case  "architectureRecommendations":
                dbpediaDocAnnotations(annotations, content);
                break;
            default:
                dbpediaDocAnnotations(annotations, content);
        }

    }

    public void dbpediaDocAnnotations(ArrayNode annotations, String content){
        Document d = new Document(content);
        ConceptAnnotatorPipeline dp = new ConceptAnnotatorPipeline();
        dp.setDocument(d);
        annotations.addAll(dp.processDocument());
    }

    public Result processDocument() {
        ObjectNode result = Json.newObject();
        JsonNode request = request().body().asJson();
        ArrayNode annotations = Json.newArray();
        if (request.has(ANNOTATION_TYPE)) {
            ArrayNode annotationType = (ArrayNode) request.findValue(ANNOTATION_TYPE);
            for (int i = 0; i < annotationType.size(); i++) {
                getAnnotations(annotationType.get(i).asText(""), annotations, request);
            }
        } else {
            getAnnotations("", annotations, request);
        }

        result.put("status", "OK");
        result.set("data", annotations);
        return ok(result);
    }

    public Result getMetaInformation() {
        ObjectNode res = Json.newObject();
        if(request().body().asJson().hasNonNull(StaticFunctions.URI)) {
            String uri = request().body().asJson().findValue(StaticFunctions.URI).toString().replace("\"", "");
            CompletionStage<JsonNode> jsonResponse = getResponse(uri.replace("resource", "data") + ".json");
            jsonResponse.thenApply(wsResponse -> {
                JsonNode resource = getResource(wsResponse, uri);
                ArrayNode allAbstract = (ArrayNode) resource.get("http://dbpedia.org/ontology/abstract");
                allAbstract.forEach(abst -> {
                    if(abst.hasNonNull("lang") && abst.get("lang").asText("").equalsIgnoreCase("en") && abst.hasNonNull("value")) {
                        metaInformation = abst.get("value").asText("");
                    }
                });
                return ok();
            }).toCompletableFuture().join();
            res.put("info", metaInformation);
            res.put("status", "200");
            return ok(res);
        }
        res.put("status", "400");
        return ok(res);
    }

    private JsonNode getResource(JsonNode wsResponse, String key) {
        if (wsResponse.has(key))
            return wsResponse.get(key);
        if (wsResponse.isObject()) {
            for(Iterator iterator = wsResponse.fieldNames(); iterator.hasNext(); ) {
                String k = (String) iterator.next();
                return getResource(wsResponse.get(k), key);
            }
        }
        return null;
    }

    private CompletionStage<JsonNode> getResponse(String url) {
        return ws.url(url).get().thenApply(WSResponse::asJson);
    }
}
