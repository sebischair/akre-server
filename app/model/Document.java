package model;

import services.HelperService;
import util.HtmlUtil;

import java.util.TreeMap;

/**
 * Created by mahabaleshwar on 6/23/2016.
 */
public class Document {

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

}
