package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Paragraph;
import play.Configuration;
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

    public CompletionStage<Result> process(String paraghraphHash) {
        Configuration configuration = Configuration.root();
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
}
