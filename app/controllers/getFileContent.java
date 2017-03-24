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

import java.io.File;


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

}
