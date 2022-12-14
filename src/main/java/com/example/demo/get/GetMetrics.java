package com.example.demo.get;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

public class GetMetrics {

	public static void main(String[] args) {
		Region region = Region.of("");
		AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create("", "");
	    AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(awsBasicCredentials);
	    CloudWatchClient cloudWatchClient  = CloudWatchClient.builder()
	    		.credentialsProvider(credentialsProvider)
	    		.region(region)
	    		.build();
	
	     String identifier = "InstanceId";
	     List<String> instanceIds = new ArrayList<>();
	     instanceIds.add("");
	     int period = 3600;
	     String namespace = "AWS/EC2";
	     List<String> metricNames = new ArrayList<>(Arrays.asList("CPUUtilization", "DiskReadBytes", "DiskWriteBytes"
	     		, "NetworkIn", "NetworkOut"));
	     List<String> statistics = new ArrayList<>(Arrays.asList("Average", "Minimum", "Maximum"));
	     
	     Instant startTime = Instant.parse("2022-11-23T10:00:00Z");
	     Instant endTime = Instant.parse("2022-11-24T10:00:00Z");
	     
	     boolean done = false;
	     String nToken = null;
	     
		    try {
		    	while(!done) {
		            List<MetricDataQuery> metricDataQueryList = new ArrayList<>();
		            
		            int numbering = 0;
		            for(String instanceId : instanceIds) {
		                Dimension dimension = Dimension.builder().name(identifier).value(instanceId).build();
		                for (String metricName : metricNames) {
		                	Metric metric = Metric.builder().namespace(namespace).dimensions(dimension).metricName(metricName).build();
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
		                
		                System.out.println(String.format("\n id : %s label : %s", result.id(), result.label()));
		                
		                for (int i=values.size()-1; i>=0; i--) {
		                	System.out.println(String.format("timestamp : %s, value : %s", 
		                    		timestamps.get(i).getEpochSecond(), values.get(i)));
		                }
		            }
		            
			        if(response.nextToken() == null) {
		                done = true;
		            } else {
		                nToken = response.nextToken();
		            }
		    	}
		    	
	 	} catch (CloudWatchException e) {
	 		System.err.println(e.awsErrorDetails().errorMessage());
	 		System.exit(1);
	 	}
		    cloudWatchClient.close();
		}
}
