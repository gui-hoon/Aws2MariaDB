package com.example.demo.get;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GetEc2PriceManyRegion {
	public static void main(String[] args) throws ParseException {
		List<String> urlList = new ArrayList<>();
//		urlList.add("https://api.allorigins.win/get?url=https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/Asia%20Pacific%20(Seoul)/Windows/index.json?timestamp=1639230933739");
		urlList.add("https://api.allorigins.win/get?url=https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/Asia%20Pacific%20(Seoul)/Linux/index.json?timestamp=1639230933739");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/US%20East%20%28Ohio%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/US%20East%20%28N.%20Virginia%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/US%20West%20%28N.%20California%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/US%20West%20%28Oregon%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/Africa%20%28Cape%20Town%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/Asia%20Pacific%20%28Hong%20Kong%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/Asia%20Pacific%20%28Jakarta%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/Asia%20Pacific%20%28Mumbai%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/Asia%20Pacific%20%28Osaka%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/Asia%20Pacific%20%28Singapore%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/Asia%20Pacific%20%28Sydney%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/Asia%20Pacific%20%28Tokyo%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/Canada%20%28Central%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/Europe%20%28Frankfurt%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/Europe%20%28Ireland%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/Europe%20%28London%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/Europe%20%28Milan%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/Europe%20%28Paris%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/Europe%20%28Stockholm%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/Middle%20East%20%28Bahrain%29/Linux/index.json");
//		urlList.add("https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/Middle%20East%20%28UAE%29/Linux/index.json");
		
		String response = "";
		for (String targetUrl : urlList) {
			try {
				URL url = new URL(targetUrl);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
				conn.setRequestMethod("GET"); // 전송 방식
				conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
				conn.setConnectTimeout(5000); // 연결 타임아웃 설정(5초) 
				conn.setReadTimeout(5000); // 읽기 타임아웃 설정(5초)
				conn.setDoOutput(true);
				
		        System.out.println("getContentType():" + conn.getContentType()); // 응답 콘텐츠 유형 구하기
		        System.out.println("getResponseCode():"    + conn.getResponseCode()); // 응답 코드 구하기
		        System.out.println("getResponseMessage():" + conn.getResponseMessage()); // 응답 메시지 구하기
	
				String inputLine;			
				StringBuffer sb = new StringBuffer();
				
				Charset charset = Charset.forName("UTF-8");
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
				
				while ((inputLine = br.readLine()) != null) {
					sb.append(inputLine);
				}
				br.close();
				
				response = sb.toString();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String contents =null;
			JSONObject content = new JSONObject();
			JSONObject regions = new JSONObject();
			
			JSONObject result = (JSONObject) new JSONParser().parse(response);
			
			if ((String) result.get("contents") != null) {
				contents = (String) result.get("contents");
				content = (JSONObject) new JSONParser().parse(contents);
				regions = (JSONObject) content.get("regions");
			} else {
				regions = (JSONObject) result.get("regions");
			}

			Iterator iterator =  regions.keySet().iterator();
			JSONObject ec2List = new JSONObject();
			while( iterator.hasNext() ) {
				String key = (String)iterator.next();
				ec2List = (JSONObject) regions.get(key);
			}
			Iterator iter =  ec2List.keySet().iterator();
	
			while( iter.hasNext() )
			{
				String key = (String)iter.next();
				JSONObject ec2 = (JSONObject) ec2List.get(key);
				
				String region = (String) ec2.get("Location");
				String instanceType = (String) ec2.get("Instance Type");
				double price = Double.parseDouble((String) ec2.get("price"));
				String vCPU = (String) ec2.get("vCPU");
				String ec2_memory = (String) ec2.get("Memory");
				String operatingSystem = (String) ec2.get("Operating System");
				String ec2_storage = (String) ec2.get("Storage");
				String networkPerformance = (String) ec2.get("Network Performance");
				
				System.out.printf("location: %s, instance type: %s, price: %s, vCPU: %s, memory: %s, operating system: %s, storage: %s, network performance: %s\n",
						region, instanceType, price, vCPU, ec2_memory, operatingSystem, ec2_storage, networkPerformance);
			}
		}
	}
}

