package com.microservice.uTest.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
public class RequestController {

	protected final static String sessionPath = "./sessionResources";
	protected final static String outputPath = "./sessionResources/output";
	private PrintWriter logWriter;
	private OperationController opController = null;

	RequestController() {

		try {
			Path pathBase = Paths.get(sessionPath);
			Path path1 = Paths.get(sessionPath + "/JSONDoc");
			Path path2 = Paths.get(sessionPath + "/JSONDoc/Files");
			Path path3 = Paths.get(outputPath);

			Files.createDirectories(pathBase);
			Files.createDirectories(path1);
			Files.createDirectories(path2);
			Files.createDirectories(path3);

			File newLogFile = new File(outputPath + "/log.txt");
			newLogFile.createNewFile();
			logWriter = new PrintWriter(new BufferedWriter(new FileWriter(outputPath + "/log.txt")));
		} catch (IOException e) {

			System.out.println("[ERROR] [RequestController] Logfile creation error");
		}

		System.out.println("\n[INFO] [RequestController] Initialization complete");
	}

	@GetMapping("/execute")
	String getExecute(@RequestParam String execOption) {
		String ret = "";

		System.out.println("\n[INFO] [RequestController] Execute \"" + execOption + "\" GET request received");
		logWriter.println("[INFO] [RequestController] Execute \"" + execOption + "\"GET request received");

		if (opController != null) {
			System.out.println("[INFO] [RequestController] OperationController already initialized");
			logWriter.println("[INFO] [RequestController] OperationController already initialized");
			logWriter.flush();
			ret = opController.execute(execOption, logWriter, outputPath + "/testsuite.txt", outputPath + "/stats.txt");
		} else {
			System.out.println("[INFO] [RequestController] OperationController not initialized");
			logWriter.println("[INFO] [RequestController] OperationController not initialized");

			opController = new OperationController();

			if (opController.init(sessionPath + "/config.txt", logWriter)) {
				ret = opController.execute(execOption, logWriter, outputPath + "/testsuite.txt",
						outputPath + "/stats.txt");
			} else {
				opController = null;
				ret = "[ERROR] [RequestController] Not Expected error";
			}
		}

		return ret;
	}

	@GetMapping("/clean")
	String getClean() {
		System.out.println("[INFO] [RequestController] Clean all GET request received");
		logWriter.println("[INFO] [RequestController] Clean all GET request received");

		try {

			FileUtils.cleanDirectory(new File(sessionPath + "/"));

			Path path1 = Paths.get(sessionPath + "/JSONDoc");
			Path path2 = Paths.get(sessionPath + "/JSONDoc/Files");
			Path path3 = Paths.get(outputPath);

			Files.createDirectories(path1);
			Files.createDirectories(path2);
			Files.createDirectories(path3);

			opController = null;

		} catch (IOException e) {
			e.printStackTrace();
			return "[ERROR] Clean failed\n";
		}

		return "[INFO] Clean success\n";
	}

	@GetMapping("/cleanEnv")
	String getCleanEnv() {
		System.out.println("[INFO] [RequestController] Clean env GET request received");
		logWriter.println("[INFO] [RequestController] Clean env GET request received");

		opController = null;

		return "[INFO] Clean environment success";
	}

	@PostMapping("/specification")
	String postSpecification(@RequestBody String specFile, @RequestParam String filename) {
		System.out.println("[INFO] [RequestController] Specification \"" + filename + "\" POST request received");
		logWriter.println("[INFO] [RequestController] Specification \"" + filename + "\"POST request received");

		try {
			File newFile = new File(sessionPath + "/JSONDoc/Files/" + filename);
			if (newFile.createNewFile()) {
				FileWriter writer = new FileWriter(newFile);
				writer.write(specFile);
				writer.close();
			} else {
				return "Specification upload error..";
			}
		} catch (IOException e) {
			System.out.println("[INFO] [RequestController] Specification POST error");
			logWriter.println("[INFO] [RequestController] Specification POST error");
			return "Specification upload error..";
		}

		return "[INFO] Specification upload completed\n";
	}

	@PostMapping("/configuration")
	String postConfiguration(@RequestBody String confFile, @RequestParam String filename) {
		System.out.println("[INFO] [RequestController] Configuration POST request received");
		logWriter.println("[INFO] [RequestController] Configuration POST request received");

		try {
			File newFile = new File(sessionPath + "/" + filename);
			if (newFile.createNewFile()) {
				FileWriter writer = new FileWriter(newFile, false);
				writer.write(confFile);
				writer.close();
			} else {
				return "Configuration upload error..";
			}
		} catch (IOException e) {
			System.out.println("[INFO] [RequestController] Configuration \"" + filename + "\" POST error");
			logWriter.println("[INFO] [RequestController] Configuration \"" + filename + "\" POST error");
			return "Configuration upload error..";
		}
		return "[INFO] Configuration upload completed\n";
	}

	@PostMapping("/jsonfilestxt")
	String postJsonFiles(@RequestBody String jsonsFileTXT, @RequestParam String filename) {
		System.out.println("[INFO] [RequestController] JSONFilesTXT \"" + filename + "\" POST request received");
		logWriter.println("[INFO] [RequestController] JSONFilesTXT \"" + filename + "\"POST request received");

		try {
			File newFile = new File(sessionPath + "/JSONDoc/" + filename);
			if (newFile.createNewFile()) {
				FileWriter writer = new FileWriter(newFile);
				writer.write(jsonsFileTXT);
				writer.close();
			} else {
				return "JSONFilesTXT upload error..";
			}
		} catch (IOException e) {
			System.out.println("[INFO] [RequestController] JSONFilesTXT POST error");
			logWriter.println("[INFO] [RequestController] JSONFilesTXT POST error");
			return "JSONFilesTXT upload error..";
		}
		return "[INFO] JSONFilesTXT upload completed\n";
	}

	@PostMapping("/retrieveSpecifications")
	String postJsonFiles(@RequestBody String conf) {
		System.out.println("[INFO] [RequestController] retrieveSpecifications POST request received");
		logWriter.println("[INFO] [RequestController] retrieveSpecifications POST request received");

		try {

			System.out.println("[INFO] [RequestController] Data:");

			JsonObject jsonObject = new JsonParser().parse(conf).getAsJsonObject();
			String host = jsonObject.get("host").getAsString();
			String path = jsonObject.get("path").getAsString();
			if (path.equals("")) {
				path = null;
			}
			System.out.println("[INFO] [RequestController] 	host: " + host);
			System.out.println("[INFO] [RequestController] 	path: " + path);
			JsonArray entries = jsonObject.get("ports").getAsJsonArray();

			ArrayList<String> ports = new ArrayList<String>();
			System.out.println("[INFO] [RequestController] 	ports:");

			for (JsonElement entry : entries) {
				String portTemp = entry.getAsString();
				System.out.print(" " + portTemp);
				ports.add(portTemp);
			}

			if (opController == null) {
				opController = new OperationController();
			} 
			
			opController.generateJson(host, path, ports);
			
			opController = null;
			
		} catch (Exception e) {
			System.out.println("[INFO] [RequestController] retrieveSpecifications POST error");
			logWriter.println("[INFO] [RequestController] retrieveSpecifications POST error");
			return "retrieveSpecifications upload error..";
		}
		return "[INFO] retrieveSpecifications completed\n";
	}

	@GetMapping("/file")
	String getFile(@RequestParam String fileID) {
		System.out.println("[INFO] [RequestController] File \"" + fileID + "\" GET request received");
		logWriter.println("[INFO] [RequestController] File GET \"" + fileID + "\"request received");
		try {

			String data = new String();
			File myObj = null;
			Scanner myReader = null;

			switch (fileID) {

			case "log":
				myObj = new File(outputPath + "/log.txt");
				myReader = new Scanner(myObj);
				while (myReader.hasNextLine()) {
					data += myReader.nextLine() + "\n";
				}
				myReader.close();
				return data;

			case "failed":
				myObj = new File(outputPath + "/failedTests.txt");
				myReader = new Scanner(myObj);
				while (myReader.hasNextLine()) {
					data += myReader.nextLine() + "\n";
				}
				myReader.close();
				return data;

			case "success":
				myObj = new File(outputPath + "/successTests.txt");
				myReader = new Scanner(myObj);
				while (myReader.hasNextLine()) {
					data += myReader.nextLine() + "\n";
				}
				myReader.close();
				return data;

			case "stats":
				myObj = new File(outputPath + "/stats.txt");
				myReader = new Scanner(myObj);
				while (myReader.hasNextLine()) {
					data += myReader.nextLine() + "\n";
				}
				myReader.close();
				return data;

			case "features":
				myObj = new File(outputPath + "/testFeatures.txt");
				myReader = new Scanner(myObj);
				while (myReader.hasNextLine()) {
					data += myReader.nextLine() + "\n";
				}
				myReader.close();
				return data;

			case "testsuite":
				myObj = new File(outputPath + "/testsuite.txt");
				myReader = new Scanner(myObj);
				while (myReader.hasNextLine()) {
					data += myReader.nextLine() + "\n";
				}
				myReader.close();
				return data;

			case "reliabilities":
				myObj = new File(outputPath + "/reliabilities.csv");
				myReader = new Scanner(myObj);
				while (myReader.hasNextLine()) {
					data += myReader.nextLine() + "\n";
				}
				myReader.close();
				return data;

			default:
				System.out.println("[INFO] [RequestController] File GET request wrong request");
				logWriter.println("[INFO] [RequestController] File GET request wrong request");
				return "[ERROR] Wrong request";

			}
		} catch (FileNotFoundException e) {
			System.out.println("[ERROR] [RequestController] File not found");
			logWriter.println("[INFO] [RequestController] File not found");
			return "[ERROR] File not found..";
		}

	}

}
