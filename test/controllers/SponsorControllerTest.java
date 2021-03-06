package controllers;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.callAction;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;
import static play.test.Helpers.running;
import static play.test.Helpers.status;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.util.Constantes;

import org.codehaus.jackson.JsonNode;
import org.junit.Assert;
import org.junit.Test;

import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import util.TestUtils;

import com.mongodb.BasicDBObject;

/**
 * <p>Tests fonctionnels pour le controleur {@link SponsorController}</p>
 * 
 * @author Martial SOMDA
 *
 */
public class SponsorControllerTest {

	@Test
    public void test_sponsor_list() throws IOException {

        running(fakeApplication(), new Runnable() {
            public void run() {
                try {
                	TestUtils.updateDatabase("test/data/sponsor.js");
                    Result result = route(fakeRequest(GET, "/sponsor/list"));
                    assertThat(status(result)).isEqualTo(OK);
                    assertThat(contentType(result)).isEqualTo("application/json");
                    JsonNode jsonNode = Json.parse(contentAsString(result));
                    Assert.assertEquals(3, jsonNode.size());

                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }
            }
        });
    }
	
	/*@Test
    public void test_sponsor_new_forbidden() {
		Logger.info("Une requête de création d'un sponsor requiert les droits d'administration");
        running(fakeApplication(), new Runnable() {
            public void run() {
                Result result = route(fakeRequest(POST, "/sponsor/new"));
                assertThat(status(result)).isEqualTo(FORBIDDEN);
            }
        });
    }*/
	
	@Test
    public void test_sponsor_new_ok() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                Logger.info("Création d'un nouveau sponsor");
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", "email@test.com");
                params.put("name", "HTTT");
                params.put("logo", "HTTT");
                params.put("level", "HTTT");
                params.put("website", "www.test.com");
                params.put("city", "HTTT");
                params.put("country", "HTTT");
                params.put("phone", "HTTT");
                params.put("about", "HTTT");
                Result result = callAction(routes.ref.SponsorController.addSponsor(), fakeRequest().withJsonBody(Json.toJson(params), POST).withSession("admin", "admin"));
                assertThat(status(result)).isEqualTo(OK);

                Logger.info("Vérification que le nouveau sponsor a bien été sauvegardé en base de données");
                List<BasicDBObject> dbObjects = TestUtils.loadFromDatabase(Constantes.COLLECTION_SPONSOR, new BasicDBObject().append("email", "email@test.com"));
                Assert.assertTrue(null != dbObjects);
                Assert.assertEquals(1,dbObjects.size());
                Assert.assertEquals("www.test.com",dbObjects.get(0).get("website"));

            }
        });
    }
	
	@Test
    public void test_sponsor_new_badrequest() {
        running(fakeApplication(), new Runnable() {
            public void run() {
                Logger.info("Création d'un nouveau sponsor");
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", "email@test.com");
                params.put("level", "HTTT");
                params.put("website", "www.test.com");
                params.put("city", "HTTT");
                params.put("country", "HTTT");
                params.put("phone", "HTTT");
                params.put("about", "HTTT");
                Result result = route(fakeRequest(POST, "/sponsor/new").withJsonBody(Json.toJson(params)).withSession("admin", "admin"));
                assertThat(status(result)).isEqualTo(BAD_REQUEST);
            }
        });
    }
	
	@Test
    public void test_sponsor_remove_ok() {
        running(fakeApplication(), new Runnable() {
            public void run() {              
                try {
                	Logger.info("Supression d'un sponsor");
					TestUtils.updateDatabase("test/data/sponsor.js");
					Map<String, String> params = new HashMap<String, String>();
	                params.put("email", "test@sponsor.com");
	                Result result = callAction(routes.ref.SponsorController.removeSponsor(), fakeRequest().withJsonBody(Json.toJson(params),POST).withSession("admin", "admin"));
	                assertThat(status(result)).isEqualTo(OK);
	                
	                Logger.info("Vérification que le sponsor a bien été supprimmé en base de données");
	                List<BasicDBObject> dbObjects = TestUtils.loadFromDatabase(Constantes.COLLECTION_SPONSOR, new BasicDBObject().append("email", "test@sponsor.com"));
	                Assert.assertTrue(null != dbObjects);
	                Assert.assertEquals(0,dbObjects.size());
				} catch (IOException e) {
					Assert.fail(e.getMessage());
				}
                

            }
        });
    }
	
	/*@Test
    public void test_sponsor_remove_forbidden() {
		Logger.info("Une requête de suppression d'un sponsor requiert les droits d'administration");
        running(fakeApplication(), new Runnable() {
            public void run() {
                Result result = route(fakeRequest(POST, "/sponsor/remove"));
                assertThat(status(result)).isEqualTo(FORBIDDEN);
            }
        });
    }*/
	
	@Test
    public void test_sponsor_update_ok() {
        running(fakeApplication(), new Runnable() {
            public void run() {              
                try {
                	Logger.info("Supression d'un sponsor");
					TestUtils.updateDatabase("test/data/sponsor.js");
					Map<String, String> params = new HashMap<String, String>();
	                params.put("email", "test@sponsor.com");
	                params.put("city", "myNewCity");
	                params.put("country", "myNewCoutry");
	                params.put("phone", "0504030201");
	                Result result = callAction(routes.ref.SponsorController.updateSponsor(), fakeRequest().withJsonBody(Json.toJson(params),POST).withSession("admin", "admin"));
	                assertThat(status(result)).isEqualTo(OK);
	                
	                Logger.info("Vérification que le sponsor a bien été modifié en base de données");
	                List<BasicDBObject> dbObjects = TestUtils.loadFromDatabase(Constantes.COLLECTION_SPONSOR, new BasicDBObject().append("email", "test@sponsor.com"));
	                Assert.assertTrue(null != dbObjects);
	                Assert.assertEquals(1,dbObjects.size());
	                BasicDBObject dbObject = dbObjects.get(0);
	                Assert.assertEquals("test@sponsor.com",dbObject.getString("email"));
	                Assert.assertEquals("Test SA",dbObject.getString("name"));
	                Assert.assertEquals("http://www.test.com/icons/logo.gif",dbObject.getString("logo"));
	                Assert.assertEquals("Platinium",dbObject.getString("level"));
	                Assert.assertEquals("www.test.com",dbObject.getString("website"));
	                Assert.assertEquals("myNewCity",dbObject.getString("city"));
	                Assert.assertEquals("myNewCoutry",dbObject.getString("country"));
	                Assert.assertEquals("0504030201",dbObject.getString("phone"));
	                Assert.assertEquals("All about test",dbObject.getString("about"));
				} catch (IOException e) {
					Assert.fail(e.getMessage());
				}
                

            }
        });
    }
	
	/*@Test
    public void test_sponsor_update_forbidden() {
		Logger.info("Une requête de mise à jour d'un sponsor requiert les droits d'administration");
        running(fakeApplication(), new Runnable() {
            public void run() {
                Result result = route(fakeRequest(POST, "/sponsor/update"));
                assertThat(status(result)).isEqualTo(FORBIDDEN);
            }
        });
    }*/
}
