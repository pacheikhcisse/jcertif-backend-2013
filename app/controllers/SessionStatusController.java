package controllers;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;
import models.exception.JCertifException;
import models.objects.SessionStatus;
import models.objects.access.SessionStatusDB;
import models.util.Tools;
import play.mvc.Http;
import play.mvc.Result;

public class SessionStatusController extends AbstractController {

    public static Result listStatusSession() {
        allowCrossOriginJson();
        return ok(JSON.serialize(SessionStatusDB.getInstance().list()));
    }

    public static Result addSessionStatus() {
        allowCrossOriginJson();

        Http.RequestBody requestBody = request().body();
        try {
            Tools.verifyJSonRequest(requestBody);
        } catch (JCertifException e) {
            return badRequest(e.getMessage());
        }

        String sessionStatusObjInJSONForm = requestBody.asJson().toString();
        SessionStatus sessionStatus;
        try{
            sessionStatus = new SessionStatus((BasicDBObject) JSON.parse(sessionStatusObjInJSONForm));
        }catch(JSONParseException exception){
            return badRequest(sessionStatusObjInJSONForm);
        }

        try {
            SessionStatusDB.getInstance().add(sessionStatus);
        } catch (JCertifException jcertifException) {
            return internalServerError(jcertifException.getMessage());
        }
        return ok(JSON.serialize("Ok"));
    }

    public static Result removeSessionStatus() {
        allowCrossOriginJson();

        Http.RequestBody requestBody = request().body();
        try {
            Tools.verifyJSonRequest(requestBody);
        } catch (JCertifException e) {
            return badRequest(e.getMessage());
        }

        String sessionStatusObjInJSONForm = requestBody.asJson().toString();

        SessionStatus sessionStatus = null;

        try{
            sessionStatus = new SessionStatus((BasicDBObject) JSON.parse(sessionStatusObjInJSONForm));
        }catch(JSONParseException exception){
            return badRequest(sessionStatusObjInJSONForm);
        }

        try{
            SessionStatusDB.getInstance().remove(sessionStatus);
        }catch(JCertifException jcertifException){
            return internalServerError(jcertifException.getMessage());
        }

        return ok(JSON.serialize("Ok"));
    }
}