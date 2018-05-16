package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import play.libs.Json;
import util.StaticFunctions;

/**
 * Created by mahabaleshwar on 11/2/2016.
 */
public class WikiTrends {
    public ArrayNode getTrends(ArrayNode resources) {
        HttpClient client = new HttpClient();
        for (int i = 0; i < resources.size(); i++) {
            ObjectNode resource = (ObjectNode) resources.get(i);
            String query = resource.get(StaticFunctions.TITLE).toString().trim().replaceAll("\"", "").replaceAll(" ", "_");
            String url = String.format("https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=%s&srlimit=1&format=json", query);
            try {
                HttpMethod method = new GetMethod(url);
                method.addRequestHeader("User-Agent", "Mozilla/5.0");
                // Execute the method.
                int statusCode = client.executeMethod(method);
                if (statusCode == HttpStatus.SC_OK) {
                    String response = IOUtils.toString(method.getResponseBodyAsStream(), "UTF-8");
                    double score = 0.0;
                    String description = "";
                    try{
                        JsonNode jsonObject = Json.parse(response);
                        score = Double.parseDouble(jsonObject.get("query").get("searchinfo").get("totalhits").asText());
                        description = jsonObject.get("query").get("search").get(0).get("snippet").asText();

                    } catch (Exception e) {}
                    resource.put(StaticFunctions.SCORE, score);
                    resource.put(StaticFunctions.DESCRIPTION, description);
                }
            } catch (HttpException e) {
                System.err.println("Fatal protocol violation: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Fatal transport error: " + e.getMessage());
            }
        }
        return resources;
    }
}
