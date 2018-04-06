package db;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import play.Configuration;

import java.util.ArrayList;
import java.util.List;

public class DefaultMongoClient {
    static public Morphia morphia;
    static public Datastore datastore;


    static public void connect() throws Exception{
        String dockerHost = "mongo";    // For docker, don't provide credentials to database
        Configuration configuration = Configuration.root();
        String dbUrl = configuration.getString("morphia.db.url");
        int dbPort = configuration.getInt("morphia.db.port");
        String userName = configuration.getString("morphia.db.username");
        String password = configuration.getString("morphia.db.pwd");
        String dbName = configuration.getString("morphia.db.name");

        ServerAddress sa = new ServerAddress(dbUrl, dbPort);
        List<MongoCredential> cl = new ArrayList<MongoCredential>();
        MongoCredential mc = MongoCredential.createCredential(userName, dbName, password.toCharArray());
        cl.add(mc);

        morphia = new Morphia();
        morphia.mapPackage("app.model");
        if (dbUrl.equals(dockerHost)) {
            datastore = morphia.createDatastore(new MongoClient(sa), dbName);
        } else {
            datastore = morphia.createDatastore(new MongoClient(sa, cl), dbName);
        }
        datastore.ensureIndexes();
        datastore.ensureCaps();
    }
}


