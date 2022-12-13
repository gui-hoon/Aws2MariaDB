package com.example.demo;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.example.demo.ConnectDB;
import com.example.demo.dto.DynaKeyDto;

@DisallowConcurrentExecution
public class DbScheduler implements Job{
//	private static final Logger logger = Logger.getLogger(DbScheduler.class);	
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
//		String log4jConfPath = "src/main/java/log4j.properties";
//    	PropertyConfigurator.configure(log4jConfPath);
//    	logger.info("DB Insert Start ...");
    	
        // Dynatrace variable
        GetEntities getentities = new GetEntities();
		ArrayList<DynaKeyDto> keyList = new ArrayList<>();
		List<String> entityList = new ArrayList<>();
		HashMap<String, String> ec2EntityIdMap = new HashMap<String, String>();
		List<String> metricKeyList = new ArrayList<>();
		String environment = null;
		String token = null;
		
		String endTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00:00'Z'"));
		String startTime = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00:00'Z'"));
		String resolution = "1h";
		String entityType = "type(\"HOST\")";
		metricKeyList.add("builtin:host.(cpu.usage,disk.bytesRead,disk.bytesWritten,net.nic.bytesRx,net.nic.bytesTx,mem.usage):(min,max,avg)");
		metricKeyList.add("builtin:host.disk.free");
		// 							Unit -> Percent, BytePerSecond, BytePerSecond, Byte, BytePerSecond, BytePerSecond, Percent
		
		// 첫 실행 시 data init
		ConnectDB td = new ConnectDB();
		try {
			td.openCon();
			td.insertInstanceDetail();
			
			// Dynatrace API
			keyList = td.selectDynatraceConfig();
			for(DynaKeyDto key : keyList) {
				environment = key.getEnvironment();
				token = key.getToken();
				
				entityList = getentities.getEntities(environment, entityType, startTime, endTime, token);
				
				for (String entityId : entityList) {
					ec2EntityIdMap.putAll(getentities.GetEntitiesProperty(environment, entityId, startTime, endTime, token));
				}
				
				for (int i = 0; i < metricKeyList.size(); i++) {
					for (String entityId : ec2EntityIdMap.keySet()) {
						td.dataProcessing(environment, metricKeyList.get(i), resolution,
								Instant.parse(startTime), Instant.parse(endTime), entityId, token, ec2EntityIdMap.get(entityId));
					}
				}
				td.callUpdateAwsconfigIdP();
			}
			
			td.insertAwsResource(Instant.parse(startTime), Instant.parse(endTime));
			td.closeCon();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}

