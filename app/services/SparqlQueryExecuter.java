package services;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.jena.query.*;
import play.libs.Json;
import util.StaticFunctions;

public class SparqlQueryExecuter {

    String sparqlService = "http://dbpedia.org/sparql";

    public ArrayNode query(String queryString) {
		
        ArrayNode result = Json.newArray();
        Query query = QueryFactory.create(queryString);
        QueryExecution exec = QueryExecutionFactory.sparqlService(sparqlService, query);
        ResultSet results = ResultSetFactory.copyResults(exec.execSelect());
        while(results.hasNext()) {
            QuerySolution r = results.next();
            ObjectNode jsonObject = Json.newObject();
            jsonObject.put(StaticFunctions.URI, r.get("x").toString());
            if(r.get(StaticFunctions.TITLE) != null) {
                jsonObject.put(StaticFunctions.TITLE, r.get(StaticFunctions.TITLE).toString().replace("@en", ""));
            }
            result.add(jsonObject);
        }
        return result;
    }
}