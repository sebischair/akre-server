package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;
import play.Play;
import services.parser.AutoDetectTikaParser;
import services.parser.Parser;
import util.StaticFunctions;

import model.Paragraph;
import model.Record;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;


public class getFileContent extends Controller {

    public Result upload() {
        try {
            MultipartFormData<File> body = request().body().asMultipartFormData();
            System.out.println(body);
            MultipartFormData.FilePart<File> f = body.getFile("file");
            File file = f.getFile();
            Parser parser = new AutoDetectTikaParser();
            ObjectNode result = (ObjectNode) parser.parse(file);
            result.put("success", true);
            return StaticFunctions.jsonResult(ok(result));
        }
        catch (Throwable t) {
            Logger.error("Exception in upload handler", t);
            return StaticFunctions.jsonResult(badRequest(StaticFunctions.errorAsJson(t)));
        }
    }

    public Result uploadAndSave() {
        try {
            MultipartFormData<File> body = request().body().asMultipartFormData();
            MultipartFormData.FilePart<File> f = body.getFile("file");
            File file = f.getFile();
            Parser parser = new AutoDetectTikaParser();
            ObjectNode parsed_result = (ObjectNode) parser.parse(file);
            String fakeSessionId=java.util.UUID.randomUUID().toString();
            String fakeDocumentHash=java.util.UUID.randomUUID().toString();
            Record record = Record.getRecord(fakeSessionId,fakeDocumentHash);
            JsonNode parasNode = parsed_result.path("paragraphs");
            Iterator itr = parasNode.elements();
            Integer paragraphNumber = 0;
            while(itr.hasNext()) {
                Paragraph paragraph = new Paragraph().setParagraphNum(paragraphNumber);
                String element = itr.next().toString();
                paragraph.setContent(element);
                record.addParagraph(paragraph).save();
                paragraphNumber++;
            }
            ObjectNode response = Json.newObject();
            response.put("success", true);
            response.put("fakeSessionID", fakeSessionId);
            response.put("fakeDocumentHash", fakeDocumentHash);
            return StaticFunctions.jsonResult(ok(response));
        }
        catch (Throwable t) {
            Logger.error("Exception in upload handler", t);
            return StaticFunctions.jsonResult(badRequest(StaticFunctions.errorAsJson(t)));
        }
    }

    public Result deleteAllDocuments(){ //unsafe TODO:replace later on
        try {
            Record.removeAllRecords();
            return StaticFunctions.jsonResult(ok());
        }
        catch (Throwable t) {
            Logger.error("Exception in deleteAllDocuments handler", t);
            return StaticFunctions.jsonResult(badRequest(StaticFunctions.errorAsJson(t)));
        }
    }

}
