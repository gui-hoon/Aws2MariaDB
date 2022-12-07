package com.example.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GetEntities {
	public List<String> getEntities(String environment, String entityType,
			String startTime, String endTime, String token) throws IOException, ParseException {
		String response = "";
		
		try {
			URL environment_url = new URL(environment +"/api/v2/entities?entitySelector=" + entityType
					+ "&from=" + startTime
					+ "&to=" + endTime
					+ "&Api-Token=" + token); // parameter 추가
			HttpURLConnection urlConnection = (HttpURLConnection) environment_url.openConnection();
			
			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			urlConnection.setConnectTimeout(5000); 
			urlConnection.setReadTimeout(5000);
			urlConnection.setDoOutput(true);
			
			String inputLine;			
			StringBuffer sb = new StringBuffer();
			
			Charset charset = Charset.forName("UTF-8");
			BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), charset));
			
			while ((inputLine = br.readLine()) != null) {
				sb.append(inputLine);
			}
			br.close();
			
			response = sb.toString();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<String> entityList = new ArrayList<>();
		
//		System.out.println(response);
		JSONObject results = (JSONObject) new JSONParser().parse(response);
		Object entitiesObj = results.get("entities");
		JSONArray entitiesArr = (JSONArray) entitiesObj;
		
		for (int i = 0; i < entitiesArr.size(); i++) {
			entityList.add((String) ((JSONObject) entitiesArr.get(i)).get("entityId"));
		}
//		System.out.println(entityList);
		
		return entityList;
	}
	
	public HashMap<String, String> GetEntitiesProperty(String environment, String entityId,
			String startTime, String endTime, String token) throws IOException, ParseException {
		String response = "";
		String entity_id = entityId;
		try {
			URL environment_url = new URL(environment +"/api/v2/entities/" + entity_id
					+ "?from=" + startTime
					+ "&to=" + endTime
					+ "&Api-Token=" + token); // parameter 추가
			HttpURLConnection urlConnection = (HttpURLConnection) environment_url.openConnection();
			
			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			urlConnection.setConnectTimeout(5000); 
			urlConnection.setReadTimeout(5000);
			urlConnection.setDoOutput(true);
			
			String inputLine;			
			StringBuffer sb = new StringBuffer();
			
			Charset charset = Charset.forName("UTF-8");
			BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), charset));
			
			while ((inputLine = br.readLine()) != null) {
				sb.append(inputLine);
			}
			br.close();
			
			response = sb.toString();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		HashMap<String, String> ec2EntityIdMap = new HashMap<String, String>();
		
		JSONObject results = (JSONObject) new JSONParser().parse(response);
		JSONObject properties = (JSONObject) results.get("properties");
		String cloudType = (String) properties.get("cloudType");
		String detectedName = (String) properties.get("detectedName");
		
		if (cloudType != null) {
			if (cloudType.equals("EC2")) {
				ec2EntityIdMap.put(entity_id, detectedName);
			}
		}
		
		return ec2EntityIdMap;
	}
}
