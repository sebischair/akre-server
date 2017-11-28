package model;

import db.DefaultMongoClient;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mahabaleshwar on 9/14/2016.
 */
@Entity("methods")
@Indexes(@Index(value = "methodName", fields = @Field("methodName")))
public class Method {
    @Id
    private ObjectId _id;

    @Indexed
    private String methodName;

    private String methodURL;

    private String description;

    public List<String> findAllMethodNames() {
        List<Method> methods = (List<Method>) DefaultMongoClient.datastore.createQuery(this.getClass()).asList();
        Set<String> methodNames = new HashSet<String>();
        for(Method method: methods) {
            methodNames.add(method.getMethodName());
        }
        return new ArrayList(methodNames);
    }

    public String getMethodName() {
        return methodName;
    }
}
