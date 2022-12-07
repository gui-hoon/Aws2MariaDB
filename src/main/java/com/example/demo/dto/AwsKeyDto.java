package com.example.demo.dto;

import lombok.Data;

@Data
public class AwsKeyDto {
	int num;
	String accessKey;
	String secretKey;
	String region;
}
