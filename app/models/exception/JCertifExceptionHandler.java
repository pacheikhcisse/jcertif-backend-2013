package models.exception;

import play.Logger;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

/**
 * <p>Classe Helper pour la gestion des exception.</p>
 * 
 * @author Martial SOMDA
 *
 */
public class JCertifExceptionHandler {

	public static Result resolve(Throwable throwable){
		
		Logger.info("JCertif ExceptionHandler");

		Result result = Results.status(Http.Status.INTERNAL_SERVER_ERROR, throwable.getMessage());
		
		Throwable unwrappedThrowable = unwrap(throwable);
		
		if(unwrappedThrowable instanceof JCertifException
				&& unwrappedThrowable.getClass().isAnnotationPresent(JCertifExceptionMapping.class)){

			JCertifExceptionMapping mapping = unwrappedThrowable.getClass().getAnnotation(JCertifExceptionMapping.class);
			
			result = Results.status(mapping.status(), throwable.getMessage());
			
		}
		
		return result;
	}

	private static Throwable unwrap(Throwable throwable) {

		Throwable unwrappedThrowable = throwable;
		
		while(unwrappedThrowable.getClass().getCanonicalName().contains("play.")){
			unwrappedThrowable = unwrappedThrowable.getCause();
		}
		return unwrappedThrowable;
	}
}
