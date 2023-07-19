package com.glassdoorbi.glassdoorbi.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.mongodb.util.JSON;
import com.opencsv.CSVReader;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

public class Parsers {
    public String parseCSV(String content){
        try {
            System.out.println(content);
            JSONArray arr = new JSONArray();
            HashMap<String, JSONObject> map = new HashMap<String, JSONObject>();
            CSVReader csvReader = new CSVReader(new StringReader(content));
            String[] nextRecord;
            String[] columnRecord = null;
            boolean flag = false;
            int count = 0;

            while ((nextRecord = csvReader.readNext()) != null) {
                if(!flag){
                    columnRecord = nextRecord;
                    System.out.println(columnRecord);
                    flag = true;
                }else{
                    JSONObject temp = new JSONObject();
                    for(int i = 0; i < columnRecord.length; i ++){
                        temp.put(columnRecord[i],nextRecord[i]);
                    }
                    map.put("json" + count, temp);
                    arr.put(map.get("json" + count));
                    count += 1;
                }
            }
            return arr.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public String parseJSON(String content){

        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(content);

            Object obj  = JSON.parse(content);
            JSONArray lastArray = objectToJSONArray(obj);
            if(lastArray != null && lastArray.length() > 0){
                for (int i = 0; i < lastArray.length(); i ++){
                    JSONObject child = lastArray.getJSONObject(i);
                    System.out.println(child);
                }
            }

            JSONObject lastObject = objectToJSONObject(obj);

            Object finalObject = new Object();

            if(lastObject != null){
                for (Object key : lastObject.keySet()) {
                    String keyStr = key.toString().trim();
                    if(keyStr.equals("features")){
                        finalObject = lastObject.get(key.toString());
                        break;
                    }else if(keyStr.equals("data")){
                        finalObject = lastObject.get(key.toString());
                    }
                }
            }

            if (! finalObject.toString().equals("")){
                return finalObject.toString();
            }
//            int count = 1;
//            Gson gson = new GsonBuilder()
//                    .setLenient()
//                    .create();
//
//            JsonElement element = gson.fromJson (content, JsonElement.class);
//
//            JsonObject jsonObj = element.getAsJsonObject();
//            JsonElement features = jsonObj.get("features");
//            JsonElement data = jsonObj.get("data");
//            if(features != null){
//                return features.toString();
//            }else if(data != null){
//                return data.toString();
//            }

            return lastObject.toString();

        } catch (Exception e) {
            e.printStackTrace();
        return "";
    }

    }

    public JSONArray objectToJSONArray(Object object){
        Object json = null;
        JSONArray jsonArray = null;
        try {
            json = new JSONTokener(object.toString()).nextValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (json instanceof JSONArray) {
            jsonArray = (JSONArray) json;
        }
        return jsonArray;
    }

    public JSONObject objectToJSONObject(Object object){
        Object json = null;
        JSONObject jsonObject = null;
        try {
            json = new JSONTokener(object.toString()).nextValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (json instanceof JSONObject) {
            jsonObject = (JSONObject) json;
        }
        return jsonObject;
    }

    public String serilizeJSON(String jsonContent){
        List<Map> mapList = new ArrayList<>();

        Object lastObj = JSON.parse(jsonContent);
        JSONArray lastArray = objectToJSONArray(lastObj);
        for (int i =0; i < lastArray.length(); i ++){
            Map<String, Object> documentMap = new HashMap<String, Object>();

            JSONObject tempObj = lastArray.getJSONObject(i);
            for (Object key : tempObj.keySet()) {
                String keyStr = key.toString();
                Object tempObj1 = tempObj.get(keyStr);

                JSONArray lastArray1 = objectToJSONArray(tempObj1);

                try{
                    int len = lastArray1.length();
                    for (int j =0; j < lastArray1.length(); j ++){
                        JSONObject tempObj2 = lastArray1.getJSONObject(j);
                        for (Object key2 : tempObj2.keySet()) {
                            String keyStr2 = key2.toString();
                            documentMap.put(keyStr,tempObj2.get(keyStr2).toString());
                        }
                    }

                }catch (NullPointerException npe){
                    JSONObject lastArray11 = objectToJSONObject(tempObj1);
                    if(lastArray11 != null){
                        for (Object key2 : lastArray11.keySet()) {
                            String keyStr2 = key2.toString();
                            documentMap.put(keyStr,lastArray11.get(keyStr2).toString());
                        }
                    }else{
                        documentMap.put(keyStr,tempObj1);
                    }
                }
            }
            mapList.add(documentMap);
        }
        JSONArray array = new JSONArray();

        for (Map<String, Object> item : mapList) {
            JSONObject itemObj = new JSONObject();
            for (Map.Entry<String, Object> entry : item.entrySet()) {
                itemObj.put(entry.getKey(),entry.getValue());
            }
            array.put(itemObj);
        }

        return array.toString();



    }

}
