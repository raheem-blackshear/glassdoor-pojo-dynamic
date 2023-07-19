package com.glassdoorbi.glassdoorbi.mongo;

import com.glassdoorbi.glassdoorbi.adapter.Marshallers;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCollection;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

public class MongoDocumentUtils {

  public static List<String> parseIdentifiers(MongoCollection<Document> collection) {
    System.out.println("collection size" + collection.countDocuments());
    List<String> identifiers = new ArrayList();
    for (Document document : collection.find()) {
      JsonElement jsonElement = new JsonParser().parse(document.toJson());
      JsonObject jsonObject = jsonElement.getAsJsonObject();
      jsonObject.keySet().remove("_id");
      identifiers.addAll(jsonObject.keySet());
    }
    return identifiers;
  }

  public static void insertIdentifier(MongoCollection<Document> collection, String identifier) {
    System.out.println("Inserting identifier " + identifier);
    Document dbObject = new Document();
    dbObject.put("_id", identifier);
    dbObject.put("records", new ArrayList<>());
    collection.insertOne(dbObject);
  }

  public static List<String> getRecords(MongoCollection<Document> collection, String identifier) {
    Document searchQuery = new Document();
    searchQuery.put("_id", identifier);
    return Marshallers.marshallFindIterableToList(collection.find(searchQuery));
  }
}
