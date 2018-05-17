package controllers.amelie;

import model.amelie.Issue;
import play.mvc.Controller;
import play.mvc.Result;
import util.StaticFunctions;

/**
 * Created by Manoj on 2/19/2018.
 */
public class DesignDecisionController extends Controller {

    public Result getAllDesignDecisions(String projectKey) {
        return StaticFunctions.jsonResult(ok(new Issue().findAllDesignDecisionsInAProject(projectKey)));
    }

    public Result getDesignDecision(String issueKey) {
        return StaticFunctions.jsonResult(ok(new Issue().getDesignDecisionByKey(issueKey)));
    }
}
