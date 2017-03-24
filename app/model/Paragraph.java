package model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

@Entity("paragraphs")
public class Paragraph {

    @Id
    private ObjectId id;

    private int paragraphNum;

    private String content;

    private String hash;

    public void setParagraphNum(int paragraphNum) {
        this.paragraphNum = paragraphNum;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
