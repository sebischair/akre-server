package model.amelie;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import db.AmelieMongoClient;
import org.bson.Document;

import java.util.ArrayList;

/**
 * Created by Manoj on 1/3/2018.
 */
public class Keyword {
    private MongoCollection<Document> keywordCollection;

    public Keyword() {
        keywordCollection = AmelieMongoClient.amelieDatabase.getCollection("keywords");
    }

    public ArrayList<String> getAllKeywords() {
        MongoCursor<Document> cursor = keywordCollection.find().iterator();
        while(cursor.hasNext()) {
            Document doc = cursor.next();
            return (ArrayList<String>) doc.get("keywords");
        }
        return new ArrayList<>();
    }
}
