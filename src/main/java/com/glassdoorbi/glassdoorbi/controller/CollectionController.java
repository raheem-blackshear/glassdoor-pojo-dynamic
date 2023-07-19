package com.glassdoorbi.glassdoorbi.controller;

import com.glassdoorbi.glassdoorbi.adapter.Marshallers;
import com.glassdoorbi.glassdoorbi.consumer.CsvParser;
import com.glassdoorbi.glassdoorbi.consumer.Downloader;
import com.glassdoorbi.glassdoorbi.consumer.Parsers;
import com.glassdoorbi.glassdoorbi.consumer.PojoGenerator;
import com.glassdoorbi.glassdoorbi.mongo.MongoUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.glassdoorbi.glassdoorbi.service.StorageService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.json.JSONTokener;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@Controller
public class CollectionController {

	@Autowired
	private StorageService storageService;
	@Autowired
	private CsvParser csvParser;
	private Downloader downloader;
	private Parsers parser = new Parsers();
	public static final String uploadingdir = System.getProperty("user.dir") + "/uploadingdir/";
	private MongoUtils mongoUtils;

	@RequestMapping({ "/", "/hello" })
	public String hello(Model model,
			@RequestParam(value = "name", required = false, defaultValue = "World") String name) {
		System.out.println("Hello being hit");
		model.addAttribute("locators", Marshallers.marshallMongoIterableToList(mongoUtils.getCollections()));

		return "index";
	}

	@RequestMapping("/download")
	public ResponseEntity<String> dowload(Model model,
			@RequestParam(value = "file_url", required = true) String file_url) {
		System.out.println("download being called");

		String[] params = file_url.split("&");
		Map<String, String> map = new HashMap<String, String>();
		String file_exe = "txt";
		if (params.length > 1) {
			for (String param : params) {
				String name = param.split("=")[0];
				String value = param.split("=")[1];
				if (name.equals("outputFormat")) {
					file_exe = value;
					break;
				}
			}
		}
		if (file_exe.equals("txt")) {
			if (file_url.indexOf(".csv") != -1) {
				file_exe = "csv";
			} else if (file_url.indexOf(".json") != -1) {
				file_exe = "json";
			} else if (file_url.indexOf(".xlsx") != -1)
				file_exe = "xlsx";
		}

		downloader = new Downloader();
		String file_content = downloader.downloadFile(file_url);
		if (file_exe.equals("")) {
			return ResponseEntity.badRequest().body("error");
		}
		System.out.println("ready parse");
		String jsonString = "";
		if (file_exe.equals("csv")) {
			jsonString = parser.parseCSV(file_content);
		} else if (file_exe.equals("txt")) {
			JSONParser parser1 = new JSONParser();
			try {
				// Object json = (org.json.simple.JSONObject) parser1.parse(file_content);
				org.json.simple.JSONArray array = new org.json.simple.JSONArray();
				if (file_content.charAt(0) == '[') {
					array = (org.json.simple.JSONArray) parser1.parse(file_content);

				} else {
					Object json = (org.json.simple.JSONObject) parser1.parse(file_content);
					array.add(json);
				}
				jsonString = array.toString();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// jsonString = parser.parseJSON(file_content);
			System.out.println(jsonString);
			// jsonString = parser.serilizeJSON(jsonString);

		}
		System.out.println("send res");
		System.out.println(jsonString);
		return ResponseEntity.ok(jsonString);

	}

	@RequestMapping("/get-locators")
	public ResponseEntity<String> getLocators() {
		System.out.println("Add collection being called");
		List<String> collections = Marshallers.marshallMongoIterableToList(mongoUtils.getCollections());
		MongoDatabase mongoDatabase = mongoUtils.getMongoDatabase();
		List<String> list = new ArrayList<>();
		for (int i = 0; i < collections.size(); i++) {
			MongoCollection mongoCollection = mongoDatabase.getCollection(collections.get(i));
			DistinctIterable<String> distinct = mongoCollection.distinct("locator", String.class);
			MongoCursor<String> cursor = distinct.iterator();
			while (cursor.hasNext()) {
				String temp = cursor.next();
				list.add(collections.get(i) + "." + temp);
			}
		}
		String json = new Gson().toJson(list);
		System.out.println(json);
		return ResponseEntity.ok(json);
	}

	@RequestMapping(value = "/import", method = RequestMethod.POST)
	public ResponseEntity<String> importData(@RequestParam("firstLocator") String firstLocator,
			@RequestParam("secondLocator") String secondLocator, @RequestParam("datasource") String datasource) {
		System.out.println("import data being called");
		System.out.println(firstLocator);
		System.out.println(secondLocator);
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			String cur_date = dateFormat.format(date);

			MongoCollection curCollection = mongoUtils.getMongoDatabase().getCollection(firstLocator);
			curCollection.createIndex(Indexes.geo2dsphere("geometry"));

			Document document = new Document();
			curCollection.createIndex(Indexes.hashed("locator"));
			document.put("locator", secondLocator);
			document.put("pub_date", cur_date);
			curCollection.createIndex(Indexes.hashed("_id"));
//      curCollection.createIndex(Indexes.hashed("row_id"));
			List<Map> mapList = new ArrayList<>();

			Object lastObj = JSON.parse(datasource);
			JSONArray lastArray = parser.objectToJSONArray(lastObj);
			Document tempDocument = new Document();
			for (int i = 0; i < lastArray.length(); i++) {
				Map<String, Object> documentMap = new HashMap<String, Object>();

				JSONObject tempObj = lastArray.getJSONObject(i);
//        Document rowDoc = new Document();

				for (Object key : tempObj.keySet()) {

					String keyStr = key.toString();
					Object tempObj1 = tempObj.get(keyStr);

					if (keyStr.equals("geometry")) {
						String[] array = tempObj1.toString().split(",");
						Point point = new Point(
								new Position(Double.parseDouble(array[0]), Double.parseDouble(array[1])));
						Object geoObj = JSON.parse(point.toJson());
						// rowDoc.put(keyStr,geoObj);
						documentMap.put(keyStr, geoObj);
						continue;
					}

					JSONArray lastArray1 = parser.objectToJSONArray(tempObj1);

					try {
						int len = lastArray1.length();
						for (int j = 0; j < lastArray1.length(); j++) {
							JSONObject tempObj2 = lastArray1.getJSONObject(j);

							for (Object key2 : tempObj2.keySet()) {

								String keyStr2 = key2.toString();
//                rowDoc.put(keyStr2,tempObj2.get(keyStr2).toString());
								documentMap.put(keyStr, tempObj2.get(keyStr2).toString());
//                System.out.println(keyStr2 + "@@@" + tempObj2.get(keyStr2));
							}
						}

					} catch (NullPointerException npe) {
						JSONObject lastArray11 = parser.objectToJSONObject(tempObj1);
						if (lastArray11 != null) {
							for (Object key2 : lastArray11.keySet()) {
								String keyStr2 = key2.toString();
//                rowDoc.put(keyStr2,lastArray11.get(keyStr2).toString());
								documentMap.put(keyStr, lastArray11.get(keyStr2).toString());
//                System.out.println(keyStr2+"@@@"+lastArray11.get(keyStr2));
							}
						} else {
							documentMap.put(keyStr, tempObj1);
//              System.out.println(keyStr + "@@" + tempObj1.toString());
						}
					}
				}
				mapList.add(documentMap);

//        rowDoc.put("row_id",count);
//        tempDocument.put(String.valueOf(count),documentMap);
			}

			int count = 1;
			for (Map<String, Object> item : mapList) {

				DBObject clazz = new BasicDBObject("_id", String.valueOf(count));
//        Map<String, Class<?>> props = new HashMap<String, Class<?>>();
				for (Map.Entry<String, Object> entry : item.entrySet()) {
//          props.put(entry.getKey(),String.class);
					((BasicDBObject) clazz).append(entry.getKey(), entry.getValue());
				}
				tempDocument.put(String.valueOf(count), clazz);

//        Class<?> clazz = PojoGenerator.generate("com.glassdoorbi.glassdoorbi.Pojo$Generated"+String.valueOf(count), props);
//        Object obj = clazz.newInstance();
//        for (Map.Entry<String, Object> entry : item.entrySet()) {
//          String entryKey= entry.getKey();
//          String methodName = "set"+entryKey.substring(0,1).toUpperCase() + entryKey.substring(1,entryKey.length());
//          clazz.getMethod(methodName,String.class).invoke(obj,entry.getValue().toString());
//          tempDocument.put(String.valueOf(count),clazz);
//        }
				count++;

			}
			curCollection.createIndex(Indexes.hashed("_id"));
			document.put("datasource", tempDocument);
			curCollection.insertOne(document);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body("error");
		}
		return ResponseEntity.ok("success");
	}

	@RequestMapping("/add")
	public String addcollection(Model model) {
		System.out.println("Add collection being called");
		List<String> collections = Marshallers.marshallMongoIterableToList(mongoUtils.getCollections());
		MongoDatabase mongoDatabase = mongoUtils.getMongoDatabase();

		JSONArray list = new JSONArray();

		for (int i = 0; i < collections.size(); i++) {
			JSONObject tempObject = new JSONObject();
			MongoCollection mongoCollection = mongoDatabase.getCollection(collections.get(i));
			DistinctIterable<String> distinct = mongoCollection.distinct("locator", String.class);
			MongoCursor<String> cursor = distinct.iterator();
			String locators = "";
			while (cursor.hasNext()) {
				locators += cursor.next() + ", ";
			}
			if (!locators.equals("")) {
				locators = locators.substring(0, locators.length() - 2);
			}
			tempObject.put("no", i + 1);
			tempObject.put("name", collections.get(i));
			tempObject.put("locators", locators);
			list.put(tempObject);
		}
		System.out.println(list);
//    String jsonStr = list.toString().replaceAll("&quot;","\"");
		model.addAttribute("collections", list);

		return "add";
	}

//  @RequestMapping(method = RequestMethod.POST, value = "/add")
//  public String addCollection(@RequestParam("file") MultipartFile file, @RequestParam("id") String id) {
//    System.out.println("POST endpoint being HIT!");
////    storageService.uploadFile(file);
//    csvParser.parse(file);
//    return "redirect:/";
//  }

	public CollectionController(@Autowired MongoUtils mongoUtils) {
		this.mongoUtils = mongoUtils;
	}

	@RequestMapping("/collections")
	public ResponseEntity<List<String>> getCollections() {
		return new ResponseEntity<>(Marshallers.marshallMongoIterableToList(mongoUtils.getCollections()),
				HttpStatus.OK);
	}

	@RequestMapping(value = "/addCollection", method = RequestMethod.POST)
	public RedirectView addCollection(@RequestParam("collectionName") String collectionName) {
		mongoUtils.createCollection(collectionName);
		return new RedirectView("/add");
	}

	@RequestMapping(value = "/renameCollection", method = RequestMethod.POST)
	public RedirectView renameCollection(@RequestParam("collectionName") String collectionName,
			@RequestParam("oldCollectionName") String oldCollectionName) {
		mongoUtils.renameCollection(oldCollectionName, collectionName);
		return new RedirectView("/add");
	}

	@RequestMapping(value = "/removeCollection", method = RequestMethod.POST)
	public RedirectView removeCollection(@RequestParam("collectionName") String collectionName) {
		mongoUtils.removeCollection(collectionName);
		return new RedirectView("/add");
	}

/*	@RequestMapping(value = "/saveRecords", method = RequestMethod.POST)
	public ResponseEntity<String> saveRecords(@RequestParam("datasource") String json,
			@RequestParam("collectionName") String collectionName) {
		collectionName = collectionName.replace(".", "");
		JSONParser parser = new JSONParser();
		System.out.println(json);
		try {
			if (json.charAt(0) == '[') {
				List<Document> list = new ArrayList<Document>();
				org.json.simple.JSONArray jsonArray = (org.json.simple.JSONArray) parser.parse(json);
				if (jsonArray != null) {
					for (Object jObj : jsonArray) {
						if (jObj != null) {
							org.json.simple.JSONObject jsonObj = (org.json.simple.JSONObject) jObj;
							Document doc = Document.parse(jsonObj.toString());
							list.add(doc);
						}
					}
					mongoUtils.createDocuments(collectionName, list);
				}
			} else {
				org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) parser.parse(json);
				mongoUtils.createDocument(collectionName, jsonObject);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok("success");
	}
*/
}
