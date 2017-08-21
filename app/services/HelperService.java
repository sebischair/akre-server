package services;

import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import play.Configuration;
import play.libs.Json;
import play.libs.ws.WSAuthScheme;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import java.util.concurrent.CompletionStage;

public class HelperService {
    WSClient ws;
    Configuration configuration = Configuration.root();

    String SC_BASE_URL = configuration.getString("sc.base.url");
    String userName = configuration.getString("sc.userName");
    String password = configuration.getString("sc.password");

    public HelperService(WSClient ws) {
        this.ws = ws;
    }

    public CompletionStage<JsonNode> getWSResponseWithAuth(String url) {
        return ws.url(url).setAuth(userName, password, WSAuthScheme.BASIC).get().thenApply(WSResponse::asJson);
    }

    public CompletionStage<JsonNode> getWSResponse(String url) {
        return ws.url(url).get().thenApply(WSResponse::asJson);
    }

    public CompletionStage<JsonNode> postWSRequest(String url, JsonNode json) {
        return ws.url(url).setAuth(userName, password, WSAuthScheme.BASIC).post(json).thenApply(WSResponse::asJson);
    }

    public CompletionStage<JsonNode> putWSRequest(String url, JsonNode json) {
        return ws.url(url).setAuth(userName, password, WSAuthScheme.BASIC).put(json).thenApply(WSResponse::asJson);
    }

    // Whitelist basic does not allow images in the content
    public static boolean isValidHtml(String html) {
        return Jsoup.isValid(html, Whitelist.basic());
    }

    public static String html2text(String html) {
        return Jsoup.parse(html).text();
    }

    public CompletionStage<JsonNode> entitiesForTypeUid(String typeId) {
        return getWSResponse(SC_BASE_URL + "entityTypes/" + typeId + "/entities");
    }

    public CompletionStage<JsonNode> entityForUid(String entityId) {
        return getWSResponse(SC_BASE_URL + "entities/" + entityId);
    }

    public CompletionStage<JsonNode> executeMxl(String workspaceId, String expression) {
        String url = SC_BASE_URL + "workspaces/" + workspaceId + "/mxlQuery";
        JsonNode json = Json.newObject().put("expression", expression);
        return postWSRequest(url, json);
    }

    public CompletionStage<JsonNode> editEntity(JsonNode entity, String id) {
        String url = SC_BASE_URL + "entities/" + id;
        return putWSRequest(url, entity);
    }

    public CompletionStage<JsonNode> createEntity(JsonNode entity) {
        String url = SC_BASE_URL + "entities";
        return postWSRequest(url, entity);
    }
}
