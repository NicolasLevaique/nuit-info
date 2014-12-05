package com.nuit.backend;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
//TODO: move the DB relation into a service ! If we want to do proper MVC...
@RestController
@RequestMapping("/missions")
public class ControllerOffer {
	
	private static Logger LOGGER = Logger.getLogger(ControllerOffer.class);
	private static DBCollection OFFER_COLLECTION ;
	
	/**
	 * MongoDB informations 
	 */
	private static String DB_NAME = "NUIT";
	private static String COLLECTION_NAME = "missions";
	
	
	/**
	 * Constructor. Connect to the DB
	 */
	public ControllerOffer() {
		MongoClient mongo;
		try {
			 mongo = new MongoClient( "localhost" ,27017 );
			 DB db = mongo.getDB(DB_NAME);
			 OFFER_COLLECTION = db.getCollection(COLLECTION_NAME);
			 
		} catch (UnknownHostException e) {
			LOGGER.error("Connection to MongoDB failed");
		}		
	}
	/**
	 * DONE
	 * Get a path from the DB, based on its id
	 * @param pathId
	 * @return
	 */
	@RequestMapping(value="/{id}", method=RequestMethod.GET)
	public String getMission(@PathVariable("id") UUID missionId) {
		LOGGER.info("Get request on mission [" + missionId + "]");
		
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("id", missionId.toString());
	 
		DBCursor cursor = OFFER_COLLECTION.find(searchQuery);
		 
		String missionResult = null ;
		if (cursor.hasNext()) {
			 missionResult = cursor.next().toString();
			LOGGER.info("Found mission : " + missionResult + " in DB");
		}
		
		return missionResult;
	}
	
	
	/**
	 * Get an array of path from the DB, based on the distance to the lat and long
	 * @param latitude and longitude
	 * @param distance in km from latitude and longitude where paths are wanted
	 * @return
	 */
	@RequestMapping(method=RequestMethod.GET)
	public String getMissionsByLocation(@RequestParam("lat") float latitude, @RequestParam("long") float longitude, 
			@RequestParam(value = "dist") float distance) {
		LOGGER.info("Get request on path from location : [latitude:" + latitude + ", longitude:" + longitude + "]");	
		 
		// variation of .001 in lat or long is a 100m variation on earth's surface
		float dist = distance * 0.00001f;
		BasicDBObject searchQuery = new BasicDBObject();
		List<BasicDBObject> searchArguments = new ArrayList<BasicDBObject>();
		searchArguments.add(new BasicDBObject("checkpoints.0.latitude", new BasicDBObject("$lte", latitude + dist)
			.append("$gte", latitude - dist)));
		searchArguments.add(new BasicDBObject("checkpoints.0.longitude", new BasicDBObject("$lte", longitude + dist)
		.append("$gte", longitude - dist)));
		searchQuery.put("$and", searchArguments);
	 
		DBCursor cursor = OFFER_COLLECTION.find(searchQuery);
//		 
		JSONObject result = new JSONObject() ;
		JSONArray pathsArray = new JSONArray();
		while (cursor.hasNext()) {
			JSONObject path = new JSONObject(cursor.next().toString());
			 pathsArray.put(path);
			LOGGER.info("Found path: " + path.toString() + " in DB");
		}
		
		result.put("paths", pathsArray);
		LOGGER.info("Returned : " + result.toString());
		return result.toString();
	}
	
	/**
	 * DONE
	 * @return
	 */
	@RequestMapping(value="/all", method=RequestMethod.GET)
	public String getMissions() {
		LOGGER.info("Get request on all missions");	
		BasicDBObject searchQuery = new BasicDBObject();

	 
		DBCursor cursor = OFFER_COLLECTION.find(searchQuery);
//		 
		JSONObject result = new JSONObject() ;
		JSONArray missionsArray = new JSONArray();
		while (cursor.hasNext()) {
			JSONObject mission = new JSONObject(cursor.next().toString());
			 missionsArray.put(mission);
			LOGGER.info("Found mission: " + mission.toString() + " in DB");
		}
		
		result.put("missions", missionsArray);
		LOGGER.info("Returned : " + result.toString());
		return result.toString();
	}
	
	/**
	 * Get a path from the DB, based on its id
	 * @param pathId
	 * @return
	 */
	@RequestMapping(value="/organization/{organizationId}", method=RequestMethod.GET)
	public String getMissionsFromOrganization(@PathVariable("organizationId") UUID organizationId) {
		LOGGER.info("Get request on path [" + organizationId + "]");
		
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("organization", organizationId.toString());
	 
		DBCursor cursor = OFFER_COLLECTION.find(searchQuery);
		 
		JSONArray offers = new JSONArray() ;
		while (cursor.hasNext()) {
			 offers.put(cursor.next());
			LOGGER.info("Found path : " + offers.toString() + " in DB");
		}
		
		return offers.toString();
	}
	
	/**
	 * Create a path in the DB from the request body
	 * @param path
	 */
	@RequestMapping(method=RequestMethod.POST)
	public void createMission(@RequestBody String info) {
		LOGGER.info("POST request received with body [" + info+ "]");
		try {
			JSONObject pathJSON = new JSONObject(info);
			String organization = pathJSON.getString("organization");
			
			UUID id = UUID.randomUUID();
			pathJSON.put("id", id);
			
			UUID orgaID = ControllerOrganization.getIdFromName(organization);
			
			pathJSON.put("organization", orgaID);
			BasicDBObject pathDB = (BasicDBObject) com.mongodb.util.JSON.parse(pathJSON.toString());
			OFFER_COLLECTION.insert(pathDB);	
			LOGGER.info("Path [" + pathJSON.toString() + "] added to DB");
		}
		catch(JSONException e) {
			LOGGER.error("Path format in request body is not a valid JSON Object"+ e);
		}		
	}
}