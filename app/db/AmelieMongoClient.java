package db;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import play.Configuration;

import java.util.Arrays;

public class AmelieMongoClient {
    static public Morphia amelieMorphia;
    static public Datastore amelieDataStore;
    static public MongoDatabase amelieDatabase;


    static public void connect() throws Exception {
        Configuration configuration = Configuration.root();
        String dbUrl = configuration.getString("morphia.db.url");
        int dbPort = configuration.getInt("morphia.db.port");
        String userName = configuration.getString("morphia.db.username");
        String password = configuration.getString("morphia.db.pwd");
        String dbName = configuration.getString("morphia.amelie.db.name");

        ServerAddress sa = new ServerAddress(dbUrl, dbPort);
        MongoCredential credential = MongoCredential.createCredential(userName, dbName, password.toCharArray());
        MongoClient mongoClient = new MongoClient(sa, Arrays.asList(credential));

        amelieMorphia = new Morphia();
        amelieMorphia.mapPackage("app.model.amelie");
        amelieDataStore = amelieMorphia.createDatastore(mongoClient, dbName);
        amelieDatabase = mongoClient.getDatabase(dbName);

        amelieDataStore.ensureIndexes();
        amelieDataStore.ensureCaps();
    }
}


