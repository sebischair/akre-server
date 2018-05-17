package services;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import java.util.concurrent.CompletionStage;

public class HelperService {
    WSClient ws;

    public HelperService(WSClient ws) {
        this.ws = ws;
    }

    public CompletionStage<JsonNode> getWSResponse(String url) {
        return ws.url(url).get().thenApply(WSResponse::asJson);
    }

    public CompletionStage<JsonNode> postWSRequest(String url, JsonNode json) {
        return ws.url(url).post(json).thenApply(WSResponse::asJson);
    }
}
