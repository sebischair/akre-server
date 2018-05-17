package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.Alternative;
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
public class AlternativesRecommendationController extends Controller {

    public Result getAlternatives() {
        if(request().body().asJson().hasNonNull(StaticFunctions.URI)) {
            String uri = request().body().asJson().findValue(StaticFunctions.URI).toString().replace("\"", "");
            ArrayNode result = Json.newArray();
            if (!uri.contains("http:")) {
                Alternative alternative = new Alternative().findByName(uri);
                if (alternative != null)
                    result.addAll(StaticFunctions.sortJsonArray(alternative.getAlternativesAsJsonArray()));
            } else {
                Alternative alternative = new Alternative().findByName(uri);
                if (alternative != null)
                    result.addAll(StaticFunctions.sortJsonArray(alternative.getAlternativesAsJsonArray()));
                else {
                    String key = "<" + uri + "> ";
                    String queryString = "PREFIX dct: <http://purl.org/dc/terms/>\n" +
                            "PREFIX dbr: <http://dbpedia.org/resource/>\n" +
                            "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
                            "PREFIX schema: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                            "PREFIX ns: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                            "SELECT DISTINCT ?x ?title WHERE { \n" +
                            "{SELECT DISTINCT ?x ?title WHERE { \n" +
                            key + "dbo:genre ?genre .\n" +
                            key + "dct:subject ?subject .\n" +
                            "?x dbo:genre ?genre .\n" +
                            "?x dct:subject ?subject .\n" +
                            "?x ns:type dbo:Software .\n" +
                            "?x schema:label ?title }\n" +
                            "} UNION { \n" +
                            "SELECT DISTINCT ?x ?title WHERE { \n" +
                            key + " ns:type dbo:Genre .\n" +
                            key + " dct:subject ?concept .\n" +
                            "?x dct:subject ?concept .\n" +
                            "?x ns:type dbo:Genre .\n" +
                            "?x schema:label ?title } } \n" +
                            "UNION { \n" +
                            "SELECT DISTINCT ?x ?title WHERE { \n" +
                            key + " dct:subject ?concept .\n" +
                            "?x dct:subject ?concept .\n" +
                            "?x ns:type <http://dbpedia.org/class/yago/Software106566077> .\n" +
                            "?x schema:label ?title } }\n" +
                            "FILTER langMatches(lang(?title), \"EN\")\n" +
                            " } LIMIT 1000";

                    SparqlQueryExecuter e = new SparqlQueryExecuter();
                    ArrayNode response = e.query(queryString);

                    if (response.size() == 0) {
                        queryString = "PREFIX dct: <http://purl.org/dc/terms/> \n" +
                                "PREFIX schema: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                                "SELECT DISTINCT ?x ?title WHERE {\n" +
                                key + " dct:subject ?concept . \n" +
                                "?x dct:subject ?concept . \n" +
                                "?x schema:label ?title . \n" +
                                "FILTER(regex(?concept, \"pattern\", \"i\")) \n" +
                                "FILTER langMatches(lang(?title), \"EN\") \n" +
                                "} LIMIT 1000";
                        e = new SparqlQueryExecuter();
                        response = e.query(queryString);
                    }

                    WikiTrends trends = new WikiTrends();
                    response = trends.getTrends(response);

                    result.addAll(StaticFunctions.sortJsonArray(response));
                    saveAlternative(uri, result);
                }
            }
            return ok(result);
        }
        ObjectNode res = Json.newObject();
        res.put("status", "400");
        return ok(res);
    }

    public Result removeAlternative() {
        ObjectNode result = Json.newObject();
        JsonNode jsonObject = request().body().asJson();
        String uri = jsonObject.findValue(StaticFunctions.URI).toString().replace("\"", "");
        String title = jsonObject.findValue(StaticFunctions.TITLE).toString().replace("\"", "");
        String description = "";
        if(jsonObject.has(StaticFunctions.DESCRIPTION))
            description = jsonObject.findValue(StaticFunctions.DESCRIPTION).toString().replace("\"", "");

        if(new Alternative().updateAlternativeScore(uri, title, description, -9999)) {
            result.put("status", "200");
        } else {
            result.put("status", "500");
        }
        return ok(result);
    }

    public Result addAlternative() {
        ObjectNode result = Json.newObject();
        JsonNode jsonObject = request().body().asJson();
        String uri = jsonObject.findValue(StaticFunctions.URI).toString().replace("\"", "");
        String title = jsonObject.findValue(StaticFunctions.TITLE).toString().replace("\"", "");
        String description = "";
        if(jsonObject.has(StaticFunctions.DESCRIPTION))
            description = jsonObject.findValue(StaticFunctions.DESCRIPTION).toString().replace("\"", "");
        Alternative alternative = new Alternative().findByName(uri);
        if(alternative != null){
            if(alternative.hasSoftware(uri, title)) {
                alternative.updateAlternativeScore(uri, title, description, 9999);
            } else {
                alternative.addSoftware(uri, new Software(title, title, description, 9999));
            }
            result.put("status", "200");
        } else {
            result.put("status", "500");
        }
        return ok(result);
    }

    private void saveAlternative(String uri, ArrayNode result) {
        Alternative alternative = new Alternative().findByName(uri);
        if(alternative == null) {
            alternative = new Alternative();
            alternative.setConceptName(uri);
            List<Software> softwareList = new ArrayList<>();
            for(int i=0; i<result.size(); i++) {
                JsonNode jsonObject = result.get(i);
                if(jsonObject.has(StaticFunctions.URI) && jsonObject.has(StaticFunctions.TITLE) && jsonObject.has(StaticFunctions.DESCRIPTION) && jsonObject.has(StaticFunctions.SCORE))
                    softwareList.add(new Software(jsonObject.get(StaticFunctions.URI).asText(""), jsonObject.get(StaticFunctions.TITLE).asText(""), jsonObject.get(StaticFunctions.DESCRIPTION).asText(""), jsonObject.get(StaticFunctions.SCORE).asDouble(0.0)));
            }
            alternative.setSoftware(softwareList);
            alternative.save();
        }
    }
}
