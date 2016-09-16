package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import play.libs.ws.WSAuthScheme;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import java.util.concurrent.CompletionStage;

public class HelperService {
    WSClient ws;
            
    public HelperService(WSClient ws) {
        this.ws = ws;
    }

    public CompletionStage<JsonNode> getWSResponse(String url) {
        return ws.url(url).setAuth("userName", "password", WSAuthScheme.BASIC).get().thenApply(WSResponse:: asJson);
    }

    /**
     * @return GSON data with correct date format
     */
    public static Gson getGson() {
        return new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    }

    // Whitelist basic does not allow images in the content
    public static boolean isValidHtml(String html) {return Jsoup.isValid(html, Whitelist.basic()); }

    public static String html2text(String html) { return Jsoup.parse(html).text(); }
}
