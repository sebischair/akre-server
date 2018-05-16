package services;

import com.fasterxml.jackson.databind.JsonNode;
import play.Configuration;
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
        return ws.url(url).post(json).thenApply(WSResponse::asJson);
    }

    public CompletionStage<JsonNode> putWSRequest(String url, JsonNode json) {
        return ws.url(url).setAuth(userName, password, WSAuthScheme.BASIC).put(json).thenApply(WSResponse::asJson);
    }
}
