package model;

import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.List;

/**
 * Created by mahabaleshwar on 11/22/2016.
 */
@Entity("codes")
@Indexes(@Index(value = "id", fields = @Field("id")))
public class Code {
    @Id
    @Indexed
    private ObjectId _id;

    private String content;

    private String progLanguage;

    private String fileName;

    private String projectId;

    public Code() {}

    public Code(String content, String progLanguage, String fileName, String projectId)  {
        this.content = content;
        this.progLanguage = progLanguage;
        this.fileName = fileName;
        this.projectId =projectId;
    }

    public Key<Code> save() {
        return MorphiaObject.datastore.save(this);
    }

    public Code findByFileName(String name) {
        List<? extends Code> code = MorphiaObject.datastore.createQuery(this.getClass()).field("fileName").equalIgnoreCase(name).asList();
        if(code.size() > 0) return code.get(0);
        return null;
    }

    public boolean updateCode(String fileName, String content, String progLanguage) {
        try {
            Query<Code> query = (Query<Code>) MorphiaObject.datastore.createQuery(this.getClass()).field("fileName").equalIgnoreCase(fileName);
            UpdateOperations<Code> ops = (UpdateOperations<Code>) MorphiaObject.datastore.createUpdateOperations(this.getClass()).set("content", content).set("progLanguage", progLanguage);
            MorphiaObject.datastore.update(query, ops);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
