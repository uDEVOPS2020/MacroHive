package com.microservice.uTest.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.microservice.uTest.notifier.Notifier;

@SpringBootApplication
@ComponentScan(basePackages = {"com.microservice.uTest"})
public class UTestApplication {
	
	public static Notifier notifier;
	
	public static String collectorServer = "host.docker.internal";
	public static int collectorPort =  11112;
	
	public UTestApplication() {
		notifier = new Notifier();
	}
	
	public static void main(String[] args) {
		SpringApplication.run(UTestApplication.class, args);
	}
	
}
