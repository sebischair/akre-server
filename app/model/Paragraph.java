package model;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.digest.DigestUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import static controllers.MorphiaObject.datastore;

@Entity("paragraph")
public class Paragraph {

    @Id
    private ObjectId id;

    private int paragraphNum;

    private String content;

    private String hash;

    public Paragraph setParagraphNum(int paragraphNum) {
        this.paragraphNum = paragraphNum;
        return this;
    }

    public Paragraph setContent(String content) {
        generateHash(content);
        this.content = content;
        return this;
    }

    public String getContent() {
        return content;
    }

    private void generateHash(String content) {
        this.hash = DigestUtils.sha1Hex(content);
    }

    public int getParagraphNum() {
        return this.paragraphNum;
    }

    public String getHash() {
        return this.hash;
    }

    public void save() {
        datastore.save(this);
    }

    public static Paragraph getParagraph(String paraghraphHash) {
        return datastore.createQuery(Paragraph.class).field("hash").equalIgnoreCase(paraghraphHash).get();
    }
}
