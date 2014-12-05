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
import com.mongodb.MongoClientURI;
//TODO: move the DB relation into a service ! If we want to do proper MVC...
@RestController
@RequestMapping("/orga")
public class ControllerOrganization {
	
	private static Logger LOGGER = Logger.getLogger(ControllerOrganization.class);
	private static DBCollection ORGANIZATION_COLLECTION ;
	
	/**
	 * MongoDB informations 
	 */
	private static String DB_NAME = "NUIT";
	private static String COLLECTION_NAME = "organization";
	
	
	/**
	 * Constructor. Connect to the DB
	 */
	public ControllerOrganization() {try {
			MongoClient mongo = new MongoClient();

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
			 PATHS_COLLECTION = db.getCollection(COLLECTION_NAME);
			 
		} catch (UnknownHostException e) {
			LOGGER.error("Connection to MongoDB failed");
		} catch (Exception e) {
			LOGGER.error("Failed: " + e.getMessage());
	        e.printStackTrace();
		}		
	}
	
	/**
	 * Get a path from the DB, based on its id
	 * @param pathId
	 * @return
	 */
	@RequestMapping(value="/{id}", method=RequestMethod.GET)
	public String getMissionById(@PathVariable("id") UUID pathId) {
		LOGGER.info("Get request on path [" + pathId + "]");
		
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("id", pathId.toString());
	 
		DBCursor cursor = ORGANIZATION_COLLECTION.find(searchQuery);
		 
		String pathResult = null ;
		if (cursor.hasNext()) {
			 pathResult = cursor.next().toString();
			LOGGER.info("Found path : " + pathResult + " in DB");
		}
		
		return pathResult;
	}
	
	public static UUID getIdFromName (String name) {
		/*JSONObject query = new JSONObject("{'name':"+ name + "}");
		JSONObject fields = new JSONObject("{_id:0, id:1}");
		BasicDBObject queryDB = (BasicDBObject) com.mongodb.util.JSON.parse(criteria.toString());
		BasicDBObject fieldsDB = (BasicDBObject) com.mongodb.util.JSON.parse(option.toString());*/
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("name", name);
		/*BasicDBObject fields = new BasicDBObject();
		searchQuery.put("_id", 0);*/
	 
		DBCursor cursor = ORGANIZATION_COLLECTION.find(searchQuery);
		 
		String pathResult = null ;
		if (cursor.hasNext()) {
			 pathResult = cursor.next().toString();
			LOGGER.info("Found path : " + pathResult + " in DB");
		}
		return  UUID.fromString((String) new JSONObject(pathResult).get("id"));
	}
	
	/*
	/**
	 * Get an array of path from the DB, based on the distance to the lat and long
	 * @param latitude and longitude
	 * @param distance in km from latitude and longitude where paths are wanted
	 * @return
	 *
	@RequestMapping(method=RequestMethod.GET)
	public String getPathsByLocation(@RequestParam("lat") float latitude, @RequestParam("long") float longitude, 
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
	 
		DBCursor cursor = ORGANIZATION_COLLECTION.find(searchQuery);
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
	*/
	/*
	/**
	 * Get an array of path from the DB, based on the distance to the lat and long
	 * @param latitude and longitude
	 * @param distance in km from latitude and longitude where paths are wanted
	 * @return
	 *
	@RequestMapping(value="/all", method=RequestMethod.GET)
	public String getPaths() {
		LOGGER.info("Get request on all paths");	
		 
		// variation of .001 in lat or long is a 100m variation on earth's surface
//		float dist = distance * 0.00001f;
		BasicDBObject searchQuery = new BasicDBObject();
//		List<BasicDBObject> searchArguments = new ArrayList<BasicDBObject>();
//		searchArguments.add(new BasicDBObject("checkpoints.0.latitude", new BasicDBObject("$lte", latitude + dist)
//			.append("$gte", latitude - dist)));
//		searchArguments.add(new BasicDBObject("checkpoints.0.longitude", new BasicDBObject("$lte", longitude + dist)
//		.append("$gte", longitude - dist)));
//		searchQuery.put("$and", searchArguments);
	 
		DBCursor cursor = ORGANIZATION_COLLECTION.find(searchQuery);
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
	*/
	/**
	 * Create an orga in the DB from the request body (name+description)
	 * @param path
	 */
	@RequestMapping(value="/missions",method=RequestMethod.POST)
	public void createMission(@RequestBody String info) {
		LOGGER.info("POST request received with body [" + info+ "]");
		try {
			JSONObject pathJSON = new JSONObject(info);
			UUID id = UUID.randomUUID();
			pathJSON.put("id", id);
			BasicDBObject pathDB = (BasicDBObject) com.mongodb.util.JSON.parse(pathJSON.toString());
			ORGANIZATION_COLLECTION.insert(pathDB);	
			LOGGER.info("Path [" + pathJSON.toString() + "] added to DB");
		}
		catch(JSONException e) {
			LOGGER.error("Path format in request body is not a valid JSON Object"+ e);
		}		
	}
	/**
	 * Create an orga in the DB from the request body (name+description)
	 * @param path
	 */
	@RequestMapping(value="",method=RequestMethod.POST)
	public void createOrganization(@RequestBody String info) {
		LOGGER.info("POST request received with body [" + info+ "]");
		try {
			JSONObject pathJSON = new JSONObject(info);
			UUID id = UUID.randomUUID();
			pathJSON.put("id", id);
			BasicDBObject pathDB = (BasicDBObject) com.mongodb.util.JSON.parse(pathJSON.toString());
			ORGANIZATION_COLLECTION.insert(pathDB);	
			LOGGER.info("Path [" + pathJSON.toString() + "] added to DB");
		}
		catch(JSONException e) {
			LOGGER.error("Path format in request body is not a valid JSON Object"+ e);
		}		
	}
}