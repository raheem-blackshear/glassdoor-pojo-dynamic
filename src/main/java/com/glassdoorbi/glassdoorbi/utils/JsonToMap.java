package com.glassdoorbi.glassdoorbi.utils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonToMap {

	public static Map<String, Class<?>> returnMap(String json) throws ParseException {
		org.json.simple.JSONObject obj = null;
		JSONParser jsonParser = new JSONParser();
		if (json.charAt(0)=='[') {
			org.json.simple.JSONArray jsonArray = (org.json.simple.JSONArray) jsonParser.parse(json);
			if (jsonArray.size() > 0) {
				obj = (org.json.simple.JSONObject) jsonArray.get(0);
			}
		} else {
			obj = (org.json.simple.JSONObject) jsonParser.parse(json);
		}

		Set<String> keys = obj.keySet();
		Map<String, Class<?>> props = new HashMap<String, Class<?>>();
		System.out.println("key : " + keys);
		for (String key : keys) {
			Object aObj = obj.get(key);
			if (aObj instanceof Integer) {
				props.put(key, Integer.class);
			}
			if (aObj instanceof String) {
				props.put(key, String.class);
			}
			if (aObj instanceof Boolean) {
				props.put(key, Boolean.class);
			}
			if (aObj instanceof Double) {
				props.put(key, Double.class);
			}
		}
		Class<?> clazz;
		try {
			clazz = PojoGenerator.generate("net.javaforge.blog.javassist.Pojo$Generated", props);
			Object obj1 = clazz.newInstance();

			System.out.println("Clazz: " + clazz);
			System.out.println("Object: " + obj1);
			System.out.println("Serializable? " + (obj1 instanceof Serializable));

			for (final Method method : clazz.getDeclaredMethods()) {
				System.out.println(method);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//
		return props;
	}
}
