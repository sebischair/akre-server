package controllers;

import akka.stream.Materializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Paragraph;
import model.Record;
import play.Configuration;
import play.Environment;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import util.StaticFunctions;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;


public class SpaCyController extends Controller{
    @Inject
    WSClient ws;

    @Inject
    private Materializer materializer;

    private Configuration configuration;

    private final String PARAGRAPH_NUMBER = "parNum";
    private final String CONTENT = "content";
    private final String DOCUMENT_HASH = "docHash";
    private final String PARAGRAPH_MAX = "parMax";
    private final String SESSION = "uuid";

    public CompletionStage<Result> process(String paraghraphHash) {
        Environment env;
        env = Environment.simple();
        configuration = Configuration.load(env);
        String url = configuration.getString("spacy.host");

        JsonNode json = Json.newObject()
                .put("text",  Paragraph.getParagraph(paraghraphHash).getContent());
        return ws.url(url).post(json).thenApply(response ->{
                ObjectNode responseWithHash = (ObjectNode) response.asJson();
                responseWithHash.put("paragraphHash", paraghraphHash);
                return StaticFunctions.jsonResult(ok(responseWithHash.toString()));
           }
        );
    }

    //temporal solution for the demo, similar to processDocument()
    public CompletionStage<Result> annotate() {
        Environment env;
        env = Environment.simple();
        configuration = Configuration.load(env);
        String url = configuration.getString("spacy.host");
        JsonNode request = request().body().asJson();
        if (request.has(SESSION) && request.has(PARAGRAPH_NUMBER) && request.has(DOCUMENT_HASH)) {
            Record record = Record.getOrCreateRecord(request.findValue(SESSION).asText(), request.findValue(DOCUMENT_HASH).asText());
            if (!(request.has(PARAGRAPH_MAX) && request.findValue(PARAGRAPH_MAX).asInt() == record.getParagraphs().size())) {
                int paragraphNumber = request.findValue(PARAGRAPH_NUMBER).asInt();
                Paragraph paragraph = new Paragraph().setParagraphNum(paragraphNumber);
                paragraph.setContent(request.findValue(CONTENT).toString().replace("\"", ""));
                record.addParagraph(paragraph).save();
            }
        }
        String content = request.findValue(CONTENT).toString().replace("\"", "");
        JsonNode json = Json.newObject()
                .put("text", content);
        return ws.url(url).post(json).thenApply(response ->{
                    ArrayNode flattenedArray = Json.newArray();
                    ObjectNode responseModified = Json.newObject();
                    for (JsonNode sen: response.asJson().get("sentences") ){
                        for (int i = 0; i < sen.size(); i++){
                            flattenedArray.add(sen.get(i));
                        }
                    }
                    responseModified.put("data", flattenedArray);
                    return StaticFunctions.jsonResult(ok(responseModified.toString()));
                }
        );
    }
}
