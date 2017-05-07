package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import model.Document;
import model.Paragraph;
import model.Record;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import services.pipeline.DefaultPipeline;
import util.StaticFunctions;

import java.util.Iterator;
import java.util.concurrent.CompletionStage;

public class DocumentController extends Controller {

    @Inject
    WSClient ws;

    private JsonNode metaInformation = Json.newObject();

    private final String PARAGRAPH_NUMBER = "parNum";
    private final String CONTENT = "content";
    private final String DOCUMENT_HASH = "docHash";
    private final String PARAGRAPH_MAX = "parMax";
    private final String SESSION = "uuid";

    public Result processDocument() {
        ObjectNode result = Json.newObject();
        JsonNode request = request().body().asJson();

        if (request.has(SESSION) && request.has(PARAGRAPH_NUMBER) && request.has(DOCUMENT_HASH)) {
            Record record = Record.getRecord(request.findValue(SESSION).asText(), request.findValue(DOCUMENT_HASH).asText());
            if (request.has(PARAGRAPH_MAX) && request.findValue(PARAGRAPH_MAX).asInt() == record.getParagraphs().size()) {
                //document already in db
            } else {
                int paragraphNumber = request.findValue(PARAGRAPH_NUMBER).asInt();
                Paragraph paragraph = new Paragraph().setParagraphNum(paragraphNumber);
                paragraph.setContent(request.findValue(CONTENT).toString().replace("\"", ""));
                record.addParagraph(paragraph).save();
            }
        }

        String content = request.findValue(CONTENT).toString().replace("\"", "");
        Document d = new Document(content);

        DefaultPipeline dp = new DefaultPipeline();
        dp.setDocument(d);
        ArrayNode annotations = dp.processDocument();

        result.put("status", "OK");
        result.replace("data", annotations);

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
