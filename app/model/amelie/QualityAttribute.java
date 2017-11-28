package model.amelie;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import db.AmelieMongoClient;
import org.bson.Document;
import play.libs.Json;
import util.StaticFunctions;

/**
 * Created by Manoj on 11/28/2017.
 */
public class QualityAttribute {
    private MongoCollection<Document> qaCollection;

    public QualityAttribute() {
        qaCollection = AmelieMongoClient.amelieDatabase.getCollection("qualityAttributes");
    }

    public ArrayNode getAllQAs() {
        ArrayNode qas = Json.newArray();
        MongoCursor<Document> cursor = qaCollection.find().iterator();
        while(cursor.hasNext()) {
            qas.add(getQADetails(cursor.next()));
        }
        return qas;
    }

    private ObjectNode getQADetails(Document obj) {
        ObjectNode qa = Json.newObject();
        qa.put("name", obj.getString("name"));
        ArrayNode keywords = StaticFunctions.getArrayNodeFromJsonNode(obj, "keywords");
        keywords.add(obj.getString("name"));
        keywords.add(obj.getString("factor"));
        qa.set("keywords", keywords);
        return qa;
    }
}
