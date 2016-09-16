import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import controllers.MorphiaObject;
import org.mongodb.morphia.Morphia;
import play.GlobalSettings;
import play.Logger;

import java.util.ArrayList;
import java.util.List;

public class Global extends GlobalSettings {

    @Override
    public void onStart(play.Application arg0) {
        super.beforeStart(arg0);
        Logger.debug("** onStart **");
        MorphiaObject.mongo = new Mongo("127.0.0.1", 27017);
        ServerAddress sa = new ServerAddress("127.0.0.1", 27017);
        List<MongoCredential> cl = new ArrayList<MongoCredential>();
        MongoCredential mc = MongoCredential.createCredential("guest", "akrec", "guest".toCharArray());
        cl.add(mc);
        MorphiaObject.morphia = new Morphia();
        MorphiaObject.morphia.mapPackage("app.model");
        MorphiaObject.datastore = MorphiaObject.morphia.createDatastore(new MongoClient(sa, cl), "akrec");
        MorphiaObject.datastore.ensureIndexes();
        MorphiaObject.datastore.ensureCaps();

        Logger.debug("** Morphia datastore: " + MorphiaObject.datastore.getDB());
    }
}