package com.microservice.uTest.dataStructure;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import java.util.concurrent.ThreadLocalRandom;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TestFrame {

	private static boolean DEBUG_MODE = true;

	private String service;

	private String name;
	private String tfID;
	private int jsonID;
	private int pathID;
	private int pathMethodID;
	private double failureProb;
	private double occurrenceProb;

	public ArrayList<InputClass> ic = new ArrayList<InputClass>();
	public ArrayList<InputClass> icHeader = new ArrayList<InputClass>();

	public ArrayList<ArrayList<InputClass>> ref = new ArrayList<ArrayList<InputClass>>();

	public ArrayList<Integer> expectedResponses = new ArrayList<Integer>();

	private String payload;

	private String reqType;
	private String url;
	private String selUrl;
	private String selPayload;
	private int responseCode;
	private long responseTime;
	private StringBuffer response;

	private int time = 1000;

	private String finalToken;

	private String state;
	private float priority;
	private int failureSeverity;

	public TestFrame(boolean debug) {
		super();
		finalToken = "";
		state = "notdefined";
		priority = 0;
	}

	public TestFrame(String _name, String _tfID, String _reqType, String _payload, boolean debug) {
		super();
		this.name = _name;
		this.tfID = _tfID;
		this.reqType = _reqType;
		this.payload = _payload;
		finalToken = "";
		DEBUG_MODE = debug;
		state = "notdefined";
		priority = 0;
		failureSeverity = 0;
	}

	public TestFrame(int _jsonID, String _name, String _tfID, String _reqType, String _payload, boolean debug) {
		super();
		this.name = _name;
		this.tfID = _tfID;
		this.reqType = _reqType;
		this.payload = _payload;
		finalToken = "";
		DEBUG_MODE = debug;
		state = "notdefined";
		priority = 0;
		failureSeverity = 0;
		this.jsonID = _jsonID;
	}

	public TestFrame(int _jsonID, int _pathID, String _name, String _tfID, String _reqType, String _payload,
			boolean debug) {
		super();
		this.name = _name;
		this.tfID = _tfID;
		this.reqType = _reqType;
		this.payload = _payload;
		finalToken = "";
		DEBUG_MODE = debug;
		state = "notdefined";
		priority = 0;
		failureSeverity = 0;
		this.jsonID = _jsonID;
		this.pathID = _pathID;
	}

	public TestFrame(int _jsonID, int _pathID, int _pathMethodID, String _name, String _tfID, String _reqType, String _payload,
			boolean debug) {
		super();
		this.name = _name;
		this.tfID = _tfID;
		this.reqType = _reqType;
		this.payload = _payload;
		finalToken = "";
		DEBUG_MODE = debug;
		state = "notdefined";
		priority = 0;
		failureSeverity = 0;
		this.jsonID = _jsonID;
		this.pathID = _pathID;
		this.pathMethodID = _pathMethodID;
	}

	// HTTP GET request
	private void sendGet() throws Exception {
		URL obj = new URL(selUrl);
		long start = System.currentTimeMillis();
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		if (DEBUG_MODE)
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] GET");

		try {
			con.setRequestMethod("GET");
			
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Authorization", "Bearer " + finalToken);

			if(icHeader.size() != 0) {
				for(int i = 0; i < icHeader.size(); i++) {
					if(icHeader.get(i).defaultValue != null && !icHeader.get(i).defaultValue.equals("null"))
						con.setRequestProperty(icHeader.get(i).name, icHeader.get(i).defaultValue);
				}
			}
			
			con.setConnectTimeout(time);
			con.setReadTimeout((time));
			
			responseCode = con.getResponseCode();
			responseTime = System.currentTimeMillis() - start;

			if (responseCode < 300) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			}

		} finally {
			if (con != null) {
				con.disconnect();
			}
		}

	}

	// HTTP POST request
	private void sendPost() throws Exception {

		URL obj = new URL(selUrl);
		long start = System.currentTimeMillis();
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		if (DEBUG_MODE)
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] POST");

		try {
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization", "Bearer " + finalToken);
			con.setRequestMethod("POST");
			
			if(icHeader.size() != 0) {
				for(int i = 0; i < icHeader.size(); i++) {
					if(icHeader.get(i).defaultValue != null && !icHeader.get(i).defaultValue.equals("null"))
						con.setRequestProperty(icHeader.get(i).name, icHeader.get(i).defaultValue);
				}
			}

			con.setConnectTimeout(time);
			con.setReadTimeout(time);

			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
			wr.write(this.selPayload);
			wr.flush();

			responseCode = con.getResponseCode();
			responseTime = System.currentTimeMillis() - start;

			if (responseCode < 300) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

			}
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
	}

	// HTTP PUT request
	private void sendPut() throws Exception {
		responseCode = 0;
		URL obj = new URL(selUrl);
		long start = System.currentTimeMillis();
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		if (DEBUG_MODE)
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] PUT");

		try {
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization", "Bearer " + finalToken);
			con.setRequestMethod("PUT");
			
			if(icHeader.size() != 0) {
				for(int i = 0; i < icHeader.size(); i++) {
					if(icHeader.get(i).defaultValue != null && !icHeader.get(i).defaultValue.equals("null"))
						con.setRequestProperty(icHeader.get(i).name, icHeader.get(i).defaultValue);
				}
			}

			con.setConnectTimeout(time);
			con.setReadTimeout(time);

			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
			wr.write(this.selPayload);
			wr.flush();

			responseCode = con.getResponseCode();
			responseTime = System.currentTimeMillis() - start;

			if (responseCode < 300) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			}
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
	}

	// HTTP DELETE request
	private void sendDelete() throws Exception {
		URL obj = new URL(selUrl);
		long start = System.currentTimeMillis();
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		if (DEBUG_MODE)
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] DELETE");

		try {
			con.setRequestMethod("DELETE");

			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Authorization", "Bearer " + finalToken);
			
			if(icHeader.size() != 0) {
				for(int i = 0; i < icHeader.size(); i++) {
					if(icHeader.get(i).defaultValue != null && !icHeader.get(i).defaultValue.equals("null"))
						con.setRequestProperty(icHeader.get(i).name, icHeader.get(i).defaultValue);
				}
			}

			con.setConnectTimeout(time);
			con.setReadTimeout((time));

			responseCode = con.getResponseCode();
			responseTime = System.currentTimeMillis() - start;

			if (responseCode < 300) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			}

		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
	}

	private void sendHead() throws Exception {
		URL obj = new URL(selUrl);
		long start = System.currentTimeMillis();
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		if (DEBUG_MODE)
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] HEAD");

		try {
			con.setRequestMethod("HEAD");

			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Authorization", "Bearer " + finalToken);
			
			if(icHeader.size() != 0) {
				for(int i = 0; i < icHeader.size(); i++) {
					if(icHeader.get(i).defaultValue != null && !icHeader.get(i).defaultValue.equals("null"))
						con.setRequestProperty(icHeader.get(i).name, icHeader.get(i).defaultValue);
				}
			}

			con.setConnectTimeout(time);
			con.setReadTimeout((time));

			responseCode = con.getResponseCode();
			responseTime = System.currentTimeMillis() - start;

			if (responseCode < 300) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			}

		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
	}
	
	private void sendOptions() throws Exception {
		URL obj = new URL(selUrl);
		long start = System.currentTimeMillis();
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		if (DEBUG_MODE)
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] OPTIONS");

		try {
			con.setRequestMethod("OPTIONS");

			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Authorization", "Bearer " + finalToken);
			
			if(icHeader.size() != 0) {
				for(int i = 0; i < icHeader.size(); i++) {
					if(icHeader.get(i).defaultValue != null && !icHeader.get(i).defaultValue.equals("null"))
						con.setRequestProperty(icHeader.get(i).name, icHeader.get(i).defaultValue);
				}
			}

			con.setConnectTimeout(time);
			con.setReadTimeout((time));

			responseCode = con.getResponseCode();
			responseTime = System.currentTimeMillis() - start;

			if (responseCode < 300) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			}

		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
	}
	
	private void sendPatch() throws Exception {
		URL obj = new URL(selUrl);
		long start = System.currentTimeMillis();
		CloseableHttpClient http = HttpClientBuilder.create().build();
        
       
		
		if (DEBUG_MODE)
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] PATCH");

		try {

			HttpPatch updateRequest = new HttpPatch(selUrl);
			
			updateRequest.setEntity(new StringEntity("inputjsonString", ContentType.APPLICATION_JSON));
	        updateRequest.setHeader("Authorization", "Bearer " + finalToken);
	        
	        HttpResponse responsehttp = http.execute(updateRequest);
	        responseCode = responsehttp.getStatusLine().getStatusCode();

			responseTime = System.currentTimeMillis() - start;

			if (responseCode < 300) {
				
				HttpEntity entity = responsehttp.getEntity();
				String responseString = EntityUtils.toString(entity, "UTF-8");
				
				response.append(responseString);
			}

		} 
	}
	

	/**************************************/

	public boolean extractAndExecuteTestCase() {

		url = this.name;
		selUrl = this.url;
		selPayload = this.payload;

		if (url.contains("{") || selPayload.contains("{")) {
			this.selectTC();
		}

		if (DEBUG_MODE) {
			System.out.println("\n[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] ----------------------------------------");
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] url: " + selUrl);
			if (selPayload != "null")
				System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] payload: " + selPayload);
		}

		try {

			if (this.reqType.equals("GET") || this.reqType.equals("get")) {
				this.sendGet();
			} else if (this.reqType.equals("POST") || this.reqType.equals("post")) {
				this.sendPost();
			} else if (this.reqType.equals("DELETE") || this.reqType.equals("delete")) {
				this.sendDelete();
			} else if (this.reqType.equals("PUT") || this.reqType.equals("put")) {
				this.sendPut();
			} else if (this.reqType.equals("HEAD") || this.reqType.equals("head")) {
				this.sendHead();
			} else if (this.reqType.equals("PATCH") || this.reqType.equals("patch")) {
				this.sendPatch();
			} else if (this.reqType.equals("OPTIONS") || this.reqType.equals("options")) {
				this.sendOptions();
			} else {
				responseCode = -2;
				state = "failed";
				return false;
			}

		} catch (java.net.SocketTimeoutException e) {
			responseCode = -1;
			state = "failed";
			failureSeverity = 0;

			if (DEBUG_MODE) {
				System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] Exception response (timeout): "
						+ responseCode);
				System.out.println(
						"[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] Response Time: " + responseTime + " ms");
				System.out.println(e);
			}

			return false;

		} catch (Exception e) {
			if (DEBUG_MODE)
				System.out.println(e);

			state = "failed";
			boolean returnValue = false;

			for (int i = 0; i < expectedResponses.size(); i++) {
				if (responseCode == expectedResponses.get(i)) {
					returnValue = true;
					state = "success";
				}
			}

			if (state.equals("failed") && responseCode >= 500) {
				failureSeverity = 2;
			} else if (state.equals("failed")) {
				failureSeverity = 1;
			}

			if (DEBUG_MODE) {
				if (returnValue) {
					System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
							+ "] [TestFrame] ExceptionHandler expected response: " + responseCode);
					System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] Response Time: "
							+ responseTime + " ms");

				} else {
					System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
							+ "] [TestFrame] ExceptionHandler not expected response: " + responseCode);
					System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] Response Time: "
							+ responseTime + " ms");
				}
			}

			return returnValue;
		}

		// noException
		boolean returnValue = false;
		state = "failed";

		for (int i = 0; i < expectedResponses.size(); i++) {
			if (responseCode == expectedResponses.get(i)) {
				returnValue = true;
				state = "success";
			}
		}

		if (state.equals("failed") && responseCode >= 500) {
			failureSeverity = 2;
		} else if (state.equals("failed")) {
			failureSeverity = 1;
		}

		if (DEBUG_MODE) {
			if (returnValue) {
				System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] Received expected response: "
						+ responseCode);
				System.out.println(
						"[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] Response Time: " + responseTime + " ms");
			} else {
				System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] Received not expected response: "
						+ responseCode);
				System.out.println(
						"[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] Response Time: " + responseTime + " ms");
			}
		}

		return returnValue;
	}

	private void selectTC() {
		String sel;
		String type;

		selPayload = payload;
		selUrl = url;

		for (int i = 0; i < this.ic.size(); i++) {
			
			type = this.ic.get(i).type;
			
			if (ic.get(i).valid && ic.get(i).defaultValue != null && !ic.get(i).defaultValue.equals("null")) {
				
				sel = ic.get(i).defaultValue;
				
			} else {

				switch (type) {
				case "float":
					sel = String.valueOf(Math.abs(ThreadLocalRandom.current().nextFloat()));
					break;

				case "double":
					sel = String.valueOf(Math.abs(ThreadLocalRandom.current().nextDouble()));
					break;

				case "int32":
					sel = String.valueOf(Math.abs(ThreadLocalRandom.current().nextInt()));
					break;

				case "int64":

					sel = String.valueOf(Math.abs(ThreadLocalRandom.current().nextLong()));
					break;

				case "date":
					sel = String.valueOf(LocalDate.now());
					break;

				case "date-time":
					TimeZone tz = TimeZone.getTimeZone("UTC");
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); // DateFormat df = new
																					// SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
					df.setTimeZone(tz);
					sel = df.format(new Date());
					break;

				case "range":
					sel = String.valueOf(ThreadLocalRandom.current().nextInt(Integer.parseInt(this.ic.get(i).min),
							Integer.parseInt(this.ic.get(i).max)));
					break;

				case "lower":
					sel = String.valueOf(ThreadLocalRandom.current().nextInt(Integer.parseInt(this.ic.get(i).min) - 500,
							Integer.parseInt(this.ic.get(i).min) - 1));
					break;

				case "greater":
					sel = String.valueOf(ThreadLocalRandom.current().nextInt(Integer.parseInt(this.ic.get(i).min) + 1,
							Integer.parseInt(this.ic.get(i).min) + 500));
					break;

				case "different":
					sel = randomNotInt();
					break;

				case "symbol":
					sel = this.ic.get(i).defaultValue;
					break;

				case "empty":
					sel = "";
					break;

				case "s_range":

					sel = RandomStringUtils.randomAlphanumeric(ThreadLocalRandom.current()
							.nextInt(Integer.parseInt(this.ic.get(i).min), Integer.parseInt(this.ic.get(i).max)));
					break;

				case "s_greater":
					sel = RandomStringUtils.randomAlphanumeric(Integer.parseInt(this.ic.get(i).min));
					break;

				case "n_range":
					sel = RandomStringUtils.randomNumeric(ThreadLocalRandom.current()
							.nextInt(Integer.parseInt(this.ic.get(i).min), Integer.parseInt(this.ic.get(i).max)));
					break;

				case "n_greater":
					sel = RandomStringUtils.randomNumeric(Integer.parseInt(this.ic.get(i).min));
					break;

				case "b_true":
					sel = "true";
					break;

				case "b_false":
					sel = "false";
					break;

				case "lang":
					String[] languages = { "ita", "eng", "fra" };
					sel = languages[ThreadLocalRandom.current().nextInt(0, languages.length - 1)];
					break;

				default:
					System.out.println("[" + ansi().fgBright(RED).a("ERROR").reset() + "] [TestFrame] Url: "+ url+" Parameter: \""+this.ic.get(i).name+"\" Type \"" + type
							+ "\" of IC not implemented, put \"error\" string in place..");
					sel = "error";
					break;
				}
			}
			
			if (selUrl.contains("{" + this.ic.get(i).name + "}")) {
				selUrl = selUrl.replace("{" + this.ic.get(i).name + "}", sel);
			}

			if (selPayload.contains("{" + this.ic.get(i).name + "}")) {
				if (type.contains("int") || type.contains("float") || type.contains("double")) {
					selPayload = selPayload.replace("\"{" + this.ic.get(i).name + "}\"", sel);
				} else {
					selPayload = selPayload.replace("{" + this.ic.get(i).name + "}", sel);
				}
			}

		}

	}

	private String randomNotInt() {
		String[] val = { "1.5", "true", "a", "null" };

		return val[ThreadLocalRandom.current().nextInt(0, 4)];
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTfID() {
		return tfID;
	}

	public int getJsonID() {
		return jsonID;
	}

	public int getPathID() {
		return pathID;
	}

	public void setTfID(String tfID) {
		this.tfID = tfID;
	}

	public double getFailureProb() {
		return failureProb;
	}

	public void setFailureProb(double failureProb) {
		this.failureProb = failureProb;
	}

	public double getOccurrenceProb() {
		return occurrenceProb;
	}

	public void setOccurrenceProb(double occurrenceProb) {
		this.occurrenceProb = occurrenceProb;
	}

	public String getPayload() {
		return payload;
	}

	public String getState() {
		return state;
	}

	public int getFailureSeverity() {
		return failureSeverity;
	}

	public float getPriority() {
		return priority;
	}

	public void setPriority(float p) {
		this.priority = p;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getReqType() {
		return reqType;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
	}

	public String getUrl() {
		return this.url;
	}

	public String getSelUrl() {
		return this.selUrl;
	}

	public int getResponseCode() {
		return this.responseCode;
	}

	public long getResponseTime() {
		return this.responseTime;
	}

	public String getSelPayload() {
		return this.selPayload;
	}

	public String getFinalToken() {
		return finalToken;
	}

	public void setFinalToken(String finalToken) {
		this.finalToken = finalToken;
	}

	public int getPathMethodID() {
		return pathMethodID;
	}

	public void setPathMethodID(int pathMethodID) {
		this.pathMethodID = pathMethodID;
	}

	public void printExpectedResponses() {
		for (int i = 0; i < expectedResponses.size(); i++) {
			System.out.printf(expectedResponses.get(i) + " ");
		}
	}

	public void printExpectedResponses(PrintWriter testFile) {
		for (int i = 0; i < expectedResponses.size(); i++) {
			testFile.write(expectedResponses.get(i) + " ");

		}
	}

	public void printValidCombination() {
		System.out.printf("Valid/nonValid combination: ");
		for (int i = 0; i < ic.size(); i++) {
			if (ic.get(i).valid) {
				System.out.printf(" V");
			} else {
				System.out.printf(" NV");

			}
		}
	}

	public void printValidCombination(PrintWriter testFile) {
		testFile.println("Valid/nonValid combination: ");

		for (int i = 0; i < ic.size(); i++) {
			if (ic.get(i).valid) {
				testFile.write(" V");
			} else {
				testFile.write(" NV");
			}
		}
	}

	public void printTestFrame() {
		System.out.printf("- " + this.getName() + " | Method:" + this.getReqType() + " | #Ic: " + this.ic.size()
				+ " | Body: " + this.getPayload() + " | Expected responses: ");
		this.printExpectedResponses();
	}

	public void removeOneNotRequiredParam() {

		try {
			Random random = new Random();
			JsonParser parser = new JsonParser();
			JsonObject newPayload;
			ArrayList<Integer> indexes = new ArrayList<Integer>();
			int countParam = 0;
			int selectedParam;

			for (int i = 0; i < ic.size(); i++) {
				if (!ic.get(i).required) {
					countParam++;
					indexes.add(i);
				}
			}

			if (DEBUG_MODE)
				System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] Payload to modify: " + payload);

			newPayload = (JsonObject) parser.parse(payload).getAsJsonObject();

			if (countParam > 1) {
				selectedParam = random.nextInt(countParam - 1);
				newPayload.remove(ic.get(indexes.get(selectedParam)).name);
				ic.remove(indexes.get(selectedParam));
			} else {
				newPayload.remove(ic.get(indexes.get(0)).name);
				ic.remove(indexes.get(0));
			}

			if (DEBUG_MODE)
				System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [TestFrame] Payload after modify: "
						+ newPayload.toString());

			this.payload = newPayload.toString();
		} catch (Exception e) {
			System.out.println("[" + ansi().fgBright(YELLOW).a("WARNING").reset()
					+ "] [TestFrame] Error trying to remove random parameter from payload, use debug mode for additional info.. ");
		}
	}



}
