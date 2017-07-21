package controllers;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import play.Configuration;
import play.Environment;
import play.Logger;

import java.util.ArrayList;
import java.util.List;

public class MorphiaObject {
    static public Morphia morphia;
    static public Datastore datastore;


    static public void connect(String dbUrl, Integer dbPort, String dbName, String dbUsername, String dbPwd) throws Exception{
        Environment env;
        env = Environment.simple();
        Configuration configuration = Configuration.load(env);

        ServerAddress sa = new ServerAddress(dbUrl, dbPort);
        List<MongoCredential> cl = new ArrayList<MongoCredential>();
        MongoCredential mc = MongoCredential.createCredential(dbUsername, dbName, dbPwd.toCharArray());
        cl.add(mc);

        morphia = new Morphia();
        morphia.mapPackage("app.model");
        datastore = morphia.createDatastore(new MongoClient(sa, cl), dbName);
        datastore.ensureIndexes();
        datastore.ensureCaps();
    }
}


