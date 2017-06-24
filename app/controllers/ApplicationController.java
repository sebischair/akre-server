package controllers;

import play.libs.concurrent.HttpExecutionContext;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

import javax.inject.Inject;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class ApplicationController extends Controller {
    @Inject HttpExecutionContext ec;
    @Inject WSClient ws;

    public Result index() {
        return ok();
    }

}
