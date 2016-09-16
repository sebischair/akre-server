package model;

import controllers.MorphiaObject;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mahabaleshwar on 9/14/2016.
 */
@Entity("doctemplates")
@Indexes(@Index(value = "keyword", fields = @Field("keyword")))
public class Template {
    @Id
    private ObjectId _id;

    @Indexed
    private String keyword;

    private String name;

    private String type;

    private String path;

    public List<String> findAllTemplateNames() {
        List<Template> templates = (List<Template>) MorphiaObject.datastore.createQuery(this.getClass()).asList();
        Set<String> templateNames = new HashSet<String>();
        for(Template template: templates) {
            templateNames.add(template.getKeyword());
            templateNames.add(template.getName());
        }
        return new ArrayList(templateNames);
    }

    public String getKeyword() {
        return keyword;
    }

    public String getName() {
        return name;
    }
}
