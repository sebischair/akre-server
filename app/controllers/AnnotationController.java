package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Annotation;
import model.DBpediaToken;
import model.Paragraph;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AnnotationController extends Controller {

    private final String OFFSETS = "offsets";
    private final String BEGIN = "begin";
    private final String END = "end";
    private final String TYPE = "type";
    private final String TOKEN = "token";
    private final String PARAGRAPH_HASH = "paragraphHash";

    public Result getAnnotationsByHash(String paragraphHash) {
        ObjectNode result = Json.newObject();
        result.put("status", "OK");
        result.replace("annotations", listToJsonArray(Annotation.getAllByParagraph(paragraphHash)));
        return ok(result);
    }

    public Result create() {
        ObjectNode result = Json.newObject();
        JsonNode request = request().body().asJson();

        if (request.has(PARAGRAPH_HASH) && request.has(TOKEN) && request.has(TYPE) && request.has(OFFSETS)) {
            JsonNode offset = request.findValue(OFFSETS);
            Annotation annotation = new Annotation(request.findValue(TOKEN).asText(), request.findValue(TYPE).asText(), Paragraph.getParagraph(request.findValue(PARAGRAPH_HASH).asText()));
            if (offset.has(BEGIN) && offset.has(END)) {
                annotation.setOffsets(new Annotation.Offset(offset.findValue(BEGIN).asInt(), offset.findValue(END).asInt()));
            }

            annotation.save();
            result.put("status", "OK");
            return created(result);
        }

        result.put("status", "FAIL");
        result.put("error", "Missing required arguments");
        return badRequest(result);
    }

    public Result update(String annotationId) {
        ObjectNode result = Json.newObject();
        JsonNode request = request().body().asJson();
        Map<String, String> update = new HashMap<>();

        if (request.has(TYPE)) {
            update.put(TYPE, request.findValue(TYPE).asText());
        }

        if (request.has(OFFSETS)) {
            if (request.findValue(OFFSETS).has(BEGIN) && request.findValue(OFFSETS).has(END)) {
                update.put(OFFSETS + "." + BEGIN, request.findValue(OFFSETS).findValue(BEGIN).asText());
                update.put(OFFSETS + "." + END, request.findValue(OFFSETS).findValue(END).asText());
            }
        }

        if (request.has(TOKEN)) {
            update.put(TOKEN, request.findValue(TOKEN).asText());
        }

        if (Annotation.update(update, annotationId)) {
            result.put("status", "OK");
            return ok(result);
        }

        result.put("status", "FAIL");
        result.put("error", "Error while performing a write operation to a database");
        return internalServerError(result);
    }

    public Result delete(String annotationId) {
        ObjectNode result = Json.newObject();

        if (Annotation.delete(annotationId)) {
            result.put("status", "OK");
            return ok(result);
        }

        result.put("status", "FAIL");
        result.put("error", "Error while performing a write operation to a database");
        return internalServerError(result);
    }

    public Result getAll() {
        ObjectNode result = Json.newObject();
        ArrayNode array = listToJsonArray(Annotation.getAllAnnotations());
        result.put("status", "OK");
        result.replace("annotations", array);
        return ok(result);
    }

    private ArrayNode listToJsonArray(List<Annotation> list) {
        ArrayNode array = Json.newArray();
        for (Annotation annotation : list) {
            ObjectNode object = Json.newObject();
            ObjectNode offsets = Json.newObject();

            offsets.put(BEGIN, annotation.getOffsetBegin());
            offsets.put(END, annotation.getOffsetEnd());

            object.replace(OFFSETS, offsets);
            object.put(TYPE, annotation.getType());
            object.put(TOKEN, annotation.getToken());
            object.put(PARAGRAPH_HASH, annotation.getParagraph().getHash());
            object.put("annotationId", annotation.getId().toString());

            array.add(object);
        }
        return array;
    }

    public Result removeArchitecturalElement(String architecturalElement) {
        if (new DBpediaToken().updateTokenScore(architecturalElement, -1))
            return ok("200");
        return ok("500");
    }

    public Result addArchitecturalElement(String architecturalElement) {
        DBpediaToken savedToken = new DBpediaToken().findByName(architecturalElement);
        if (savedToken == null) {
            DBpediaToken dbType = new DBpediaToken(architecturalElement, "custom", 1);
            dbType.save();
        } else {
            new DBpediaToken().updateTokenScore(architecturalElement, 1);
        }
        return ok("200");
    }
}
