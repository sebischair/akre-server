package services.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.metadata.Metadata;
import org.xml.sax.SAXException;
import play.libs.Json;

import java.io.*;

public class AutoDetectTikaParser implements Parser {

    @Override
    public JsonNode parse(File file) throws IOException, TikaException, SAXException {
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        try (InputStream stream = new FileInputStream(file)) {
            parser.parse(stream, handler, metadata);
            ObjectNode node = Json.newObject();
            node.replace("paragraphs", ContextToParagraphs(handler.toString()));
            return node;
        }
    }
    private JsonNode ContextToParagraphs(String context) {
        String[] paragraphs = context.split("\n\n");
        ArrayNode array = Json.newArray();
        for (String par : paragraphs) {
            array.add(par);
        }
        return array;
    }
}
