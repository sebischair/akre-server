package model;

import services.HelperService;
import util.HtmlUtil;
import java.util.TreeMap;

public class Document {

    public TreeMap<String, Integer> totalAmountOfAnnotations = new TreeMap<>();
    public TreeMap<String, TreeMap<String, Integer>> totalAmountOfDistinctAnnotations = new TreeMap<>();

    protected String language = "en";
    public String getLanguage() {
        return this.language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    protected String content;
    public String getContent() { return this.content; }
    public void setContent(String content) { this.content = content; }

    protected String rawContent;
    public String getRawContent() { return this.rawContent; }
    public void setRawContent(String rawContent) { this.rawContent = rawContent; }

    public Document(String content) {
        this.content = content;
        if(HelperService.isValidHtml(content)) this.setRawContent(HtmlUtil.convertToPlaintext(this.getContent())); else
            this.setRawContent(content);
    }

    public boolean isRawContentInHTML() {
        return HtmlUtil.isHtml(this.getContent());
    }
}
