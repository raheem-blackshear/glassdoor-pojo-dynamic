package com.glassdoorbi.glassdoorbi.consumer;

import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import com.glassdoorbi.glassdoorbi.utils.JsonToMap;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

@Component
public class Downloader {
    public String downloadFile(String file_url){
        System.out.println(file_url);
        String[] params = file_url.split("&");
        Map<String, String> map = new HashMap<String, String>();
        String file_exe = "txt";
        if(params.length >1){
            for (String param : params)
            {
                String name = param.split("=")[0];
                String value = param.split("=")[1];
                if(name.equals("outputFormat")){
                    file_exe = value;
                    break;
                }
            }
            }
        if(file_exe.equals("txt")){
            if(file_url.indexOf(".csv") != -1){
                file_exe = "csv";
            }else if(file_url.indexOf(".json") != -1){
                file_exe = "json";
            }else if(file_url.indexOf(".xlsx") != -1)
                file_exe = "xlsx";
        }
        System.out.println(file_exe);


        String myJSON = ""; //TEMP VARIABLE TO HOLD JSON DATA
        String completeJSONdata = ""; //VARIABLE TO HOLD COMPLETE JSON DATA

        try {
            URL urlObject = new URL( file_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlObject.openConnection();
            InputStream inputStreamObject = httpURLConnection.getInputStream();
            BufferedReader bufferedReaderObject = new BufferedReader( new InputStreamReader( inputStreamObject ) );
            if(file_exe.equals("csv")){
                while (myJSON != null) {
                    myJSON = bufferedReaderObject.readLine();
                    if(myJSON!= null){
                        completeJSONdata += myJSON +"\n";
                    }
                }
            }else {
                while (myJSON != null) {
                    myJSON = bufferedReaderObject.readLine();
                    if(myJSON!= null){
                        completeJSONdata += myJSON;
                    }
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("JSON data "+completeJSONdata);
        try {
			JsonToMap.returnMap(completeJSONdata) ;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return completeJSONdata;
    }

}
