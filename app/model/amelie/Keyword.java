package model.amelie;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import db.AmelieMongoClient;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;

/**
 * Created by Manoj on 1/3/2018.
 */
public class Keyword {
    private MongoCollection<Document> keywordCollection;
    private MongoCollection<Document> keywordENCollection;

    public Keyword() {
        keywordCollection = AmelieMongoClient.amelieDatabase.getCollection("keywordsDE");
        keywordENCollection = AmelieMongoClient.amelieDatabase.getCollection("keywords");
    }

    public ArrayList<String> getAllKeywords() {
        MongoCursor<Document> cursor = keywordCollection.find().iterator();
        while(cursor.hasNext()) {
            Document doc = cursor.next();
            return (ArrayList<String>) doc.get("keywords");
        }
        return new ArrayList<>();
    }

    public ArrayList<String> getAllENKeywords() {
        MongoCursor<Document> cursor = keywordENCollection.find().iterator();
        while(cursor.hasNext()) {
            Document doc = cursor.next();
            return (ArrayList<String>) doc.get("keywords");
        }
        return new ArrayList<>();
    }

    public void addKeyWord(String topicName) {
        System.out.println(topicName);
        BasicDBObject modifiedObject =new BasicDBObject();
        modifiedObject.put("$push", new BasicDBObject().append("keywords", topicName));
        BasicDBObject searchQuery = new BasicDBObject().append("_id", new ObjectId("5a786bf01f2aa1456c5c8bae"));
        keywordCollection.updateOne(searchQuery, modifiedObject);
    }
}
