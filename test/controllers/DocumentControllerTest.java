package controllers;

import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.ws.WSClient;
import play.mvc.Result;
import play.test.WithApplication;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;

/**
 * Created by mahabaleshwar on 6/23/2016.
 */
public class DocumentControllerTest extends WithApplication {

    private WSClient ws;

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().configure("play.http.router", "router.Routes").build();
    }

    /*@Test
    public void testProcessDocument() {
        Result result = new DocumentController().processDocument();
        assertEquals(OK, result.status());
    }*/

    @Test
    public void testGetMetaInformation() {
        ws = provideApplication().injector().instanceOf(WSClient.class);
        Result result = new DocumentController().getMetaInformation();
        assertEquals(OK, result.status());
    }

}
