package uProxy.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import uProxy.dataStructure.InfoPacket;
import uProxy.main.UProxyApplication;

import org.springframework.http.server.reactive.ServerHttpRequest;

@Controller
public class HTTPController {

	HashMap<Long, InfoPacket> packetBuffer;

	public HTTPController() {
		this.packetBuffer = new HashMap<Long, InfoPacket>();
	}

	@RequestMapping(value = "**")
	@ResponseBody
	ResponseEntity requestController(@RequestBody(required = false) String body,
			@RequestHeader Map<String, String> _headers, HttpMethod method, ServerHttpRequest request,
			@RequestParam(required = false) String queryParam) throws URISyntaxException {
		try {

			String host = request.getRemoteAddress().toString();

			System.out.println();
			System.out.println("[INFO] [HTTPController] Request received from: " + host);

			URI newUri = new URI("http", null, UProxyApplication.server, UProxyApplication.port, null, null, null);

			String uri = request.getURI().toString();
			String path = request.getPath().toString();
			String queryParams = "";

			System.out.println("[INFO] [HTTPController] Method: " + method);
			System.out.println("[INFO] [HTTPController] URI: " + uri);
			System.out.println("[INFO] [HTTPController] Path: " + path);

			if (uri.contains("?")) {
				String[] parts = uri.split("\\?");

				uri = parts[0];
				queryParams = parts[1];
				System.out.println("[INFO] [HTTPController] Part 1 (URI): " + parts[0]);
				System.out.println("[INFO] [HTTPController] Part 2 (Query): " + parts[1]);
			}

			newUri = UriComponentsBuilder.fromUri(newUri).path(path).query(queryParams).build(true).toUri();

			System.out.println("[INFO] [HTTPController] Sending: " + newUri.toString());

			HttpHeaders headers = new HttpHeaders();
			_headers.forEach((key, value) -> {
				System.out.println("[INFO] [HTTPController] " + String.format("Header '%s' = %s", key, value));
				headers.set(key, value);
			});
			
			
			HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
			RestTemplate restTemplate = new RestTemplate();

			ResponseEntity resp = null;
			long respTime = System.currentTimeMillis();

			try {
				
				resp = restTemplate.exchange(newUri, method, httpEntity, String.class);

				System.out.println("[INFO] [HTTPController] Response received: " + resp);

			} catch (HttpStatusCodeException e) {

				resp = ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders())
						.body(e.getResponseBodyAsString());

				System.out.println("[INFO] [HTTPController] Response received on exception: " + resp);

			} catch (Exception ex) {
				System.out.println("[ERROR] [HTTPController] Error sending request to proxied service, exception: " +  ex.getClass());
			}
			
			respTime = System.currentTimeMillis() - respTime;
			
			if (resp != null) {
			
				HttpHeaders respHeaders = resp.getHeaders();
				
				System.out.println("[INFO] [HTTPController] Response headers:");
				_headers.forEach((key, value) -> {
					System.out.println("[INFO] [HTTPController] " + String.format("Header '%s' = %s", key, value));
					headers.set(key, value);
				});
				
				packetBuffer.put(Thread.currentThread().getId(), new InfoPacket(path, method.toString(),
						request.getRemoteAddress().toString(), UProxyApplication.proxedIP, resp.getStatusCodeValue(), (long) respTime));
			} else {
				
				packetBuffer.put(Thread.currentThread().getId(),
						new InfoPacket(path, method.toString(), request.getRemoteAddress().toString(), UProxyApplication.proxedIP, 0, (long) 0.0));
			}

			return resp;

		} finally {
			try {
				URI collectorURI = new URI("http", null, UProxyApplication.collectorServer,
						UProxyApplication.collectorPort, null, null, null);
				collectorURI = UriComponentsBuilder.fromUri(collectorURI).path("/proxyInfo")
						.query("proxyID=" + UProxyApplication.proxyID).build(true).toUri();

				System.out.println("[INFO] [HTTPController] Sending report to: " + collectorURI.toString());

				HttpHeaders headers = new HttpHeaders();
				headers.set("Content-Type", "application/json");

				InfoPacket pack = packetBuffer.get(Thread.currentThread().getId());
				String bodyProxy = pack.getJson();

				System.out.println("[INFO] [HTTPController] Body: " + bodyProxy);

				HttpEntity<String> httpEntity = new HttpEntity<>(bodyProxy, headers);
				RestTemplate restTemplate = new RestTemplate();

				ResponseEntity collectorACK = restTemplate.exchange(collectorURI, HttpMethod.POST, httpEntity,
						String.class);

				System.out.println("[INFO] [HTTPController] Response from collector: " + collectorACK.getBody());
			} catch (Exception e) {
				System.out.println("[ERROR] [HTTPController] Error sending report to collector, exception: " + e.getClass());
			}
		}

	}
	
	@RequestMapping(value = "/setIP", method = RequestMethod.GET)
	@ResponseBody
	String setIP(@RequestParam String proxedIP) {
		System.out.println("[INFO] [HTTPController] Received \"setIP\" request on IP: " + proxedIP);
		
		UProxyApplication.proxedIP = proxedIP;
		
		return "[INFO] IP set.";
	}

}
