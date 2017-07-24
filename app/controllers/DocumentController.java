package controllers;

import akka.stream.Materializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import model.Document;
import model.Paragraph;
import model.Record;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.StatusHeader;
import services.pipeline.DefaultPipeline;
import util.StaticFunctions;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

public class DocumentController extends Controller {

    @Inject
    WSClient ws;

    @Inject
    private Materializer materializer;

    private JsonNode metaInformation = Json.newObject();

    private final String PARAGRAPH_NUMBER = "parNum";
    private final String CONTENT = "content";
    private final String SPACY_SERVER_ERROR = "[{}] server error: {}";
    private final String DOCUMENT_HASH = "docHash";
    private final String PARAGRAPH_MAX = "parMax";
    private final String SESSION = "uuid";

    private final String ANNOTATION_TYPE = "annotationType";
    private final String TAGS = "tags";

    private void getAnnotations(String type, ArrayNode annotations, Configuration configuration, JsonNode request){
        String content = request.findValue(CONTENT).toString().replace("\"", "");
        switch (type){
            case "uncertainty":
                String url = configuration.getString("spacy.host");
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
    private void dbpediaDocAnnotations(ArrayNode annotations, String content){
        Document d = new Document(content);
        DefaultPipeline dp = new DefaultPipeline();
        dp.setDocument(d);
        annotations.addAll(dp.processDocument());
    }

    public Result processDocument() {
        ObjectNode result = Json.newObject();
        JsonNode request = request().body().asJson();
        ArrayNode annotations = Json.newArray();
        //TODO: Move to the filter
//        if (request.has(SESSION) && request.has(PARAGRAPH_NUMBER) && request.has(DOCUMENT_HASH)) {
//            Record record = Record.getOrCreateRecord(request.findValue(SESSION).asText(), request.findValue(DOCUMENT_HASH).asText());
//            if (request.has(PARAGRAPH_MAX) && request.findValue(PARAGRAPH_MAX).asInt() == record.getParagraphs().size()) {
//                //document already in db
//            } else {
//                int paragraphNumber = request.findValue(PARAGRAPH_NUMBER).asInt();
//                Paragraph paragraph = new Paragraph().setParagraphNum(paragraphNumber);
//                paragraph.setContent(request.findValue(CONTENT).toString().replace("\"", ""));
//                record.addParagraph(paragraph).save();
//            }
//        }
        Configuration configuration = Configuration.root();
        if (request.has(ANNOTATION_TYPE)) {
            ArrayNode annotationType = (ArrayNode) request.findValue(ANNOTATION_TYPE);
            for (int i = 0; i < annotationType.size(); i++) {
                getAnnotations(annotationType.get(i).asText(""), annotations, configuration, request);
            }
        } else {
            getAnnotations("", annotations, configuration, request);
        }

        result.put("status", "OK");
        result.set("data", annotations);

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
        if (wsResponse.has(key))
            return wsResponse.get(key);
        if (wsResponse.isObject()) {
            for (Iterator iterator = wsResponse.fieldNames(); iterator.hasNext(); ) {
                String k = (String) iterator.next();
                return getResource(wsResponse.get(k), key);
            }
        }
        return null;
    }

    public CompletionStage<JsonNode> getResponse(String url) {
        return ws.url(url).get().thenApply(WSResponse::asJson);
    }
}
