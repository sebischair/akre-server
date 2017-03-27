package model;

import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

import java.util.ArrayList;
import java.util.List;


@Entity("record")
public class Record {

    @Id
    private ObjectId id;

    @Reference
    private List<Paragraph> paragraphs;

    private String sessionId;

    private String hash;

    private int version;

    //don't delete, it's used by Morphia
    private Record() { }

    private Record(String uuid, String hash) {
        sessionId = uuid;
        this.hash = hash;
        paragraphs = new ArrayList<>();
        this.increaseVersion(this.sessionId);
    }

    public static Record getRecord(String sessionId, String hash) {
        //get the record with the same hash
        List<Record> list = MorphiaObject.datastore.createQuery(Record.class).field("sessionId").equalIgnoreCase(sessionId).field("hash").equalIgnoreCase(hash).asList();
        if(!list.isEmpty()) {
            //if record exists
            //we load it
            return list.get(0);
        } else {
            return new Record(sessionId, hash);
        }
    }

    public List<Paragraph> getParagraphs() {
        return this.paragraphs;
    }

    public Record addParagraph(Paragraph paragraph) {
        if(!Record.containsParagraph(this.paragraphs, paragraph)) {
            paragraphs.add(paragraph);
            paragraph.save();
        }
        return this;
    }

    private void increaseVersion(String sessionId) {
        List<Record> list = (List<Record>) MorphiaObject.datastore.createQuery(getClass()).field("sessionId").equalIgnoreCase(sessionId).order("-version").asList();
        version = list.isEmpty() ? 0 : list.get(0).getVersion() + 1;
    }

    public int getVersion() {
        return this.version;
    }

    private static boolean containsParagraph(List<Paragraph> paragraphs, Paragraph paragraph) {
        for(Paragraph item : paragraphs) {
            if(item.getParagraphNum() == paragraph.getParagraphNum() && item.getHash().equals(paragraph.getHash())) {
                return true;
            }
        }
        return false;
    }

    public void save() {
        MorphiaObject.datastore.save(this);
    }
}
