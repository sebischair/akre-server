import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import controllers.MorphiaObject;
import org.mongodb.morphia.Morphia;
import play.Configuration;
import play.Environment;
import play.GlobalSettings;
import play.Logger;

import java.util.ArrayList;
import java.util.List;

public class Global extends GlobalSettings {

    @Override
    public void onStart(play.Application arg0) {
        super.beforeStart(arg0);
        Logger.debug("** onStart **");
        Configuration configuration = Configuration.root();
        try {

            MorphiaObject.connect(
                    configuration.getString("morphia.db.url"),
                    configuration.getInt("morphia.db.port"),
                    configuration.getString("morphia.db.name"),
                    configuration.getString("morphia.db.username"),
                    configuration.getString("morphia.db.pwd")
            );

            Logger.debug("** Morphia datastore: " + MorphiaObject.datastore.getDB());
        } catch (Exception e) {
            Logger.error("** Morphia datastore: " + e.toString());
        }


    }
}