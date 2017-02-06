package model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.query.Query;
import play.libs.Json;
import util.StaticFunctions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mahabaleshwar on 1/22/2017.
 */
@Entity("customcodeannotations")
@Indexes(@Index(value = "id", fields = @Field("id")))
public class CustomCodeAnnotation {
    @Id
    @Indexed
    private ObjectId _id;

    private String projectId;

    private String fileId;

    private String token;

    @Embedded
    private Range range;

    private ArrayList<String> lines;

    private String description;

    private String path;

    private String progLanguage;

    private Date createdAt;

    private ArrayList<String> tags;

    @Embedded
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public ArrayList<String> getLines() {
        return lines;
    }

    public void setLines(ArrayList<String> lines) {
        this.lines = lines;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getProgLanguage() {
        return progLanguage;
    }

    public void setProgLanguage(String progLanguage) {
        this.progLanguage = progLanguage;
    }

    public void save() {
        MorphiaObject.datastore.save(this);
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public static ObjectNode searalize(CustomCodeAnnotation cca) {
        ObjectNode obj = Json.newObject();
        obj.put("id", cca.get_id().toString());
        obj.put("projectId", cca.getProjectId());
        obj.put("fileId", cca.getFileId());
        obj.put("range", cca.getRange().searalize());
        obj.put("token", cca.getToken());
        obj.put("lines", StaticFunctions.getJsonFromList(cca.getLines()));
        obj.put("path", cca.getPath());
        obj.put("progLanguage", cca.getProgLanguage());
        obj.put("description", cca.getDescription());
        User u = cca.getUser();
        ObjectNode uObj = Json.newObject();
        uObj.put("name", u.getName());
        uObj.put("mail", u.getMail());
        obj.put("user", uObj);
        obj.put("createdAt", cca.getCreatedAt().toString());
        obj.put("tags", StaticFunctions.getJsonFromList(cca.getTags()));
        return obj;
    }

    public static ArrayNode searalize(List<CustomCodeAnnotation> ccas) {
        ArrayNode result = Json.newArray();
        for(CustomCodeAnnotation cca: ccas) {
            result.add(searalize(cca));
        }
        return result;
    }

    public static ArrayNode getAll() {
        Query<CustomCodeAnnotation> query = MorphiaObject.datastore.createQuery(CustomCodeAnnotation.class);
        return searalize(query.asList());
    }

    public static ArrayNode getAllAnnotationsForProject(String projectId) {
        List<CustomCodeAnnotation> annotations = MorphiaObject.datastore.createQuery(CustomCodeAnnotation.class).field("projectId").equalIgnoreCase(projectId).asList();
        return searalize(annotations);
    }

    public static ArrayNode getAllAnnotationsForFile(String fileId) {
        List<CustomCodeAnnotation> annotations = MorphiaObject.datastore.createQuery(CustomCodeAnnotation.class).field("fileId").equalIgnoreCase(fileId).asList();
        return searalize(annotations);
    }

    public static CustomCodeAnnotation findById(String id) {
        ObjectId objectId = new ObjectId(id);
        return MorphiaObject.datastore.get(CustomCodeAnnotation.class, objectId);
    }

    public static boolean delete(String id) {
        try{
            MorphiaObject.datastore.delete(findById(id));
            return true;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;

    }

}
