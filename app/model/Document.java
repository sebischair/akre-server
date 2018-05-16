package model;

public class Document {

    protected String language = "en";
    protected String rawContent;

    public String getLanguage() {
        return this.language;
    }

    public String getRawContent() { return this.rawContent; }

    public Document(String content) {
        this.rawContent = content;
    }
}
