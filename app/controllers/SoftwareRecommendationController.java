package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Genre;
import model.Software;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.SparqlQueryExecuter;
import services.WikiTrends;
import util.StaticFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mahabaleshwar on 8/31/2016.
 */
public class SoftwareRecommendationController extends Controller {

    public Result removeSoftwareSolution() {
        ObjectNode result = Json.newObject();
        JsonNode jsonObject = request().body().asJson();
        String uri = jsonObject.findValue(StaticFunctions.URI).toString().replace("\"", "");
        String title = jsonObject.findValue(StaticFunctions.TITLE).toString().replace("\"", "");
        String description = "";
        if(jsonObject.has(StaticFunctions.DESCRIPTION))
            description = jsonObject.findValue(StaticFunctions.DESCRIPTION).toString().replace("\"", "");

        if(new Genre().updateSoftwareScore(uri, title, description, -9999.0)) {
            result.put("status", "200");
        } else {
            result.put("status", "500");
        }
        return ok(result);
    }

    public Result addSoftwareSolution() {
        ObjectNode result = Json.newObject();
        JsonNode jsonObject = request().body().asJson();
        String uri = jsonObject.findValue(StaticFunctions.URI).toString().replace("\"", "");
        String title = jsonObject.findValue(StaticFunctions.TITLE).toString().replace("\"", "");
        String description = "";
        if(jsonObject.has(StaticFunctions.DESCRIPTION))
            description = jsonObject.findValue(StaticFunctions.DESCRIPTION).toString().replace("\"", "");
        Genre genre = new Genre().findByName(uri);
        if(genre != null){
            if(genre.hasSoftware(uri, title)) {
                genre.updateSoftwareScore(uri, title, description, 9999);
            } else {
                genre.addSoftware(uri, new Software(title, title, description, 9999));
            }
            result.put("status", "200");
        } else {
            result.put("status", "500");
        }
        return ok(result);
    }

    public Result getSoftwareSolutions() {
        if(request().body().asJson().hasNonNull(StaticFunctions.URI)) {
            String uri = request().body().asJson().findValue(StaticFunctions.URI).toString().replace("\"", "");
            ArrayNode result = Json.newArray();
            if(!uri.contains("http:")) {
                Genre genre = new Genre().findByName(uri);
                if(genre != null)
                    result.addAll(StaticFunctions.sortJsonArray(genre.getSoftwareAsJsonArray()));
            } else {
                Genre genre = new Genre().findByName(uri);
                if(genre != null)
                    result.addAll(StaticFunctions.sortJsonArray(genre.getSoftwareAsJsonArray()));
                else {
                    String key = "<" + uri + "> ";
                    String queryString = "PREFIX dct:<http://purl.org/dc/terms/> \n" +
                            "PREFIX dbo: <http://dbpedia.org/ontology/> \n" +
                            "PREFIX ns: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                            "PREFIX schema: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                            "SELECT DISTINCT ?x ?title WHERE { { \n" +
                            "SELECT ?x ?title WHERE { \n" +
                            "?x dbo:genre" + key + " . \n" +
                            "?x ns:type dbo:Software . \n" +
                            "?x schema:label ?title } } UNION { \n" +
                            "SELECT ?x ?title WHERE { \n" +
                            key + "dct:subject ?concept . \n" +
                            "?x dct:subject ?concept . \n" +
                            "?x ns:type dbo:Software . \n" +
                            "?x schema:label ?title } \n" +
                            "} UNION { \n" +
                            "SELECT ?x ?title WHERE { \n" +
                            key + "dct:subject ?concept . \n" +
                            "?genre dct:subject ?concept . \n" +
                            "?x dbo:genre ?genre . \n" +
                            "?x ns:type dbo:Software ." +
                            "?x schema:label ?title } } \n" +
                            "filter(langMatches(lang(?title),\"EN\")) } LIMIT 1000";

                    SparqlQueryExecuter e = new SparqlQueryExecuter();
                    ArrayNode response = e.query(queryString);
                    WikiTrends trends = new WikiTrends();
                    response = trends.getTrends(response);
                    result.addAll(StaticFunctions.sortJsonArray(response));
                    saveGenre(uri, result);
                }
            }
            return ok(result);
        }
        ObjectNode res = Json.newObject();
        res.put("status", "400");
        return ok(res);
    }

    private void saveGenre(String uri, ArrayNode result) {
        Genre genre = new Genre().findByName(uri);
        if(genre == null) {
            genre = new Genre();
            genre.setGenreName(uri);
            List<Software> softwareList = new ArrayList<Software>();
            for(int i=0; i<result.size(); i++) {
                JsonNode jsonObject = result.get(i);
                String id =  jsonObject.get(StaticFunctions.URI) != null ? jsonObject.get(StaticFunctions.URI).asText() : "";
                String title = jsonObject.get(StaticFunctions.TITLE) != null ? jsonObject.get(StaticFunctions.TITLE).asText() : "";
                String description = jsonObject.get(StaticFunctions.DESCRIPTION) != null ? jsonObject.get(StaticFunctions.DESCRIPTION).asText() : "";
                double score = jsonObject.get(StaticFunctions.SCORE) != null ? jsonObject.get(StaticFunctions.SCORE).asDouble() : 0.0;
                softwareList.add(new Software(id, title, description, score));
            }
            genre.setSoftware(softwareList);
            genre.save();
        }
    }
}
