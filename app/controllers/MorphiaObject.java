package controllers;

import com.mongodb.Mongo;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

/**
 * Created by mahabaleshwar on 8/11/2016.
 */
public class MorphiaObject {
    static public Mongo mongo;
    static public Morphia morphia;
    static public Datastore datastore;
}
