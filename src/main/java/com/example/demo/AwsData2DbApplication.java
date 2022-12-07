package com.example.demo;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.demo.dto.DynaKeyDto;


@SpringBootApplication
public class AwsData2DbApplication {
    public static void main(String[] args) {
        // SchedulerFactory Create
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        
        // 첫 실행 시 data init
        // Dynatrace variable
        
//        GetEntities getentities = new GetEntities();
//		ArrayList<DynaKeyDto> keyList = new ArrayList<>();
//		List<String> entityList = new ArrayList<>();
//		HashMap<String, String> ec2EntityIdMap = new HashMap<String, String>();
//		List<String> metricKeyList = new ArrayList<>();		
//		String environment = null;
//		String token = null;
//		
//		String endTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00:00'Z'"));
//		String startTime = LocalDateTime.now().minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00:00'Z'"));
//		String resolution = "1h";
//		String entityType = "type(\"HOST\")";
//		metricKeyList.add("builtin:host.(cpu.usage,disk.bytesRead,disk.bytesWritten,net.nic.bytesRx,net.nic.bytesTx,mem.usage):(min,max,avg)");
//		metricKeyList.add("builtin:host.disk.free");
//		ConnectDB td = new ConnectDB();
//		
//		try {
//			td.openCon();
//			td.insertEc2Price();
//			td.insertInstanceDetail();
//			
//			// Dynatrace API
//			keyList = td.selectDynatraceConfig();
//			for(DynaKeyDto key : keyList) {
//				environment = key.getEnvironment();
//				token = key.getToken();
//				
//				entityList = getentities.getEntities(environment, entityType, startTime, endTime, token);
//				
//				for (String entityId : entityList) {
//					ec2EntityIdMap.putAll(getentities.GetEntitiesProperty(environment, entityId, startTime, endTime, token));
//				}
//				
//				for (int i = 0; i < metricKeyList.size(); i++) {
//					for (String entityId : ec2EntityIdMap.keySet()) {
//						td.dataProcessing(environment, metricKeyList.get(i), resolution,
//								Instant.parse(startTime), Instant.parse(endTime), entityId, token, ec2EntityIdMap.get(entityId));
//					}
//				}
//				td.callUpdateAwsconfigIdP();
//			}
//			
//			td.insertAwsResource(Instant.parse(startTime), Instant.parse(endTime));
//			td.closeCon();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		// 매일 자정 scheduler 동작
        try {
            Scheduler scheduler = schedulerFactory.getScheduler();

            JobDetail job = newJob(DbScheduler.class).withIdentity("DBInsertJob", Scheduler.DEFAULT_GROUP).build();

            Trigger trigger = newTrigger().withIdentity("DBInsertTrigger", Scheduler.DEFAULT_GROUP)
                    .withSchedule(cronSchedule("0 0 0 * * ?")).build(); // every day 00:00:00 -> (0 0 0* * ?)

            scheduler.scheduleJob(job, trigger);
            scheduler.start();
        } catch (Exception e) {
        	e.printStackTrace();      
    	}
    }

}
