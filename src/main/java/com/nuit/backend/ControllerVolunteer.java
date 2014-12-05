package com.nuit.backend;

import java.net.UnknownHostException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
//TODO: move the DB relation into a service ! If we want to do proper MVC...
@RestController
@RequestMapping("/volunteers")
public class ControllerVolunteer {
	
	private static Logger LOGGER = Logger.getLogger(ControllerVolunteer.class);
	private static DBCollection VOLUNTEER_COLLECTION ;
	
	/**
	 * MongoDB informations 
	 */
	private static String DB_NAME = "NUIT";
	private static String COLLECTION_NAME = "volunteer";
	
	
	/**
	 * Constructor. Connect to the DB
	 */
	public ControllerVolunteer() {
		MongoClient mongo = null;
		try {
			String vcap = System.getenv("VCAP_SERVICES");
			if (vcap!=null){
				JSONObject vcapServices = new JSONObject(vcap);
				if (vcapServices.has("mongodb-2.4")) {
					JSONObject credentials = vcapServices.getJSONArray("mongodb-2.4").getJSONObject(0).getJSONObject("credentials");
					String connURL = credentials.getString("url");
			        mongo = new MongoClient(new MongoClientURI(connURL));
				}
			} else {
			   mongo = new MongoClient( "localhost" , 27017 );
			}
			 DB db = mongo.getDB(DB_NAME);
			 VOLUNTEER_COLLECTION = db.getCollection(COLLECTION_NAME);
			 
		} catch (UnknownHostException e) {
			LOGGER.error("Connection to MongoDB failed");
		}		
	}
	
	/**
	 * Get a path from the DB, based on its id
	 * @param pathId
	 * @return
	 */
	@RequestMapping(value="/{id}", method=RequestMethod.GET)
	public String getVolunteerById(@PathVariable("id") UUID pathId) {
		LOGGER.info("Get request on path [" + pathId + "]");
		
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("id", pathId.toString());
	 
		DBCursor cursor = VOLUNTEER_COLLECTION.find(searchQuery);
		 
		String pathResult = null ;
		if (cursor.hasNext()) {
			 pathResult = cursor.next().toString();
			LOGGER.info("Found path : " + pathResult + " in DB");
		}
		
		return pathResult;
	}
	
	/**
	 * GET /volunteer/missions/:volunteerid
	 */
	@RequestMapping(value="/missions/{volunteerid}", method=RequestMethod.GET)
	public String getVolunteerMissions (@PathVariable("volunteerid") UUID volunteerId){
		
		LOGGER.info("Get request on path [" + volunteerId + "]");
		String volunteer = this.getVolunteerById(volunteerId);
		if (volunteer != null){
			JSONObject jsonVol = new JSONObject(volunteer);
			JSONArray missionArray = jsonVol.getJSONArray("missions");
			
			if (missionArray != null){
				JSONArray list = new JSONArray();
	
				for (int i = 0; i < missionArray.length(); i++) {
					JSONObject obj = missionArray.getJSONObject(i);
					String mission = ControllerOffer.getMission(UUID.fromString((String) obj.get("missionid")));
					JSONObject item = new JSONObject();
					item.put("mission", mission);
					item.put("present",obj.get("present"));
					list.put(item);
				}
			
				JSONObject result = new JSONObject() ;	
				result.put("missions", list);
				LOGGER.info("Returned : " + result.toString());
				return result.toString();
			}
		}
		return "No missions";
	}
	
	/**
	 * Get an array of path from the DB, based on the distance to the lat and long
	 * @param latitude and longitude
	 * @param distance in km from latitude and longitude where paths are wanted
	 * @return
	 */
	@RequestMapping(value="/all", method=RequestMethod.GET)
	public String getVolunteers() {
		LOGGER.info("Get request on all paths");	
		 
		BasicDBObject searchQuery = new BasicDBObject();
	 
		DBCursor cursor = VOLUNTEER_COLLECTION.find(searchQuery);

		JSONObject result = new JSONObject() ;
		JSONArray pathsArray = new JSONArray();
		while (cursor.hasNext()) {
			JSONObject path = new JSONObject(cursor.next().toString());
			 pathsArray.put(path);
			LOGGER.info("Found path: " + path.toString() + " in DB");
		}
		
		result.put("volunteers", pathsArray);
		LOGGER.info("Returned : " + result.toString());
		return result.toString();
	}
	
	/**
	 * Create a path in the DB from the request body
	 * @param path
	 */
	@RequestMapping(method=RequestMethod.POST)
	public void createVolunteer(@RequestBody String info) {
		LOGGER.info("POST request received with body [" + info+ "]");
		try {
			JSONArray listOfMissions= new JSONArray();
			JSONObject pathJSON = new JSONObject(info);
			UUID id = UUID.randomUUID();
			pathJSON.put("id", id);
			pathJSON.put("listOfMissions", listOfMissions);
			BasicDBObject pathDB = (BasicDBObject) com.mongodb.util.JSON.parse(pathJSON.toString());
			VOLUNTEER_COLLECTION.insert(pathDB);	
			LOGGER.info("Path [" + pathJSON.toString() + "] added to DB");
		}
		catch(JSONException e) {
			LOGGER.error("Path format in request body is not a valid JSON Object"+ e);
		}		
	}
	
	
	/**
	 * Create a path in the DB from the request body
	 * @param path
	 */
	@RequestMapping(value="/addMission",method=RequestMethod.POST)
	public void addMissionToVolunteer(@RequestBody String info) {
		LOGGER.info("POST request received with body [" + info+ "]");
		try {
			
			JSONObject pathJSON = new JSONObject(info);
			UUID userId=UUID.fromString( pathJSON.get("userId").toString());
			UUID missionId=UUID.fromString( pathJSON.get("missionId").toString());
			LOGGER.warn("userId : " + userId + " et missionId " + missionId);
			DBObject mission = new BasicDBObject("listOfMissions", new BasicDBObject("missionId",missionId.toString()).append("present",false));
			DBObject updateQuery = new BasicDBObject("$push", mission);
			DBObject userIdObject= new BasicDBObject("id",userId.toString());
			LOGGER.warn("userIdObj : " + userIdObject + " et updateQuery " + updateQuery);
			VOLUNTEER_COLLECTION.update( userIdObject, updateQuery);
			
				
			LOGGER.info("Path [" + mission.toString() + "] added to DB");
		}
		catch(JSONException e) {
			LOGGER.error("Path format in request body is not a valid JSON Object"+ e);
		}		
	}

}