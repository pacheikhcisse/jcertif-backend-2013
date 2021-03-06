package models.objects.access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import models.database.MongoDatabase;
import models.exception.JCertifDuplicateObjectException;
import models.exception.JCertifException;
import models.exception.JCertifObjectNotFoundException;
import models.objects.JCertifObject;
import models.objects.checker.Checker;
import models.util.Tools;
import play.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.WriteResult;

public abstract class JCertifObjectDB<T extends JCertifObject> implements
		IDao<BasicDBObject> {

	private Checker checker;
	private String collectionName;
	
	public JCertifObjectDB(String collectionName) {
        super();
		this.collectionName = collectionName;
		checker = null;
	}

	public JCertifObjectDB(String collectionName, Checker checker) {
        super();
		this.collectionName = collectionName;
		this.checker = checker;
	}

	public final Checker getChecker() {
		return checker;
	}

	protected final void setChecker(Checker checker1) {
		this.checker = checker1;
	}

	/**
	 * 
	 * @param query
	 * @param columnToReturn
	 * @return
	 */
	@Override
	public final List<BasicDBObject> list(BasicDBObject query,
			BasicDBObject columnToReturn) {
		DBCursor dbCursor = MongoDatabase.getInstance().list(
				getCollectionName(), query, columnToReturn);
		BasicDBObject object;
		List<BasicDBObject> resultList = new ArrayList<BasicDBObject>();
		while (dbCursor.hasNext()) {
			object = (BasicDBObject) dbCursor.next();
			resultList.add(object);
		}
		return resultList;
	}

	@Override
	public final List<BasicDBObject> list() {
		DBCursor dbCursor = MongoDatabase.getInstance().list(
				getCollectionName());
		BasicDBObject object;
		List<BasicDBObject> resultList = new ArrayList<BasicDBObject>();
		while (dbCursor.hasNext()) {
			object = (BasicDBObject) dbCursor.next();
			resultList.add(object);
		}
		return resultList;
	}

	@Override
	public final List<BasicDBObject> list(BasicDBObject query) {
		DBCursor dbCursor = MongoDatabase.getInstance().list(
				getCollectionName(), query);
		BasicDBObject object;
		List<BasicDBObject> resultList = new ArrayList<BasicDBObject>();
		while (dbCursor.hasNext()) {
			object = (BasicDBObject) dbCursor.next();
			resultList.add(object);
		}
		return resultList;
	}
	
	@Override
	public final BasicDBObject get(String keyName, Object keyValue) {
		if (null == keyName){
			return null;
        }
		BasicDBObject dbObject = new BasicDBObject();
		dbObject.put(keyName, keyValue);
		
		return get(dbObject);
	}
	
	public final BasicDBObject get(BasicDBObject objectToGet) {
		if (null == objectToGet){
			return null;
        }

		/* If the object does not exist, null is returned */
		return MongoDatabase.getInstance().readOne(
				getCollectionName(), objectToGet);
	}

	@Override
	public final boolean add(BasicDBObject objectToAdd, String idKeyname) {
		getChecker().addCheck(objectToAdd);		
		BasicDBObject dbObject = new BasicDBObject();
		dbObject.put(idKeyname, objectToAdd.get(idKeyname));
		BasicDBObject existingObjectToAdd = MongoDatabase.getInstance()
				.readOne(getCollectionName(), dbObject);
		if (null != existingObjectToAdd) {
			throw new JCertifDuplicateObjectException(this, "Object '" + existingObjectToAdd.getString(idKeyname) + "' already exists");
		}
		
		WriteResult result = MongoDatabase.getInstance().create(
				getCollectionName(), objectToAdd);
		if (!Tools.isBlankOrNull(result.getError())) {
			throw new JCertifException(this, result.getError());
		}
		return true;
	}

	/**
	 * This method sould be revisited
	 * 
	 * @param objectToUpdate
	 * @param idKeyname
	 * @return
	 * @throws JCertifException
	 */
	@Override
	public final boolean update(BasicDBObject objectToUpdate, String idKeyname) {
		getChecker().updateCheck(objectToUpdate);
		BasicDBObject dbObject = new BasicDBObject();
		dbObject.put(idKeyname, objectToUpdate.get(idKeyname));
		BasicDBObject existingObjectToUpdate = MongoDatabase.getInstance()
				.readOne(getCollectionName(), dbObject);
		if (null == existingObjectToUpdate) {
			Logger.info("not found");
			throw new JCertifObjectNotFoundException(this, "Object to update does not exist");
		}

		existingObjectToUpdate = merge(objectToUpdate,existingObjectToUpdate);

		WriteResult result = MongoDatabase.getInstance().update(
				getCollectionName(), existingObjectToUpdate);
		if (!Tools.isBlankOrNull(result.getError())) {
			throw new JCertifException(this, result.getError());
		}
		return true;
	}

	/**
	 * @param objectToUpdate
	 * @param existingObjectToUpdate
	 * @return
	 */
	private BasicDBObject merge(BasicDBObject objectToUpdate,
			BasicDBObject existingObjectToUpdate) {
		
		Map<String, Object> fieldMap = objectToUpdate.toMap();
		Map<String, Object> fieldToSaveMap = new HashMap<String, Object>();
		for (Entry<String, Object> entry : (Set<Entry<String, Object>>)fieldMap.entrySet()) {
			if(entry.getValue() != null && 
					(!(entry.getValue() instanceof List<?>)||
					((entry.getValue() instanceof List<?>) &&
							!Tools.isBlankOrNull((ArrayList<?>)entry.getValue())))){
				fieldToSaveMap.put(entry.getKey(), entry.getValue());
			}
		}
		existingObjectToUpdate.putAll(fieldToSaveMap);
		
		return existingObjectToUpdate;
	}

	@Override
	public final boolean remove(BasicDBObject objectToDelete, String idKeyname) {
		getChecker().deleteCheck(objectToDelete);
		BasicDBObject dbObject = new BasicDBObject();
		dbObject.put(idKeyname, objectToDelete.get(idKeyname));
		BasicDBObject existingObjectToDelete = MongoDatabase.getInstance()
				.readOne(getCollectionName(), dbObject);
		
		if (null == existingObjectToDelete) {
			throw new JCertifObjectNotFoundException(this, "Object to delete does not exist");
		}

		WriteResult result = MongoDatabase.getInstance().delete(
				getCollectionName(), existingObjectToDelete);
		if (!Tools.isBlankOrNull(result.getError())) {
			throw new JCertifException(this, result.getError());
		}
		return true;
	}

	public final String getCollectionName() {
		return collectionName;
	}

	public final void setCollectionName(String collectionName1) {
		this.collectionName = collectionName1;
	}
}
