package model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import services.HelperService;
import util.HtmlUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.TreeMap;

@Entity("Document")
public class Document {

    @Id
    private ObjectId id;

    public TreeMap<String, Integer> totalAmountOfAnnotations = new TreeMap<>();
    public TreeMap<String, TreeMap<String, Integer>> totalAmountOfDistinctAnnotations = new TreeMap<>();

    protected String language = "en";
    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    protected String content;
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    protected String rawContent;
    public String getRawContent() { return rawContent; }
    public void setRawContent(String rawContent) { this.rawContent = rawContent; }

    public Document(String content) {
        this.content = content;
        if(HelperService.isValidHtml(content)) setRawContent(HtmlUtil.convertToPlaintext(getContent())); else setRawContent(content);
    }

    public boolean isRawContentInHTML() {
        return HtmlUtil.isHtml(getContent());
    }

    @Reference
    protected ArrayList<Paragraph> paragraphs;

    private String sessionId;

    private String hash;

}
