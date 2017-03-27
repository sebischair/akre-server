package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class SessionController extends Controller {

    public Result createNewSession() {
        ObjectNode result = Json.newObject();

        String uuid=java.util.UUID.randomUUID().toString();
        session("uuid", uuid);

        result.put("session", uuid);
        result.put("status", "OK");

        return ok(result);
    }

}
