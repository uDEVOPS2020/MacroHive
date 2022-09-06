package com.microservice.uTest.notifier;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microservice.uTest.main.UTestApplication;

public class Notifier {
	
	public void notifyStartTest(String testID) {
		
		try {
			URI collectorURI = new URI("http", null, UTestApplication.collectorServer,
					UTestApplication.collectorPort, null, null, null);
			collectorURI = UriComponentsBuilder.fromUri(collectorURI).path("/recordStart")
					.query("testID=" + testID).build(true).toUri();

			HttpEntity<String> httpEntity = new HttpEntity<>("");
			RestTemplate restTemplate = new RestTemplate();

			ResponseEntity collectorACK = restTemplate.exchange(collectorURI, HttpMethod.GET, httpEntity,
					String.class);

		} catch (Exception e) {
			System.out.println("[ERROR] [Notifier] Error sending notification to collector, exception: " + e.getClass());
		}
		
	}
	
	public void notifyEndTest(Integer JsonID, Integer pathMethodID, String testID, String method, String url, Integer failureSev, Integer code) {
		
		try {
			URI collectorURI = new URI("http", null, UTestApplication.collectorServer,
					UTestApplication.collectorPort, null, null, null);
			collectorURI = UriComponentsBuilder.fromUri(collectorURI).path("/recordStop")
					.query("testID=" + testID).build(true).toUri();

			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");
			
			ArrayList<String> values = new ArrayList<String>();
			
			values.add(JsonID.toString());
			values.add(pathMethodID.toString());
			values.add(url);
			values.add(method);
			values.add(code.toString());
			values.add(failureSev.toString());
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String body = gson.toJson(values);
			
			HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
			RestTemplate restTemplate = new RestTemplate();

			ResponseEntity collectorACK = restTemplate.exchange(collectorURI, HttpMethod.POST, httpEntity,
					String.class);

		} catch (Exception e) {
			System.out.println("[ERROR] [Notifier] Error sending notification to collector, exception: " + e.getClass());
		}
		
	}
	
	
}
