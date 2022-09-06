package com.microservice.uTest.controller;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.Color.YELLOW;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

import org.fusesource.jansi.AnsiConsole;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microservice.uTest.analyzer.TestFrameAnalyzer;
import com.microservice.uTest.executor.TestExecutor;
import com.microservice.uTest.generator.TestFrameGenerator;


public class OperationController {

	private boolean DEBUG_MODE = false;
	private boolean NOTIFY_MODE = true;
	private int CONFIG_PARAM_LWBOUND = 4;
	private int CONFIG_PARAM_UPBOUND = 11;
	public final ArrayList<String> testModeEnum = new ArrayList<String>(
			Arrays.asList("base_valid", "base_invalid", "base_valid_invalid", "full", "full_invalid", "full_bounded",
					"full_invalid_bounded", "pairwise", "pairwise_valid", "pairwise_invalid", "all_class"));
	public final ArrayList<String> loginModeEnum = new ArrayList<String>(Arrays.asList("no_login", "login", "token"));

	//private String debugMode = "";
	private String loginMode = "";
	private String testMode = "";
	private String executionMode = "";
	private String jsonsFile = "";
	private String username = "";
	private String password = "";
	private String token = "";
	private String loginUrl = "";
	private String priorityMode = "";
	private String priorityFocus = "";
	private int dynamicRequestBuffer;
	private int quickModeBound;
	private String path = "./sessionResources/";
	private ArrayList<String> jsonFiles = new ArrayList<String>();
	private ArrayList<String> configParameters = new ArrayList<String>();

	public int clean() {

		jsonFiles.clear();
		configParameters.clear();
		AnsiConsole.systemUninstall();

		return 0;
	}

	public boolean init(String configFile, PrintWriter logWriter) {

		AnsiConsole.systemInstall();
				
		System.out.println("\n[INFO] [OperationController] Initializing");
		logWriter.println("\n[INFO] [OperationController] Initializing");

		if (readConfig(configFile)) {

			System.out.println("[INFO] [OperationController] ***************** CONFIGURATION");
			logWriter.println("[INFO] [OperationController] ***************** CONFIGURATION");
			System.out.println("[INFO] [OperationController] Json File: " + jsonsFile);
			logWriter.println("[INFO] [OperationController] Json File: " + jsonsFile);
			System.out.println("[INFO] [OperationController] Generation mode: " + testMode);
			logWriter.println("[INFO] [OperationController] Generation mode: " + testMode);
			
			if (testMode.contains("bound")) {
				System.out.println("[INFO] [OperationController] 	Bound: " + quickModeBound);
				logWriter.println("[INFO] [OperationController] 	Bound: " + quickModeBound);
			}

			System.out.println("[INFO] [OperationController] Execution mode: " + executionMode);
			logWriter.println("[INFO] [OperationController] Execution mode: " + executionMode);
			System.out.println("[INFO] [OperationController] Prioritization mode: " + priorityMode);
			logWriter.println("[INFO] [OperationController] Prioritization mode: " + priorityMode);
			System.out.println("[INFO] [OperationController] Prioritization focus: " + priorityFocus);
			logWriter.println("[INFO] [OperationController] Prioritization focus: " + priorityFocus);
			System.out.println("[INFO] [OperationController] Random buffer: " + dynamicRequestBuffer);
			logWriter.println("[INFO] [OperationController] Random buffer: " + dynamicRequestBuffer);
			System.out.println("[INFO] [OperationController] Login mode: " + loginMode);
			logWriter.println("[INFO] [OperationController] Login mode: " + loginMode);
			
			if (loginMode.equals("login")) {
				System.out.println("[INFO] [OperationController] 	Username: " + username);
				logWriter.println("[INFO] [OperationController] 	Username: " + username);
				System.out.println("[INFO] [OperationController] 	Password: " + password);
				logWriter.println("[INFO] [OperationController] 	Password: " + password);
				System.out.println("[INFO] [OperationController] 	Login Url: " + loginUrl);
				logWriter.println("[INFO] [OperationController] 	Login Url: " + loginUrl);
			} else if (loginMode.equals("token")) {
				System.out.println("[INFO] [OperationController] 	Token: " + token);
				logWriter.println("[INFO] [OperationController] 	Token: " + token);
			}

			// Login Setting
			if (loginMode.equals("login")) {
				token = login();
				if (token != null) {
					System.out.println("\n[INFO] [OperationController] Login done, token acquired..");
					logWriter.println("\n[INFO] [OperationController] Login done, token acquired..");
				} else {
					System.out.println("\n[INFO] [OperationController] Login failed..");
					logWriter.println("\n[INFO] [OperationController] Login failed..");
				}
			} else if (loginMode.equals("token")) {
				if (token != null) {
					System.out.println("\n[INFO] [OperationController] Token correctly acquired from config file..");
					logWriter.println("\n[INFO] [OperationController] Token correctly acquired from config file..");
				} else {
					System.out.println("\n[INFO] [OperationController] Failed to get token from config file..");
					logWriter.println("\n[INFO] [OperationController] Failed to get token from config file..");
				}
			}

			// json files reading
			if (readJsons(RequestController.sessionPath + "/JSONDoc/" + jsonsFile, jsonFiles) == -1) {
				System.out.println("[" + ansi().fgBright(RED).a("ERROR").reset()
						+ "] [OperationController] Json file read failed");
				logWriter.println("[ERROR] [OperationController] Json file read failed");

				logWriter.flush();
				return false;
			}


		} else {
			System.out.println(
					"[" + ansi().fgBright(RED).a("ERROR").reset() + "] [OperationController] Configuration Failed");
			logWriter.println("[ERROR] [OperationController] Configuration failed");
			
			AnsiConsole.systemUninstall();
			logWriter.flush();
			return false;
		}
		
		AnsiConsole.systemUninstall();
		return true;
	}

	public String execute(String execOption, PrintWriter logWriter, String testFile, String statFile) {

		try {

			File newTestFile = new File(testFile);
			newTestFile.createNewFile();
			PrintWriter testWriter = new PrintWriter(new BufferedWriter(new FileWriter(testFile)));

			File newStatFile = new File(statFile);
			newStatFile.createNewFile();
			PrintWriter statWriter = new PrintWriter(new BufferedWriter(new FileWriter(statFile)));

			TestFrameGenerator tfg;

			tfg = new TestFrameGenerator(DEBUG_MODE);
			
			if (testMode.equals("full")) {
				tfg.setQuickMode(true);
			} else if (testMode.equals("pairwise")) {
				tfg.setPairWiseMode(true);
			} else if (testMode.equals("base_valid")) {
				tfg.setValidMode(true);
			} else if (testMode.equals("pairwise_valid")) {
				tfg.setValidModeP(true);
			} else if (testMode.equals("base_invalid")) {
				tfg.setInvalidModeP(true);
			} else if (testMode.equals("pairwise_invalid")) {
				tfg.setPairWiseNVMode(true);
			} else if (testMode.equals("base_valid_invalid")) {
				tfg.setValidInvalidMode(true);
			} else if (testMode.equals("full_bounded")) {
				tfg.setQuickBoundedMode(true);
				tfg.setQuickModeBound(quickModeBound);
			} else if (testMode.equals("full_invalid")) {
				tfg.setQuickNVMode(true);
			} else if (testMode.equals("full_invalid_bounded")) {
				tfg.setQuickBoundedNVMode(true);
				tfg.setQuickModeBound(quickModeBound);
			}

			System.out.println("[INFO] [OperationController] ***************** Generating Test ");
			logWriter.println("[INFO] [OperationController] ***************** Generating Test ");

			for (int i = 0; i < jsonFiles.size(); i++) {
				long startParsing = System.currentTimeMillis();
				
				int result = tfg.JsonURIParser(path + jsonFiles.get(i), i + 1, logWriter);
				
				long parsingTime = System.currentTimeMillis() - startParsing;
				
				if(result == 0) {
					System.out.println(
							"[INFO] [OperationController] Generation time: " + new SimpleDateFormat("mm:ss:SSS").format(new Date(parsingTime)));
					logWriter.println(
							"[INFO] [OperationController] Generation time: " + new SimpleDateFormat("mm:ss:SSS").format(new Date(parsingTime)));
				} else {
					System.out.println("[ERROR] [OperationController] Error generating test for JSON: " + jsonFiles.get(i));
					logWriter.println("[ERROR] [OperationController] Error generating test for JSON: " + jsonFiles.get(i));
				}


			}

			System.out.println("[INFO] [OperationController] ***************** Generation Done");
			logWriter.println("[INFO] [OperationController] ***************** Generation Done");
			
			tfg.printGenerationStatistics();
			tfg.printGenerationStatistics(statWriter);
			tfg.stampaTestFrames(testWriter);

			boolean execDone = false;
			ArrayList<Integer> tfs = new ArrayList<Integer>();
			
			System.out.println("\n[INFO] [OperationController] ***************** Requests Start");
			logWriter.println("\n[INFO] [OperationController] ***************** Requests Start");
			
			if (execOption.equals("all")) {

				TestExecutor te = new TestExecutor(DEBUG_MODE, NOTIFY_MODE, executionMode, token);
				
				long reqTime = te.ExecuteAllTest(tfg.testFrames, tfs, logWriter);

				System.out.println("\n[INFO] [OperationController] Requesting time: " + new SimpleDateFormat("mm:ss:SSS").format(new Date(reqTime)));
				logWriter.println("\n[INFO] [OperationController] Requesting time: " + new SimpleDateFormat("mm:ss:SSS").format(new Date(reqTime)));
				
				execDone = true;

			} else if (execOption.contains("random")) {

				String[] parts = execOption.split(":");

				int testNum = Integer.parseInt(parts[1]);

				TestExecutor te = new TestExecutor(DEBUG_MODE, NOTIFY_MODE, executionMode, token);
				
				logWriter.flush();
				long reqTime = te.ExecuteRandomTest(tfg.testFrames, tfs, testNum, logWriter);

				System.out.println("\n[INFO] [OperationController] Requesting time: " + new SimpleDateFormat("mm:ss:SSS").format(new Date(reqTime)));
				logWriter.println("\n[INFO] [OperationController] Requesting time: " + new SimpleDateFormat("mm:ss:SSS").format(new Date(reqTime)));
				execDone = true;

			} else if (execOption.contains("prioritization")) {

				TestExecutor te = new TestExecutor(DEBUG_MODE, NOTIFY_MODE, executionMode, token, priorityMode, priorityFocus,
						dynamicRequestBuffer);

				String[] parts = execOption.split(":");

				int testNum = Integer.parseInt(parts[1]);

				long reqTime = te.ExecuteDynamicPrioritizationTest(tfg.testFrames, tfs, testNum, logWriter);

				System.out.println("\n[INFO] [OperationController] Requesting time: " + new SimpleDateFormat("mm:ss:SSS").format(new Date(reqTime)));
				logWriter.println("\n[INFO] [OperationController] Requesting time: " + new SimpleDateFormat("mm:ss:SSS").format(new Date(reqTime)));
				execDone = true;

			} else if (execOption.contains("filePrioritization")) {

			} else if (execOption.contains("30random")) {

			} else if (execOption.contains("30prioritization")) {

			} else if (execOption.contains("APFD")) {

			} else {
				logWriter.println("[ERROR] [OperationController] Wrong execution option");
				logWriter.flush();
				testWriter.close();
				statWriter.close();
				return "[ERROR] [OperationController] Wrong execution option";
			}

			if (execDone) {
				
				System.out.println("[INFO] [Executor] ***************** Requests End");
				logWriter.println("[INFO] [Executor] ***************** Requests End");
				
				System.out.println("\n[INFO] [OperationController] ***************** Analysis");
				logWriter.println("\n[INFO] [OperationController] ***************** Analysis");

				TestFrameAnalyzer tfa = new TestFrameAnalyzer(DEBUG_MODE);

				tfa.analyzeTest(tfg.testFrames, tfs);
				
				statWriter.println("\n***************** Execution statistics");
				tfa.printExecutionStatistics(tfs);
				tfa.printStatistics(tfg, tfs, statWriter);
				tfa.printAllPathAndMethods(statWriter);

				tfa.printTest(tfg.testFrames, tfa.failedIndexes, "./sessionResources/output/failedTests.txt");

				tfa.printTest(tfg.testFrames, tfa.successIndexes, "./sessionResources/output/successTests.txt");

				tfa.printTestToFile(tfg, tfs, "./sessionResources/output/testFeatures.txt");
				
				tfa.printReliabilities(tfg.testFrames, tfs, "./sessionResources/output/reliabilities.csv");

			}

			System.out.println("[INFO] [OperationController] ***************** Finished");
			logWriter.println("[INFO] [OperationController] ***************** Finished");

			logWriter.flush();
			testWriter.close();
			statWriter.close();

		} catch (IOException e) {
			System.out.println(
					"[" + ansi().fgBright(RED).a("ERROR").reset() + "] [OperationController] Unexpected IOException");
			logWriter.println("[ERROR] [OperationController] [OperationController] Unexpected IOException");
		}

		return "[INFO] Execution done\n";
	}

	private int readJsons(String fileToRead, ArrayList<String> jsonFiles) {
		try {
			File myObj = new File(fileToRead);
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				if (!data.contains("**"))
					jsonFiles.add(data);
			}
			myReader.close();
		} catch (Exception e) {
			System.out.println("[" + ansi().fgBright(RED).a("ERROR").reset() + "] Json retrieval: File read failed.");
			e.printStackTrace();
			return -1;
		}

		return 0;
	}

	private String login() {
		int time = 5000;

		HttpURLConnection con = null;
		StringBuffer response = null;

		try {
			URL obj = new URL(loginUrl);
			con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setConnectTimeout(time);
			con.setReadTimeout((time));

			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());

			wr.write("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}");

			wr.flush();

			int responseCode = con.getResponseCode();

			if (responseCode < 300) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);

				}
				in.close();
			}

		} catch (IOException e) {
			System.out.println("\n" + "[" + ansi().fgBright(RED).a("ERROR").reset() + "] Login: Connection problem.");
			System.out.println("\n" + "[" + ansi().fgBright(RED).a("ERROR").reset() + "] Login: Retrying");
			
			try {
			URI loginURI = new URI("http", null, "internal.docker.host",
					12340, null, null, null);
			loginURI = UriComponentsBuilder.fromUri(loginURI).path("/api/v1/users/login")
					.build(true).toUri();

			System.out.println("[INFO] [OperationController] Sending login request to: " + loginURI.toString());

			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");

			String loginBody = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";

			System.out.println("[INFO] [OperationController] Body: " + loginBody);

			HttpEntity<String> httpEntity = new HttpEntity<>(loginBody, headers);
			RestTemplate restTemplate = new RestTemplate();

			ResponseEntity loginResponse = restTemplate.exchange(loginURI, HttpMethod.POST, httpEntity,
					String.class);
			
			System.out.println("[INFO] [OperationController] Login request response: " + loginResponse.getBody());
			
			} catch(Exception ex) {
				System.out.println("\n" + "[" + ansi().fgBright(RED).a("ERROR").reset() + "] Login: Retry failed.");
			}
			
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}

		if (response != null)
			return getToken(response.toString());
		else
			return null;
	}

	private String getToken(String json) {
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(json);
		JsonObject obj = element.getAsJsonObject();
		JsonObject data = obj.getAsJsonObject("data");

		return data.get("token").toString().replace("\"", "");
	}

	private boolean readConfig(String configFile) {
		try {
			File myObj = new File(configFile);
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				if (!data.contains("**"))
					configParameters.add(data);
			}
			myReader.close();
		} catch (Exception e) {
			System.out.println("[" + ansi().fgBright(RED).a("ERROR").reset() + "] Configuration: File read failed.");
		}

		return parseConfigParameters();
	}

	private boolean parseConfigParameters() {

		int index = 0;

		try {
			if (configParameters.size() >= CONFIG_PARAM_LWBOUND && configParameters.size() <= CONFIG_PARAM_UPBOUND) {
				// jsonsFile parsing
				jsonsFile = configParameters.get(index++).split("=")[1];
				testMode = configParameters.get(index++).split("=")[1];

				if (testModeEnum.contains(testMode)) {

					if (testMode.contains("bound"))
						quickModeBound = Integer.parseInt(configParameters.get(index++).split("=")[1]);

					executionMode = configParameters.get(index++).split("=")[1];
					priorityMode = configParameters.get(index++).split("=")[1];
					priorityFocus = configParameters.get(index++).split("=")[1];
					dynamicRequestBuffer = Integer.parseInt(configParameters.get(index++).split("=")[1]);
					loginMode = configParameters.get(index++).split("=")[1];

					if (loginModeEnum.contains(loginMode)) {

						if (loginMode.equals("login")) {

							username = configParameters.get(index++).split("=")[1];
							password = configParameters.get(index++).split("=")[1];
							loginUrl = configParameters.get(index++).split("=")[1];

						} else if (loginMode.equals("token")) {

							token = configParameters.get(index++).split("=")[1];

						}

						return true;

					} else {
						System.out.println("[" + ansi().fgBright(RED).a("ERROR").reset()
								+ "] Configuration parsing: Wrong Login Mode");
						return false;
					}

				} else {
					System.out.println("[" + ansi().fgBright(RED).a("ERROR").reset()
							+ "] Configuration parsing: Wrong Generation Mode");
					return false;
				}

			} else {
				System.out.println("[" + ansi().fgBright(RED).a("ERROR").reset()
						+ "] Configuration parsing: Wrong configuration parameters number, follow instructions.");
				return false;
			}

		} catch (Exception e) {
			System.out.println(
					"[" + ansi().fgBright(RED).a("ERROR").reset() + "] Configuration parsing: Unexpected error.");
			return false;
		}

	}
	
	/**********************************************************************************/
	
	public void generateJson(String host, String _path, ArrayList<String> ports) {
		
		if(ports.size() > 0) {
		    for(int i=0; i<ports.size(); i++) {
		    	StringBuffer respJson = getJson(host, _path, i, ports);
		    	if(respJson != null) {
		    		writeJson(respJson, i, ports);
		    		System.out.println("[INFO] [OperationController] Json \"swagger_"+ ports.get(i)+"\" generated..");
		    	}else {System.out.println("[ERROR] [OperationController] Json response null on port "+ ports.get(i));}
		    }		
		}
	}
		
	
	private void writeJson(StringBuffer toWrite,int index, ArrayList<String> ports) {
		try {

		      FileWriter myWriter = new FileWriter(RequestController.sessionPath + "/JSONDoc/Files/" + "swagger_"+ports.get(index)+".json");
		      myWriter.write(toWrite.toString());
		      myWriter.close();
		      
	      } catch (Exception e) {
	        System.out.println("[ERROR] [OperationController] File writing error, port "+ ports.get(index));
	        
	      }
	}
	
	private StringBuffer getJson(String host, String _path, int index, ArrayList<String>ports) {
		String url;
		if(_path != null){
			url = "http://"+host+":"+ports.get(index)+_path+"/v2/api-docs";
		}else {
			url = "http://"+host+":"+ports.get(index)+"/v2/api-docs";
		}
		int time=5000;
		
		System.out.println("[INFO] [OperationController] Requesting json to url: " + url);
		
		
		HttpURLConnection con = null;
		StringBuffer response = null;

		try {
			URL obj = new URL(url);
			con = (HttpURLConnection) obj.openConnection();	
			
			con.setRequestMethod("GET");

			con.setRequestProperty("Content-Type", "application/json");

			con.setConnectTimeout(time);
			con.setReadTimeout((time));

			int responseCode = con.getResponseCode();
			
			if(responseCode < 300){
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				response = new StringBuffer();
	
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			}
			
		} catch (IOException e) {
			
	        System.out.println("[ERROR] [OperationController] Connection problem, port "+ ports.get(index));
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
		return response;
	}
	
	
	
	

}
