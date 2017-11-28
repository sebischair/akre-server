package services;

import com.aylien.textapi.TextAPIClient;
import com.aylien.textapi.TextAPIException;
import com.aylien.textapi.parameters.ConceptsParams;
import com.aylien.textapi.parameters.EntitiesParams;
import com.aylien.textapi.responses.Concept;
import com.aylien.textapi.responses.Concepts;
import com.aylien.textapi.responses.Entities;
import com.aylien.textapi.responses.Entity;
import play.Configuration;

import java.util.List;

/**
 * Created by Manoj on 4/12/2017.
 */
public class TextAnalysisClient {
    Configuration configuration = Configuration.root();

    String APIKEY = configuration.getString("aylien.api.key");
    String APPID = configuration.getString("aylien.api.id");

    public List<Entity> extractKeywords(String text) {
        TextAPIClient client = new TextAPIClient(APPID, APIKEY);
        EntitiesParams.Builder builder = EntitiesParams.newBuilder();
        builder.setText(text);
        try {
            Entities entities = client.entities(builder.build());
            synchronized(this){
                wait(2000);
            }
            return entities.getEntities();
        } catch (TextAPIException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Concept> extractConcepts(String text) {
        TextAPIClient client = new TextAPIClient(APPID, APIKEY);
        ConceptsParams.Builder builder = ConceptsParams.newBuilder();
        builder.setText(text);
        try {
            Concepts concepts = client.concepts(builder.build());
            synchronized(this){
                wait(2000);
            }
            return concepts.getConcepts();
        } catch (TextAPIException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
