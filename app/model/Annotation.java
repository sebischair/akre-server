package model;

import db.DefaultMongoClient;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity("annotations")
public class Annotation {

    @Id
    private ObjectId id;

    private String token;

    private String type;

    private Offset offsets;

    @Embedded
    public static class Offset {
        private int begin;
        private int end;

        public Offset() {}

        public Offset(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }
    }

    @Reference
    private Paragraph paragraph;

    public Annotation() {}

    public Annotation(String token, String type, Paragraph paragraph) {
        this.token = token;
        this.type = type;
        this.paragraph = paragraph;
    }

    public void save() {
        DefaultMongoClient.datastore.save(this);
    }


    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public void setOffsets(Offset offsets) {
        this.offsets = offsets;
    }

    public int getOffsetBegin() {
        return offsets.begin;
    }

    public int getOffsetEnd() {
        return offsets.end;
    }

    public Paragraph getParagraph() {
        return paragraph;
    }

    public ObjectId getId() {
        return id;
    }

    public static List<Annotation> getAllAnnotations() {
        return DefaultMongoClient.datastore.createQuery(Annotation.class).asList();
    }

    public static boolean delete(String annotationId) {
        return DefaultMongoClient.datastore.delete(Annotation.class, new ObjectId(annotationId)).wasAcknowledged();
    }

    public static boolean update(Map<String, String> update, String annotationId) {
        UpdateOperations ops = DefaultMongoClient.datastore
                .createUpdateOperations(Annotation.class);

        for (String key : update.keySet()) {
            ops.set(key, update.get(key));
        }

        Annotation annotation = DefaultMongoClient.datastore.get(Annotation.class, new ObjectId(annotationId));
        return annotation != null && DefaultMongoClient.datastore.update(annotation, ops).getUpdatedExisting();
    }

    public static List<Annotation> getAllByParagraph(String paragraphHash){
        List<Paragraph> paragraphs = DefaultMongoClient.datastore.createQuery(Paragraph.class).field("hash").equalIgnoreCase(paragraphHash).asList();
        if(paragraphs.isEmpty()) {
            return new ArrayList<>();
        }
        return DefaultMongoClient.datastore.createQuery(Annotation.class).filter("paragraph", paragraphs.get(0)).asList();
    }
}
