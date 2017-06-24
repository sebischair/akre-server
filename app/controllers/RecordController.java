package controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import model.Record;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import util.StaticFunctions;

import java.util.Iterator;
import java.util.List;


public class RecordController extends Controller {

    public Result getAllDocuments() {
        try {
            List<Record> list = Record.getAllRecords();
            ObjectNode response = Json.newObject();
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode array = mapper.valueToTree(list);
            response.putArray("records").addAll(array);
            return StaticFunctions.jsonResult(ok(response));
        }
        catch (Throwable t){
            Logger.error("Exception in getAllDocuments handler", t);
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
