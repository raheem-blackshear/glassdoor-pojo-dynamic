package com.glassdoorbi.glassdoorbi.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;

public class Marshallers {

  public static List<String> marshallMongoIterableToList(MongoIterable<String> collections) {
    List<String> collectionlist = new ArrayList();
    MongoCursor<String> cursor = collections.iterator();
    while (cursor.hasNext()) {
      String collection = cursor.next();
      if (!collection.equals("system.indexes")) {
        collectionlist.add(collection);
      }
    }
    return collectionlist;
  }

  public static List<String> marshallFindIterableToList(FindIterable<Document> document) {
    List<String> records = new ArrayList<>();
    MongoCursor<Document> cursor = document.iterator();
    while(cursor.hasNext()) {
      Document record = cursor.next();
      System.out.println("record" + record);
      JsonElement element = new JsonParser().parse(record.toJson());
      JsonObject object = element.getAsJsonObject();
      System.out.println(object.get("records"));
      //records.addAll(object.get("records").get());
    }
    return records;
  }
}
