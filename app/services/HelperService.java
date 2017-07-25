package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import play.libs.Json;
import play.libs.ws.WSAuthScheme;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import java.util.concurrent.CompletionStage;

public class HelperService {
    WSClient ws;
    public static String SC_BASE_URL = "https://server.sociocortex.com/api/v1/";

    public HelperService(WSClient ws) {
        this.ws = ws;
    }

    public CompletionStage<JsonNode> getWSResponseWithAuth(String url, String userName, String password) {
        return ws.url(url).setAuth(userName, password, WSAuthScheme.BASIC).get().thenApply(WSResponse::asJson);
    }

    public CompletionStage<JsonNode> getWSResponse(String url) {
        return ws.url(url).get().thenApply(WSResponse::asJson);
    }

    public CompletionStage<JsonNode> postWSRequest(String url, JsonNode json) {
        return ws.url(url).post(json).thenApply(WSResponse::asJson);
    }

    /**
     * @return GSON data with correct date format
     */
    public static Gson getGson() {
        return new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
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
}
