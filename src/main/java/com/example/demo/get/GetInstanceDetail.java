package com.example.demo.get;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;

public class GetInstanceDetail {

	public static void main(String[] args) {
		BasicAWSCredentials  awsBasicCredentials = new BasicAWSCredentials("", "");
	    AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard()
	    		.withCredentials(new AWSStaticCredentialsProvider(awsBasicCredentials))
	    		.withRegion("")
	    		.build();
	    
	    DescribeAvailabilityZonesResult zones_response = ec2.describeAvailabilityZones();
		String region = "";
		
		for(AvailabilityZone zone : zones_response.getAvailabilityZones()) {
			region = zone.getRegionName();
		}
		
    	boolean done = false;
		
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		
		while(!done) {
		    DescribeInstancesResult response = ec2.describeInstances(request);

		    for(Reservation reservation : response.getReservations()) {
		        for(Instance instance : reservation.getInstances()) {
		            System.out.printf("%s, %s, %s, %s, %s, %s, %s, %s \n",
		            		instance.getInstanceId(), 
		            		instance.getImageId(), 
		            		instance.getPublicDnsName(), 
		            		instance.getPrivateDnsName(), 
		            		instance.getInstanceType(), 
		            		instance.getArchitecture(), 
		            		instance.getPlatformDetails(), 
		            		region);
		        }
		    }

		    request.setNextToken(response.getNextToken());

		    if(response.getNextToken() == null) {
		        done = true;
		    }
		}

	}

}

