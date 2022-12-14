package com.example.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.example.demo.dto.AwsKeyDto;
import com.example.demo.dto.DynaKeyDto;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.CloudWatchException;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.Metric;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.cloudwatch.model.MetricStat;

public class ConnectDB {
	Connection con;
	String query;
	Statement stmt;
	ResultSet rs;
	
	// DB연결
	public void openCon() throws Exception {
		String url = "jdbc:mariadb://127.0.0.1:3306/cloudresource?allowMultiQueries=true";
		String userid = "root";
		String pwd = "root";

		Class.forName("org.mariadb.jdbc.Driver");
		con = DriverManager.getConnection(url, userid, pwd);
		stmt = con.createStatement();
	}
	
	// DB 연결 끊기
	public void closeCon() throws Exception {
		con.close();
	}
	
	// call procedure
	public void callUpdateAwsconfigIdP() throws SQLException {
		query = "CALL update_awsconfig_id();";
		rs = stmt.executeQuery(query);
	}

	public void insertInstanceData(String accessKey, String instanceID, String imageID, String publicDns, String privateDns, String instanceType, String architecture, String platform, String region) throws Exception {
		query = "INSERT INTO awsconfig_id VALUES ((SELECT num FROM awsconfig_key WHERE accessKey ='" + accessKey + "'), '" + instanceID + "', '" + imageID + "', '" + publicDns + "', '" + privateDns + "', '" + instanceType + "', '" + architecture + "', '" + platform + "', '" + region + "', 0) \r\n" +
				"        ON DUPLICATE KEY UPDATE \r\n" + 
				"			instanceID = '" + instanceID + "', \r\n" + 
				"			imageID = '" + imageID + "', \r\n" +
				"			publicDns = '" + publicDns + "', \r\n" + 
				"			privateDns = '" + privateDns + "', \r\n" +
				"			instanceType = '" + instanceType + "', \r\n" +
				"			architecture = '" + architecture + "', \r\n" +
				"			platform = '" + platform + "', \r\n" +
				"			region = '" + region + "'";
				
		stmt.executeUpdate(query);
	}
	
	// privateDns name을 instanceID로 변환
	public String privateDns2InstanceID (String privateDns) throws SQLException {
		String instanceID = "";
		query = "SELECT instanceID FROM awsconfig_id WHERE privateDns='" + privateDns + "';";
		rs = stmt.executeQuery(query);
		
		while (rs.next()) {
			instanceID = rs.getString(1);
		}
		
		return instanceID;
	}
	
	// Dynatrace로 부터 이미 data를 받았는지 확인
	public int checkUpdateAwsResource (String privateDns, String times, String resource, String statistic) throws SQLException {
		int success = 0;
		query = "SELECT EXISTS \r\n" +
				"		(SELECT * FROM awsresource WHERE instanceID=(SELECT instanceID FROM awsconfig_id WHERE privateDns='" + privateDns + "') \r\n" +
				"		AND times='" + times + "' AND resource='" + resource + "' AND statistic='" + statistic + "' limit 1) \r\n" +
				"		as success;";
		rs = stmt.executeQuery(query);
		
		while (rs.next()) {
			success = rs.getInt(1);
		}
		
		return success;
	}
	
	// Dynatrace로 부터 이미 data를 받았는지 확인
	public int checkUpdateAwsResourceDisk (String privateDns, String times, String resource, String statistic) throws SQLException {
		int success = 0;
		query = "SELECT EXISTS \r\n" +
				"		(SELECT * FROM awsresource_disk WHERE instanceID=(SELECT instanceID FROM awsconfig_id WHERE privateDns='" + privateDns + "') \r\n" +
				"		AND times='" + times + "' AND resource='" + resource + "' AND statistic='" + statistic + "' limit 1) \r\n" +
				"		as success;";
		
		rs = stmt.executeQuery(query);
		
		while (rs.next()) {
			success = rs.getInt(1);
		}
		
		return success;
	}
	
	// DB insert to awsresource
	public void insertAwsData(String instanceID, String times, String resource, String statistic, Object val, String sources) throws Exception {
		query = "INSERT INTO awsresource VALUES ('" + instanceID + "', '" + times + "', '" + resource + "', '" + statistic + "', '" + val + "', '" + sources + "') \r\n" +
				"		ON DUPLICATE KEY UPDATE \r\n" +
				"			instanceID = '" + instanceID + "', \r\n" +
				"           times = '" + times + "', \r\n" +
				"			resource = '" + resource + "', \r\n" +
				"			statistic = '" + statistic + "', \r\n" +
				"			val = '" + val + "', \r\n" +
				"			sources = '" + sources + "'";
		
		stmt.executeUpdate(query);
	}
	
	// DB insert to awsresource_disk
	public void insertAwsDiskData(String instanceID, String diskID, String times, String resource, String statistic, Object val, String sources) throws Exception {
		query = "INSERT INTO awsresource_disk VALUES ('" + instanceID + "','" + diskID + "', '" + times + "', '" + resource + "', '" + statistic + "', '" + val + "', '" + sources + "') \r\n" +
				"		ON DUPLICATE KEY UPDATE \r\n" +
				"			instanceID = '" + instanceID + "', \r\n" +
				" 			diskID = '" + diskID + "', \r\n" +
				"           times = '" + times + "', \r\n" + 
				"			resource = '" + resource + "', \r\n" +
				"			statistic = '" + statistic + "', \r\n" +
				"			val = '" + val + "', \r\n" +
				"			sources = '" + sources + "'";
		
		stmt.executeUpdate(query);
	}
	
	public void insertEc2Data(String instanceType, double price, String ec2_vCPU, String ec2_memory, String operatingSys, String ec2_storage, String networkPerformance, String region) throws Exception {
		query = "INSERT INTO ec2_pricing_comparison(instanceType, price, ec2_vCPU,  ec2_memory, operatingSys, ec2_storage, networkPerformance, region)\r\n" + 
				"        VALUES('" + instanceType + "', '" + price + "', '" + ec2_vCPU + "', '" + ec2_memory + "', '" + operatingSys + "', '" + ec2_storage + "', '" + networkPerformance + "', '" + region + "')\r\n" + 
				"        ON DUPLICATE KEY UPDATE \r\n" + 
				"	        instanceType = '" + instanceType + "', \r\n" + 
				"	        price = '" + price + "', \r\n" + 
				"	        ec2_vCPU = '" + ec2_vCPU + "', \r\n" + 
				"	        ec2_memory = '" + ec2_memory + "', \r\n" + 
				"	        operatingSys = '" + operatingSys + "', \r\n" + 
				"	        ec2_storage = '" + ec2_storage + "', \r\n" + 
				"	        networkPerformance = '" + networkPerformance + "', \r\n" +
				"           region = '" + region + "'";
		stmt.executeUpdate(query);
	}
	
	public ArrayList<AwsKeyDto> selectAccessKey() throws Exception {
		ArrayList<AwsKeyDto> keyList = new ArrayList<>();
		query = "SELECT num, accessKey, secretKey, region FROM awsconfig_key";
		rs = stmt.executeQuery(query);
		
		while (rs.next()) {
			AwsKeyDto key = new AwsKeyDto();
			key.setNum(rs.getInt(1));
			key.setAccessKey(rs.getString(2));
			key.setSecretKey(rs.getString(3));
			key.setRegion(rs.getString(4));
			
			keyList.add(key);
		}
		
		return keyList;
	}
	
	// select Dynatrace config
	public ArrayList<DynaKeyDto> selectDynatraceConfig() throws Exception {
		ArrayList<DynaKeyDto> keyList = new ArrayList<>();
		query = "SELECT num, environment, token FROM dynatrace_key";
		rs = stmt.executeQuery(query);
		
		while (rs.next()) {
			DynaKeyDto key = new DynaKeyDto();
			key.setNum(rs.getInt(1));
			key.setEnvironment(rs.getString(2));
			key.setToken(rs.getString(3));
			
			keyList.add(key);
		}
		
		return keyList;
	}
	
	public ArrayList<String> selectInstanceID(String accessKey) throws Exception {
		ArrayList<String> instanceIDList = new ArrayList<>();
		query = "SELECT instanceID FROM awsconfig_id WHERE FK_key_num = (SELECT num FROM awsconfig_key WHERE accessKey ='" + accessKey + "')";
		rs = stmt.executeQuery(query);
		
		while (rs.next()) {
			instanceIDList.add(rs.getString(1));
		}
		
		return instanceIDList;
	}
	
	public int selectInstanceIsUpdate(String instanceID) throws Exception {
		int flag = 0;
		query = "SELECT isUpdate FROM awsconfig_id WHERE instanceID = '" + instanceID + "'";
		rs = stmt.executeQuery(query);
		
		while (rs.next()) {
			flag = rs.getInt(1);
		}
		
		return flag;
	}
	
	public ArrayList<String> selectRegionName() throws Exception {
		ArrayList<String> regionnameList = new ArrayList<>();
		query = "SELECT regionname FROM region_code2name;";
		rs = stmt.executeQuery(query);
		
		while (rs.next()) {
			regionnameList.add(rs.getString(1));
		}
		
		return regionnameList;
	}
	
	// instance detail data insert db
	public void insertInstanceDetail() throws Exception {
		ArrayList<AwsKeyDto> keyList = new ArrayList<>();
		keyList = selectAccessKey();
		String accessKey = null;
		String secretKey = null;
		String region = null;
		
		for (int i = 0; i < keyList.size(); i++) {
			accessKey = keyList.get(i).getAccessKey();
			secretKey = keyList.get(i).getSecretKey();
			region = keyList.get(i).getRegion();
			
			BasicAWSCredentials  awsBasicCredentials = new BasicAWSCredentials(accessKey, secretKey);
		    AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard()
		    		.withCredentials(new AWSStaticCredentialsProvider(awsBasicCredentials))
		    		.withRegion(region)
		    		.build();
			
	    	boolean done = false;
			
			DescribeInstancesRequest request = new DescribeInstancesRequest();
			
			try {
				while(!done) {
				    DescribeInstancesResult response = ec2.describeInstances(request);

				    for(Reservation reservation : response.getReservations()) {
				        for(Instance instance : reservation.getInstances()) {
				            insertInstanceData(accessKey, instance.getInstanceId(), instance.getImageId(), 
				            		instance.getPublicDnsName(), instance.getPrivateDnsName(), instance.getInstanceType(), instance.getArchitecture(), instance.getPlatformDetails(), region);
				        }
				    }

				    request.setNextToken(response.getNextToken());

				    if(response.getNextToken() == null) {
				        done = true;
				    }
				}
			} catch (AmazonEC2Exception e) {
	    		System.err.println(e.getErrorMessage());
	    		continue;
	    	}
		}
	}

	// AWS CloudWatch data insert to DB (raw data)
	public void insertAwsResource(Instant startTime, Instant endTime) throws Exception {
		ArrayList<AwsKeyDto> keyList = new ArrayList<>();
		keyList = selectAccessKey();
		String accessKey = null;
		String secretKey = null;
		String oneInstanceID = null;
		String id = null;
        String re = null;
        String st = null;
        
		for (int k = 0; k < keyList.size(); k++) {
			// Need to modify from 0 to k
			
			accessKey = keyList.get(k).getAccessKey();
			secretKey = keyList.get(k).getSecretKey();
			Region region = Region.of(keyList.get(k).getRegion());
			
			ArrayList<String> instanceIDList = selectInstanceID(accessKey);
			ArrayList<String> resourceList =  new ArrayList<>(Arrays.asList("CPUUtilization", "DiskReadBytes", "DiskWriteBytes", "NetworkIn", "NetworkOut"));
			ArrayList<String> statList = new ArrayList<>(Arrays.asList("Average", "Minimum", "Maximum"));
			
			
			AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);
			AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(awsBasicCredentials);
			CloudWatchClient cloudWatchClient  = CloudWatchClient.builder()
		    		.credentialsProvider(credentialsProvider)
		    		.region(region)
		    		.build();
			
		    int period = 3600;
	        boolean done = false;
	        String nToken = null;
	        
	        List<String> instanceIds = new ArrayList<>();
	        instanceIds.addAll(instanceIDList);
	        
	        List<String> metricNames = new ArrayList<>();
	        metricNames.addAll(resourceList);
	        
	        List<String> statistics = new ArrayList<>();
	        statistics.addAll(statList);
	        
	        ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");
	        
	        int partitionSize = 30; // GetMetrics limit 500.
	        List<List<String>> partitions = new ArrayList<>();
	        for (int i = 0; i < instanceIds.size(); i += partitionSize) {
	            partitions.add(instanceIds.subList(i, Math.min(i + partitionSize, instanceIds.size())));
	        }
	        
	        if (partitions.size() == 1) {	
	        	oneInstanceID = partitions.get(0).get(0);	
	        } else {
	        	oneInstanceID = null;
	        }
	        
	        for (int j = 0; j < partitions.size(); j ++) {
	        	try {
			    	while(!done) {
			            List<MetricDataQuery> metricDataQueryList = new ArrayList<>();
			            
			            int numbering = 0;
			            for(String instanceId : partitions.get(j)) {
			                Dimension dimension = Dimension.builder().name("InstanceId").value(instanceId).build();
			                for (String metricName : metricNames) {
			                	Metric metric = Metric.builder().namespace("AWS/EC2").dimensions(dimension).metricName(metricName).build();
			                	for (String stat : statistics) {
					                MetricStat metricStat = MetricStat.builder().metric(metric).period(period).stat(stat).build();
					                MetricDataQuery metricDataQuery = MetricDataQuery.builder().metricStat(metricStat).id("m"+(numbering++)).build();
					                metricDataQueryList.add(metricDataQuery);
			                	}
			                }
			            }
			            GetMetricDataRequest request = GetMetricDataRequest.builder().metricDataQueries(metricDataQueryList).startTime(startTime).endTime(endTime).build();
			            GetMetricDataResponse response = cloudWatchClient.getMetricData(request);
			            
			            List<Instant> timestamps = null;
			            List<Double> values = null;
			            
			            for(MetricDataResult result : response.metricDataResults()) {
			                timestamps = result.timestamps();
			                values = result.values();
			                
			                if (partitions.get(j).size() == 1) {
			                	id = oneInstanceID;
				                re = result.label().split(" ")[0];
				                st = result.label().split(" ")[1];
			                } else {
			                	id = result.label().split(" ")[0];
				                re = result.label().split(" ")[1];
				                st = result.label().split(" ")[2];
			                }
			                
//			                System.out.println(String.format("\n id : %s label : %s", result.id(), result.label()));
			                if (selectInstanceIsUpdate(id) == 0) {
			                	if (re.contains("Disk")) {
				                	for (int i=values.size()-1; i>=0; i--) {
//				                		System.out.println(String.format("timestamp : %s, value : %s", timestamps.get(i).atZone(ZONE_ID), values.get(i)));
				                		insertAwsDiskData(id, "DEFAULT", String.valueOf(timestamps.get(i).getEpochSecond()), re, st, values.get(i), "AWS");
				                	}
				                } else {
				                	for (int i=values.size()-1; i>=0; i--) {
				                		insertAwsData(id, String.valueOf(timestamps.get(i).getEpochSecond()), re, st, values.get(i), "AWS");
				                	}
				                }
			                }
			                
			                id = null;
			                re = null;
			                st = null;
			            }
			            
				        if(response.nextToken() == null) {
			                done = true;
			            } else {
			                nToken = response.nextToken();
			            }
			    	}
			    	
		    	} catch (CloudWatchException e) {
		    		System.err.println(e.awsErrorDetails().errorMessage());
		    		continue;
		    	}
	        }
	        cloudWatchClient.close();
	    }
	}

	// Data extraction with json api and DB insert
	public void insertEc2Price() throws Exception {
		ArrayList<String> regionnameList = new ArrayList<>();
		List<String> urlList = new ArrayList<>();
		regionnameList = selectRegionName();
		
		for (String regionname : regionnameList) {
			String windowPricingUrl = "https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/"
					+ regionname + "/Windows/index.json";
			String linuxPricingUrl = "https://b0.p.awsstatic.com/pricing/2.0/meteredUnitMaps/ec2/USD/current/ec2-ondemand-without-sec-sel/"
					+ regionname + "/Linux/index.json";
			urlList.add(windowPricingUrl);
			urlList.add(linuxPricingUrl);
		}
		
		String response = "";
		for (String targetUrl : urlList) {
			try {
				URL url = new URL(targetUrl);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
				conn.setRequestMethod("GET"); // 전송 방식
				conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
				conn.setConnectTimeout(10000); // 연결 타임아웃 설정(5초) 
				conn.setReadTimeout(10000); // 읽기 타임아웃 설정(5초)
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
				continue;
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
				
				String instanceType = (String) ec2.get("Instance Type");
				double price = Double.parseDouble((String) ec2.get("price"));
				String ec2_vCPU = (String) ec2.get("vCPU");
				String ec2_memory = (String) ec2.get("Memory");
				String operatingSystem = (String) ec2.get("Operating System");
				String ec2_storage = (String) ec2.get("Storage");
				String networkPerformance = (String) ec2.get("Network Performance");
				String region = (String) ec2.get("Location");
				
				insertEc2Data(instanceType, price, ec2_vCPU, ec2_memory, operatingSystem, ec2_storage, networkPerformance, region);
				
				System.out.printf("location: %s, instance type: %s, price: %s, vCPU: %s, memory: %s, operating system: %s, storage: %s, network performance: %s\n",
						region, instanceType, price, ec2_vCPU, ec2_memory, operatingSystem, ec2_storage, networkPerformance);
			}
		}
    }
	
	public void dataProcessing(String environment, String metricKey, String resolution,
			Instant startTime, Instant endTime, String entityId, String token, String detectedName) throws Exception {
		String response = "";
		String instanceId = detectedName;
		try {
			URL environment_url = new URL(environment +"/api/v2/metrics/query?metricSelector=" + metricKey
					+ "&resolution=" + resolution
					+ "&from=" + startTime
					+ "&to=" + endTime
					+ "&entitySelector=entityId(\"" + entityId + "\")"
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
		List<JSONObject> resultList = new ArrayList<>();
		
		JSONObject results = (JSONObject) new JSONParser().parse(response);
		
		Object resultObj = results.get("result");
		JSONArray resultArr = (JSONArray) resultObj;
		for (int r = 0; r < resultArr.size(); r++) {
			resultList.add((JSONObject) resultArr.get(r));
		}
		String metric = null;
		for (JSONObject result : resultList) { // metric마다
			List<JSONObject> dataList = new ArrayList<>();
			
			Object metricId = result.get("metricId");
			Object dataObj = result.get("data");
			JSONArray dataArr = (JSONArray) dataObj;
			for (int d = 0; d < dataArr.size(); d++) {
				dataList.add((JSONObject) dataArr.get(d));
			}
			for (JSONObject data : dataList) { // Host 마다
				JSONArray demensions = (JSONArray) data.get("dimensions");
				JSONArray timestamps = (JSONArray) data.get("timestamps");
				JSONArray values = (JSONArray) data.get("values");
				
				if (metricId.toString().contains("cpu.usage")) {
					metric = "CPUUtilization";
				} else if (metricId.toString().contains("disk.bytesRead")) {
					metric = "DiskReadBytes";
				} else if (metricId.toString().contains("disk.bytesWritten")) {
					metric = "DiskWriteBytes";
				} else if (metricId.toString().contains("disk.free")) {
					metric = "DiskFree";
				} else if (metricId.toString().contains("net.nic.bytesRx")) {
					metric = "NetworkIn";
				} else if (metricId.toString().contains("net.nic.bytesTx")) {
					metric = "NetworkOut";
				} else {
					metric = "MemoryUsed";
				}
				
				for (int i = 0; i < timestamps.size(); i++) {
					String statistic = metricId.toString().substring(metricId.toString().length() - 3);
					
					if (metric.contains("Disk")) {
						if (statistic.equals("min")) {
							if (values.get(i) != null) {
								int success = checkUpdateAwsResourceDisk(instanceId.toString(), String.valueOf(((long) timestamps.get(i) / 1000)), metric, "Minimum");
								if (success == 0) {
									String tmp = privateDns2InstanceID(instanceId.toString());
									if (!tmp.isEmpty()) {
										instanceId = tmp;
									}
									insertAwsDiskData(instanceId.toString(), demensions.get(1).toString(), String.valueOf(((long) timestamps.get(i) / 1000)), metric, "Minimum", 
											values.get(i), "Dynatrace");
								}
							}
						} else if (statistic.equals("max")) {
							if (values.get(i) != null) {
								int success = checkUpdateAwsResourceDisk(instanceId.toString(), String.valueOf(((long) timestamps.get(i) / 1000)), metric, "Maximum");
								if (success == 0) {
									String tmp = privateDns2InstanceID(instanceId.toString());
									if (!tmp.isEmpty()) {
										instanceId = tmp;
									}
									insertAwsDiskData(instanceId.toString(), demensions.get(1).toString(), String.valueOf(((long) timestamps.get(i) / 1000)), metric, "Maximum", 
											values.get(i), "Dynatrace");
								}
							}
						} else {
							if (values.get(i) != null) {
								int success = checkUpdateAwsResourceDisk(instanceId.toString(), String.valueOf(((long) timestamps.get(i) / 1000)), metric, "Average");
								if (success == 0) {
									String tmp = privateDns2InstanceID(instanceId.toString());
									if (!tmp.isEmpty()) {
										instanceId = tmp;
									}
									insertAwsDiskData(instanceId.toString(), demensions.get(1).toString(), String.valueOf(((long) timestamps.get(i) / 1000)), metric, "Average", 
											values.get(i), "Dynatrace");
								}
							}
						}
					} else {
						if (statistic.equals("min")) {
							if (values.get(i) != null) {
								int success = checkUpdateAwsResource(instanceId.toString(), String.valueOf(((long) timestamps.get(i) / 1000)), metric, "Minimum");
								if (success == 0) {
									String tmp = privateDns2InstanceID(instanceId.toString());
									if (!tmp.isEmpty()) {
										instanceId = tmp;
									}
									insertAwsData(instanceId.toString(), String.valueOf(((long) timestamps.get(i) / 1000)), metric, "Minimum", 
											values.get(i), "Dynatrace");
								}
							}
						} else if (statistic.equals("max")) {
							if (values.get(i) != null) {
								int success = checkUpdateAwsResource(instanceId.toString(), String.valueOf(((long) timestamps.get(i) / 1000)), metric, "Maximum");
								if (success == 0) {
									String tmp = privateDns2InstanceID(instanceId.toString());
									if (!tmp.isEmpty()) {
										instanceId = tmp;
									}
									insertAwsData(instanceId.toString(), String.valueOf(((long) timestamps.get(i) / 1000)), metric, "Maximum", 
											values.get(i), "Dynatrace");
								}
							}
						} else {
							if (values.get(i) != null) {
								int success = checkUpdateAwsResource(instanceId.toString(), String.valueOf(((long) timestamps.get(i) / 1000)), metric, "Average");
								if (success == 0) {
									String tmp = privateDns2InstanceID(instanceId.toString());
									if (!tmp.isEmpty()) {
										instanceId = tmp;
									}
									insertAwsData(instanceId.toString(), String.valueOf(((long) timestamps.get(i) / 1000)), metric, "Average", 
											values.get(i), "Dynatrace");
								}
							}
						}
					}
				}
			}
		}
	}
}
