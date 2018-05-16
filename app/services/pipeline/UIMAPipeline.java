package services.pipeline;

import com.fasterxml.jackson.databind.node.ArrayNode;
import model.Document;

/**
 * Created by mahabaleshwar on 6/23/2016.
 */
public abstract class UIMAPipeline {
    private Document document;

    public Document getDocument() { return document; }

    public void setDocument(Document document) { this.document = document; }

    public abstract ArrayNode processDocument();
}
