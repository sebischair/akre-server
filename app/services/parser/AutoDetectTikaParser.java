package services.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.metadata.Metadata;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import play.libs.Json;

import java.io.*;
import java.util.HashMap;
import java.util.List;

public class AutoDetectTikaParser implements Parser {

    @Override
    public JsonNode parse(File file) throws IOException, TikaException, SAXException {
        AutoDetectParser parser = new AutoDetectParser();
        ContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        try (InputStream stream = new FileInputStream(file)) {
            parser.parse(stream, handler, metadata);
            ObjectNode node = Json.newObject();
            node.put("content", handler.toString());
            node.replace("meta", MetadataToJson(metadata));
            return node;
        }
    }

    private JsonNode MetadataToJson(Metadata metadata) {
        /*
        Metadata stores values in a HashMap in private field,
        since it doesn't return full HashMap
        we create a json object using names()
         */
        ObjectNode node = Json.newObject();
        String[] names = metadata.names();
        for (String name : names) {
            if(!metadata.get(name).equals("")) {
                node.put(name, metadata.get(name));
            }
        }
        return node;
    }
}
