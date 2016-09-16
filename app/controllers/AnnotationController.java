package controllers;

import model.DBpediaToken;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * Created by mahabaleshwar on 8/29/2016.
 */
public class AnnotationController extends Controller {

    public Result removeToken(String token) {
        if(new DBpediaToken().updateTokenScore(token, -1))
            return ok("200");
        return ok("500");
    }

    public Result addToken(String token) {
        DBpediaToken savedToken = new DBpediaToken().findByName(token);
        if(savedToken == null) {
            DBpediaToken dbType = new DBpediaToken(token, "custom", 1);
            dbType.save();
        } else {
            new DBpediaToken().updateTokenScore(token, 1);
        }
        return ok("200");
    }
}
