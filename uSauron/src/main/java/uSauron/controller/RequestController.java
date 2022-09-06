package uSauron.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import uSauron.builder.ComposeBuilder;
import uSauron.collector.InfoCollector;
import uSauron.graph.Graph;
import uSauron.graph.Vertex;
import uSauron.main.USauronApplication;

@Controller
public class RequestController {
	
	public final static String sessionPath = "./sessionResources";
	
	
	RequestController() {

		try {
			Path pathBase = Paths.get(sessionPath);

			Files.createDirectories(pathBase);

		} catch (IOException e) {

			System.out.println("[ERROR] [RequestController] Initialization error");
		}

		System.out.println("\n[INFO] [RequestController] Initialization complete");
	}
	
	@RequestMapping(value = "/proxyCompose", method = RequestMethod.POST)
	@ResponseBody
	String proxyCompose(@RequestBody String _composeFile) {
		System.out.println("[INFO] [RequestController] Received \"proxy compose\" request");
		return ComposeBuilder.proxyCompose(_composeFile);
	}

	@RequestMapping(value = "/proxyInfo", method = RequestMethod.POST)
	@ResponseBody
	String proxyInfo(@RequestBody String _infoPacket, @RequestParam String proxyID) {
		System.out.println("[INFO] [RequestController] Info received from proxy: " + proxyID);

		InfoCollector collector = InfoCollector.getInstance();

		collector.savePacket(proxyID, _infoPacket);

		return "uSauron-ACK";
	}

	@RequestMapping(value = "/recordStart", method = RequestMethod.GET)
	@ResponseBody
	String testStart(@RequestParam String testID) {
		System.out.println("[INFO] [RequestController] Received \"record start\" on testID: " + testID);

		InfoCollector collector = InfoCollector.getInstance();

		collector.initSession(testID);
		collector.recordSession = true;
		collector.sessionKey = Integer.parseInt(testID);

		return "uSauron-ACK";
	}

	@RequestMapping(value = "/recordStop", method = RequestMethod.POST)
	@ResponseBody
	String testStop(@RequestParam String testID, @RequestBody String info) {
		System.out.println("[INFO] [RequestController] Received \"record stop\" on testID: " + testID);

		InfoCollector collector = InfoCollector.getInstance();

		JsonArray obj = new JsonParser().parse(info).getAsJsonArray();

		collector.closeSession(obj.get(0).getAsInt(), obj.get(1).getAsInt(), obj.get(2).getAsString(), obj.get(3).getAsString(),
				obj.get(4).getAsInt(), obj.get(5).getAsInt());

		collector.recordSession = false;
		collector.sessionKey = -1;

		return "uSauron-ACK";
	}

	@RequestMapping(value = "/getInfo", method = RequestMethod.GET)
	@ResponseBody
	String getInfo(@RequestParam String format) {
		System.out.println("[INFO] [RequestController] Received \"get info\" request with format: " + format);
		InfoCollector collector = InfoCollector.getInstance();

		return collector.getInfo(format);
	}

	@RequestMapping(value = "/getSpecInfo", method = RequestMethod.GET)
	@ResponseBody
	String getSpecInfo(@RequestParam String conf) {
		System.out.println("[INFO] [RequestController] Received \"get spec info\" request with format: " + conf);
		InfoCollector collector = InfoCollector.getInstance();

		return collector.getSpecInfo(conf);
	}

	@RequestMapping(value = "/getServiceInfo", method = RequestMethod.GET)
	@ResponseBody
	String getServiceInfo(@RequestParam String _serviceName) {
		System.out.println("[INFO] [RequestController] Received \"get info of service\" request");
		return null;
	}

	@RequestMapping(value = "/getStats", method = RequestMethod.GET)
	@ResponseBody
	String getStats(@RequestParam String format) {
		System.out.println("[INFO] [RequestController] Received \"get stats\" request with format: " + format);
		InfoCollector collector = InfoCollector.getInstance();

		return collector.getStats(format);
	}

	@RequestMapping(value = "/getBugReport", method = RequestMethod.GET)
	@ResponseBody
	String getBugReport(@RequestParam String format) {
		System.out.println("[INFO] [RequestController] Received \"get bug report\" request with format: " + format);
		InfoCollector collector = InfoCollector.getInstance();

		return collector.getBugReport(format);
	}

	@RequestMapping(value = "/getDependencies", method = RequestMethod.GET)
	@ResponseBody
	String getDependencies(@RequestParam String option) {
		System.out.println("[INFO] [RequestController] Received \"get dependencies\" with option: " + option);
		InfoCollector collector = InfoCollector.getInstance();

		return collector.getDependencies(option);
	}

	@RequestMapping(value = "/clear", method = RequestMethod.GET)
	@ResponseBody
	String clear() {
		System.out.println("[INFO] [RequestController] Received \"clear\" request");
		InfoCollector collector = InfoCollector.getInstance();
		
		return collector.clear();
	}
	
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	@ResponseBody
	String uploadFiles(@RequestBody String file, @RequestParam String option, @RequestParam String filename) {
		System.out.println("[INFO] [RequestController] upload option\"" + option + "\" POST request received, filename: " + filename);

		if (option.equalsIgnoreCase("groundtruth")) {
			try {
				File newFile = new File(sessionPath + "/"+ filename + ".json");
				if (newFile.createNewFile()) {
					FileWriter writer = new FileWriter(newFile);
					writer.write(file);
					writer.close();
				} else {
					return "[ERROR] groundtrouth upload error..";
				}
			} catch (IOException e) {
				System.out.println("[INFO] [RequestController] groundtrouth POST error");
				return "[ERROR] groundtrouth upload error..";
			}
			return "[INFO] groundtrouth upload completed\n";
		}else {
			return "[ERROR] Not valid option\n";
		}
	}
	
	@RequestMapping(value = "/dependenciesCov", method = RequestMethod.POST)
	@ResponseBody
	String dependenciesCov(@RequestBody String dependencies, @RequestParam String filename) {
		System.out.println("[INFO] [RequestController] dependenciesCov POST request received");
		
		Float cov = (float) 0;
		int gndTDep = 0;
		int toCompDep = 0;
		String gndtS = readFile(sessionPath, "/" + filename +".json");
			
		Gson gson = new Gson();

		HashMap<String, ArrayList<String>> gndtGraph = gson.fromJson(gndtS, HashMap.class);
		HashMap<String, ArrayList<String>> compareGraph = gson.fromJson(dependencies, HashMap.class);
		
		for (HashMap.Entry<String, ArrayList<String>> entry : gndtGraph.entrySet()) {
			gndTDep += entry.getValue().size();
		}
		
		for (HashMap.Entry<String, ArrayList<String>> entry : compareGraph.entrySet()) {
			toCompDep += entry.getValue().size();
		}
		
		cov = (float) toCompDep / gndTDep;
		
		return cov.toString();
	}
	
	@RequestMapping(value = "/dependenciesCovXM", method = RequestMethod.GET)
	@ResponseBody
	String dependenciesCovXM(@RequestParam String format) {
		System.out.println("[INFO] [RequestController] dependenciesCovXM GET request received");
		InfoCollector collector = InfoCollector.getInstance();

		return collector.getDependenciesCov(format);
	}
	
	private String readFile(String path, String filename) {
		
		String data = "";
		
		try {
			File myObj = new File(path+filename);
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				data += myReader.nextLine();
			}
			myReader.close();
		} catch (Exception e) {
			System.out.println("[ERROR] [RequestController] File read failed");
		}
		
		
		return data;
	}

}
