package com.glassdoorbi.glassdoorbi.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.util.JSON;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

//TODO Add better exception handling.
@Configuration
public class MongoUtils extends AbstractMongoConfiguration {

  private String uri;

  private String databaseName;

  public MongoUtils(@Value("${spring.data.mongodb.uri}") String uri,
      @Value("${mongodb.database}") String databaseName) {
    this.uri = uri;
    this.databaseName = databaseName;
  }


  @Override
  public MongoMappingContext mongoMappingContext()
      throws ClassNotFoundException {
    // TODO Auto-generated method stub
    return super.mongoMappingContext();
  }

  @Override
  public MongoClient mongoClient() {
    return new MongoClient(new MongoClientURI(uri));
  }

  @Override
  protected String getDatabaseName() {
    return databaseName;
  }

  public MongoIterable<String> getCollections() {
    return getMongoDatabase().listCollectionNames();
  }

  public MongoCollection<Document> getCollection(String collectionName) {
	  
	    return getMongoDatabase().getCollection(collectionName);
	  }
  public MongoDatabase getMongoDatabase() {
    return mongoClient().getDatabase(getDatabaseName());
  }

  public void createCollection(String collectionName) {
    getMongoDatabase().createCollection(collectionName);
  }
  
  public void createDocument(String collectionName, org.json.simple.JSONObject jsonObj) {
	  Document doc = Document.parse( jsonObj.toString() );

	  getMongoDatabase().getCollection(collectionName).insertOne(doc);
	  }
  
  public void createDocuments(String collectionName,  List<? extends Document> documents) {
	getMongoDatabase().getCollection(collectionName).insertMany(documents) ;
	  }
  
  public void removeCollection(String collectionName) {
    getMongoDatabase().getCollection(collectionName).drop();
  }
  public void renameCollection(String oldCollectionName,String collectionName) {
    MongoCollection<Document> collection = this.getMongoDatabase().getCollection(oldCollectionName);
    MongoNamespace newName = new MongoNamespace(this.getDatabaseName() ,collectionName);
    collection.renameCollection(newName);
  }

  public MongoCollection<Document> getdocuments(String collectionName) {
    return getMongoDatabase().getCollection(collectionName);
  }

}
