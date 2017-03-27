package model;

import controllers.MorphiaObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

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
        MorphiaObject.datastore.save(this);
    }
}
