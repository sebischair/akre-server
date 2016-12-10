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
import util.StaticFunctions;

import java.io.File;


public class getFileContent extends Controller {

    public Result upload() {
        ObjectNode result = Json.newObject();
        try {
            MultipartFormData<File> body = request().body().asMultipartFormData();
            System.out.println(body);
            MultipartFormData.FilePart<File> f = body.getFile("file");

            String fileName = f.getFilename();
            String contentType = f.getContentType();
            File file = f.getFile();
            System.out.println(fileName);
            System.out.println(file);
            JsonNode json = Json.parse(Files.toString(Play.application().getFile("conf/response.json"), Charsets.UTF_8));
            result.replace("content", json);
            result.put("success", true);
            return StaticFunctions.jsonResult(ok(result));
        }
        catch (Throwable t) {
            Logger.error("Exception in upload handler", t);
            return StaticFunctions.jsonResult(badRequest(StaticFunctions.errorAsJson(t)));
        }
    }

}
