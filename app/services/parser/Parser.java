package services.parser;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

/**
 * Created by Alex on 21/03/17.
 */
public interface Parser {

    JsonNode parse(File file) throws IOException, TikaException, SAXException;
}
