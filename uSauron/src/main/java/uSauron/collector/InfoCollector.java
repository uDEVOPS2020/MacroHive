package uSauron.collector;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import uSauron.controller.RequestController;
import uSauron.dataStructure.InfoPacket;
import uSauron.dataStructure.TestSession;
import uSauron.dataStructure.UService;
import uSauron.graph.Graph;
import uSauron.graph.Vertex;

public class InfoCollector {

	public static InfoCollector collector = null;

	public boolean recordSession = false;
	public int sessionKey = -1;

	private HashMap<Integer, TestSession> sessions;

	private InfoCollector() {
		this.sessions = new HashMap<Integer, TestSession>();
	}

	public static InfoCollector getInstance() {
		if (collector == null)
			collector = new InfoCollector();
		return collector;
	}

	public String getInfo(String format) {

		String info = "Format not supported";

		if (format.equalsIgnoreCase("json")) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			info = gson.toJson(sessions);
		} else if (format.equalsIgnoreCase("csv")) {
			info = getInfoAsCSVString();
		}
		return info;
	}

	public void savePacket(String proxyID, String infoPacket) {

		TestSession tempSession;

		if (sessions.containsKey(sessionKey)) {
			tempSession = sessions.get(sessionKey);
		} else {
			tempSession = new TestSession();
		}

		tempSession.addPacketToService(proxyID, infoPacket);

		this.sessions.put(sessionKey, tempSession);
	}

	private String getInfoAsCSVString() {

		String out = "sessionKey,serviceName,packetMethod,packetUriSender,packetResponseCode,packetResponseTime\n";

		int sessionKey;

		String serviceName;

		String packetMethod;
		String packetUriSender;
		int packetResponseCode;
		long packetResponseTime;

		for (HashMap.Entry<Integer, TestSession> session : sessions.entrySet()) {

			sessionKey = session.getKey();

			for (int i = 0; i < session.getValue().getServices().size(); i++) {
				serviceName = session.getValue().getServices().get(i).getName();

				for (int j = 0; j < session.getValue().getServices().get(i).getPackets().size(); j++) {
					packetMethod = session.getValue().getServices().get(i).getPackets().get(j).getMethod();
					packetUriSender = session.getValue().getServices().get(i).getPackets().get(j).geturiSender();
					packetResponseCode = session.getValue().getServices().get(i).getPackets().get(j).getResponseCode();
					packetResponseTime = session.getValue().getServices().get(i).getPackets().get(j).getResponseTime();

					out += sessionKey + "," + serviceName + "," + packetMethod + "," + packetUriSender + ","
							+ packetResponseCode + "," + packetResponseTime + "\n";
				}
			}
		}

		return out;
	}

	public String getSpecInfo(String conf) {

		String ret = "";
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		ArrayList<UService> servicesArray = new ArrayList<UService>();
		HashMap<String, Float> relServ = new HashMap<String, Float>();
		HashMap<String, Float> relServXM = new HashMap<String, Float>();
		HashMap<String, Integer> methodCount = new HashMap<String, Integer>();
		HashMap<String, Object> toPrint = new HashMap<String, Object>();

		if (conf.equalsIgnoreCase("services")) {
			for (HashMap.Entry<Integer, TestSession> session : sessions.entrySet()) {

				for (int i = 0; i < session.getValue().getServices().size(); i++) {

					int x = 0;
					boolean exist = false;
					while (x < servicesArray.size() && !exist) {
						if (servicesArray.get(x).getName().equals(session.getValue().getServices().get(i).getName())) {
							exist = true;
						} else {
							x++;
						}
					}

					if (!exist) {
						servicesArray.add(new UService(session.getValue().getServices().get(i).getName()));
						x = servicesArray.size() - 1;
					}

					for (int j = 0; j < session.getValue().getServices().get(i).getPackets().size(); j++) {
						servicesArray.get(x).getPackets()
								.add(session.getValue().getServices().get(i).getPackets().get(j));
					}
				}
			}

			for (int i = 0; i < servicesArray.size(); i++) {

				HashMap<String, ArrayList<String>> countMet = new HashMap<String, ArrayList<String>>();
				HashMap<String, ArrayList<String>> countMetPck = new HashMap<String, ArrayList<String>>();

				int countSF = 0;
				int countSFXM = 0;
				int countPcktsM = 0;
				float rel = (float) 0;
				float relXM = (float) 0;

				for (int j = 0; j < servicesArray.get(i).getPackets().size(); j++) {

					if (countMetPck.containsKey(servicesArray.get(i).getPackets().get(j).getUrl())) {
						if (!countMetPck.get(servicesArray.get(i).getPackets().get(j).getUrl())
								.contains(servicesArray.get(i).getPackets().get(j).getMethod())) {
							countPcktsM++;
							countMetPck.get(servicesArray.get(i).getPackets().get(j).getUrl())
									.add(servicesArray.get(i).getPackets().get(j).getMethod());
						}
					} else {
						countPcktsM++;
						ArrayList<String> tempAS = new ArrayList<String>();
						tempAS.add(servicesArray.get(i).getPackets().get(j).getMethod());
						countMetPck.put(servicesArray.get(i).getPackets().get(j).getUrl(), tempAS);
					}

					if (servicesArray.get(i).getPackets().get(j).getResponseCode() >= 500) {
						countSF++;
						if (countMet.containsKey(servicesArray.get(i).getPackets().get(j).getUrl())) {
							if (!countMet.get(servicesArray.get(i).getPackets().get(j).getUrl())
									.contains(servicesArray.get(i).getPackets().get(j).getMethod())) {
								countSFXM++;
								countMet.get(servicesArray.get(i).getPackets().get(j).getUrl())
										.add(servicesArray.get(i).getPackets().get(j).getMethod());
							}
						} else {
							countSFXM++;
							ArrayList<String> tempAS = new ArrayList<String>();
							tempAS.add(servicesArray.get(i).getPackets().get(j).getMethod());
							countMet.put(servicesArray.get(i).getPackets().get(j).getUrl(), tempAS);
						}

					}
				}

				rel = 1 - (float) countSF / servicesArray.get(i).getPackets().size();
				relServ.put(servicesArray.get(i).getName(), rel);

				relXM = 1 - (float) countSFXM / countPcktsM;
				relServXM.put(servicesArray.get(i).getName(), relXM);
				methodCount.put(servicesArray.get(i).getName(), countPcktsM);
			}

			toPrint.put("ReliabilitiesXM", relServXM);
			toPrint.put("Reliabilities", relServ);
			toPrint.put("Services details", servicesArray);
			toPrint.put("Methods XS", methodCount);

		} else if (conf.equalsIgnoreCase("pathServices")) {

			HashMap<Integer, ArrayList<UService>> pathMethodServices = new HashMap<Integer, ArrayList<UService>>();
			HashMap<Integer, HashMap<Integer, TestSession>> tempPathMethodSessions = new HashMap<Integer, HashMap<Integer, TestSession>>();

			for (HashMap.Entry<Integer, TestSession> session : sessions.entrySet()) {
				if (tempPathMethodSessions.containsKey(session.getValue().getPathMethodID())) {
					tempPathMethodSessions.get(session.getValue().getPathMethodID()).put(session.getKey(),
							session.getValue());
				} else {
					tempPathMethodSessions.put(session.getValue().getPathMethodID(),
							new HashMap<Integer, TestSession>());
					tempPathMethodSessions.get(session.getValue().getPathMethodID()).put(session.getKey(),
							session.getValue());
				}
			}

			for (HashMap.Entry<Integer, HashMap<Integer, TestSession>> pathMethod : tempPathMethodSessions.entrySet()) {

				pathMethodServices.put(pathMethod.getKey(), new ArrayList<UService>());

				for (HashMap.Entry<Integer, TestSession> session : pathMethod.getValue().entrySet()) {

					for (int i = 0; i < session.getValue().getServices().size(); i++) {

						int x = 0;
						boolean exist = false;
						while (x < pathMethodServices.get(pathMethod.getKey()).size() && !exist) {
							if (pathMethodServices.get(pathMethod.getKey()).get(x).getName()
									.equals(session.getValue().getServices().get(i).getName())) {
								exist = true;
							} else {
								x++;
							}
						}

						if (!exist) {
							pathMethodServices.get(pathMethod.getKey())
									.add(new UService(session.getValue().getServices().get(i).getName()));
							x = pathMethodServices.get(pathMethod.getKey()).size() - 1;
						}

						for (int j = 0; j < session.getValue().getServices().get(i).getPackets().size(); j++) {
							pathMethodServices.get(pathMethod.getKey()).get(x).getPackets()
									.add(session.getValue().getServices().get(i).getPackets().get(j));
						}
					}
				}
			}

			toPrint.put("PathMethod details", pathMethodServices);

		} else if (conf.equalsIgnoreCase("internalcoverage")) {

			HashMap<String, HashMap<String, HashMap<Integer, ArrayList<InfoPacket>>>> servicePathMethodPackets = new HashMap<String, HashMap<String, HashMap<Integer, ArrayList<InfoPacket>>>>();
			HashMap<String, HashMap<Integer, HashMap<Integer, TestSession>>> pathMethodTest = new HashMap<String, HashMap<Integer, HashMap<Integer, TestSession>>>();

			HashMap<String, HashMap<String, Float>> coverages3JSON = new HashMap<String, HashMap<String, Float>>();
			HashMap<String, HashMap<String, Float>> coverages2JSON = new HashMap<String, HashMap<String, Float>>();

			HashMap<String, HashMap<Integer, Float>> coverages3JSONLvl = new HashMap<String, HashMap<Integer, Float>>();
			HashMap<String, HashMap<Integer, Float>> coverages2JSONLvl = new HashMap<String, HashMap<Integer, Float>>();

			HashMap<String, HashMap<Integer, Float>> delta3JSONLvl = new HashMap<String, HashMap<Integer, Float>>();
			HashMap<String, HashMap<Integer, Float>> delta2JSONLvl = new HashMap<String, HashMap<Integer, Float>>();

			for (HashMap.Entry<Integer, TestSession> session : sessions.entrySet()) {

				if (session.getKey() != -1) {
					if (session.getValue().getJsonID() != null) {

						String jsonID = session.getValue().getJsonID().toString();

						if (!pathMethodTest.containsKey(jsonID)) {
							pathMethodTest.put(jsonID, new HashMap<Integer, HashMap<Integer, TestSession>>());
						}

						if (!pathMethodTest.get(jsonID).containsKey(session.getValue().getPathMethodID())) {
							pathMethodTest.get(jsonID).put(session.getValue().getPathMethodID(),
									new HashMap<Integer, TestSession>());
						}

						pathMethodTest.get(jsonID).get(session.getValue().getPathMethodID()).put(session.getKey(),
								new TestSession(session.getValue()));

						for (int i = 0; i < session.getValue().getServices().size(); i++) {

							if (!servicePathMethodPackets.containsKey(jsonID)) {
								servicePathMethodPackets.put(jsonID,
										new HashMap<String, HashMap<Integer, ArrayList<InfoPacket>>>());
							}

							if (!servicePathMethodPackets.get(jsonID)
									.containsKey(session.getValue().getServices().get(i).getName())) {
								servicePathMethodPackets.get(jsonID).put(
										session.getValue().getServices().get(i).getName(),
										new HashMap<Integer, ArrayList<InfoPacket>>());
							}

							if (!servicePathMethodPackets.get(jsonID)
									.get(session.getValue().getServices().get(i).getName())
									.containsKey(session.getValue().getPathMethodID())) {
								servicePathMethodPackets.get(jsonID)
										.get(session.getValue().getServices().get(i).getName())
										.put(session.getValue().getPathMethodID(), new ArrayList<InfoPacket>());
							}

							for (int j = 0; j < session.getValue().getServices().get(i).getPackets().size(); j++) {

								servicePathMethodPackets.get(jsonID)
										.get(session.getValue().getServices().get(i).getName())
										.get(session.getValue().getPathMethodID())
										.add(session.getValue().getServices().get(i).getPackets().get(j));
							}
						}
					}
				}
			}

			for (HashMap.Entry<String, HashMap<Integer, HashMap<Integer, TestSession>>> json : pathMethodTest
					.entrySet()) {

			}


			for (HashMap.Entry<String, HashMap<String, HashMap<Integer, ArrayList<InfoPacket>>>> json : servicePathMethodPackets
					.entrySet()) {

				HashMap<String, Float> coverages3 = new HashMap<String, Float>();
				HashMap<String, Float> coverages2 = new HashMap<String, Float>();
				HashMap<Integer, Float> coverages3Lvl = new HashMap<Integer, Float>();
				HashMap<Integer, Float> coverages2Lvl = new HashMap<Integer, Float>();

				HashMap<Integer, Float> delta3Lvl = new HashMap<Integer, Float>();
				HashMap<Integer, Float> delta2Lvl = new HashMap<Integer, Float>();

				HashMap<Integer, Float> coverages3Lvlnum = new HashMap<Integer, Float>();
				HashMap<Integer, Float> coverages2Lvlnum = new HashMap<Integer, Float>();
				HashMap<Integer, Integer> coverages3Lvlden = new HashMap<Integer, Integer>();
				HashMap<Integer, Integer> coverages2Lvlden = new HashMap<Integer, Integer>();
				HashMap<String, Integer> servLevel = new HashMap<String, Integer>();
				int maxLvl = 0;

				for (HashMap.Entry<String, HashMap<Integer, ArrayList<InfoPacket>>> service : servicePathMethodPackets
						.get(json.getKey()).entrySet()) {

					Integer countPathMethod = service.getValue().size();
					Integer count2xxPathMethod = 0;
					Integer count4xxPathMethod = 0;
					Integer count5xxPathMethod = 0;
					Integer count45xxPathMethod = 0;
					float serv3Coverage = (float) 0;
					float serv2Coverage = (float) 0;

					if (!servLevel.containsKey(service.getKey())) {
						servLevel.put(service.getKey(), 0);
					}

					for (HashMap.Entry<Integer, ArrayList<InfoPacket>> pathMethod : service.getValue().entrySet()) {

						boolean class2xx = false;
						boolean class4xx = false;
						boolean class5xx = false;
						Graph dependencyGraph = new Graph();
						dependencyGraph.buildGraph(pathMethodTest.get(json.getKey()).get(pathMethod.getKey()));

						int serviceLevel = dependencyGraph.getLevel(service.getKey());

						if (serviceLevel > maxLvl)
							maxLvl = serviceLevel;

						if (serviceLevel > servLevel.get(service.getKey())) {
							servLevel.put(service.getKey(), serviceLevel);
						}

						for (int i = 0; i < pathMethod.getValue().size(); i++) {
							if (pathMethod.getValue().get(i).getResponseCode() >= 500) {
								class5xx = true;
							} else if (pathMethod.getValue().get(i).getResponseCode() >= 400) {
								class4xx = true;
							} else if (pathMethod.getValue().get(i).getResponseCode() >= 200) {
								class2xx = true;
							}
						}

						if (class2xx) {
							count2xxPathMethod++;
						}
						if (class4xx) {
							count4xxPathMethod++;
						}
						if (class5xx) {
							count5xxPathMethod++;
						}

						if (class4xx || class5xx) {
							count45xxPathMethod++;
						}
					}


					serv3Coverage = (float) (count2xxPathMethod + count4xxPathMethod + count5xxPathMethod)
							/ (3 * countPathMethod);

					serv2Coverage = (float) (count2xxPathMethod + count45xxPathMethod) / (2 * countPathMethod);

					coverages3.put(service.getKey(), serv3Coverage);
					coverages2.put(service.getKey(), serv2Coverage);
				}
								
				
				String gndtS = readFile(RequestController.sessionPath, "/" + json.getKey() + ".json");
				if (!gndtS.equals("")) {
					int gndTDep = 0;

					Gson gson_2 = new Gson();

					HashMap<String, ArrayList<String>> gndtGraphTemp = gson_2.fromJson(gndtS, HashMap.class);
					
					Graph gtGraph = new Graph(gndtGraphTemp, 0);

					for (HashMap.Entry<String, ArrayList<String>> entry : gndtGraphTemp.entrySet()) {
						
						if(!servLevel.containsKey(entry.getKey())) {
							int serviceLevel = gtGraph.getLevel(entry.getKey());
							
							if (serviceLevel > maxLvl)
								maxLvl = serviceLevel;
							
							servLevel.put(entry.getKey(), serviceLevel);
							coverages3.put(entry.getKey(), (float)0);
							coverages2.put(entry.getKey(), (float)0);				
						}
						
					}

				}
								

				for (int i = 0; i <= maxLvl; i++) {
					float num3 = (float) 0;
					float num2 = (float) 0;
					int den = 0;
					float maxCov3 = (float) 0;
					float minCov3 = (float) 2;
					float maxCov2 = (float) 0;
					float minCov2 = 2;

					for (HashMap.Entry<String, Integer> entry : servLevel.entrySet()) {

						if (entry.getValue() == i) {

							if (coverages3.get(entry.getKey()) > maxCov3)
								maxCov3 = coverages3.get(entry.getKey());
							if (coverages2.get(entry.getKey()) > maxCov2)
								maxCov2 = coverages2.get(entry.getKey());

							if (coverages3.get(entry.getKey()) < minCov3)
								minCov3 = coverages3.get(entry.getKey());
							if (coverages2.get(entry.getKey()) < minCov2)
								minCov2 = coverages2.get(entry.getKey());

							num3 += coverages3.get(entry.getKey());
							num2 += coverages2.get(entry.getKey());
							den++;
						}

					}

					if (den > 0) {
						coverages3Lvl.put(i, (float) num3 / den);
						coverages2Lvl.put(i, (float) num2 / den);

						delta3Lvl.put(i, (float) maxCov3 - minCov3);
						delta2Lvl.put(i, (float) maxCov2 - minCov2);
					}
				}

				delta3JSONLvl.put(json.getKey(), delta3Lvl);
				delta2JSONLvl.put(json.getKey(), delta2Lvl);

				coverages3JSONLvl.put(json.getKey(), coverages3Lvl);
				coverages2JSONLvl.put(json.getKey(), coverages2Lvl);

				coverages3JSON.put(json.getKey(), coverages3);
				coverages2JSON.put(json.getKey(), coverages2);

				toPrint.put("Services levels for json: " + json.getKey(), servLevel);
			}

			HashMap<String, Object> simpleCoverages = new HashMap<String, Object>();
			HashMap<String, Object> levelCoverages = new HashMap<String, Object>();

			simpleCoverages.put("Coverage 3 class", coverages3JSON);
			simpleCoverages.put("Coverage 2 class", coverages2JSON);

			levelCoverages.put("Coverage 3 class", coverages3JSONLvl);
			levelCoverages.put("Coverage 2 class", coverages2JSONLvl);
			levelCoverages.put("Delta 3 class", delta3JSONLvl);
			levelCoverages.put("Delta 2 class", delta2JSONLvl);

			toPrint.put("Level Coverage", levelCoverages);
			toPrint.put("Simple coverage", simpleCoverages);

		} else {
			return "[ERROR] Conf selected not supported: " + conf;
		}

		ret = gson.toJson(toPrint);

		return ret;
	}

	public void closeSession(Integer JsonID, Integer pathMethodID, String url, String method, Integer code,
			Integer failSev) {

		for (int i = 0; i < sessions.get(sessionKey).getServices().size(); i++) {
			for (int j = 0; j < sessions.get(sessionKey).getServices().get(i).getPackets().size(); j++) {
				sessions.get(sessionKey).getServices().get(i).getPackets().get(j).setTestID(sessionKey);
			}
		}

		sessions.get(sessionKey).setJsonID(JsonID);
		sessions.get(sessionKey).setPathMethodID(pathMethodID);
		sessions.get(sessionKey).setMethod(method);
		sessions.get(sessionKey).setUrl(url);
		sessions.get(sessionKey).setFailureSeverity(failSev);
		sessions.get(sessionKey).setCode(code);

	}

	public void initSession(String testID) {
		this.sessions.put(Integer.parseInt(testID), new TestSession());
	}

	public String getStats(String format) {

		String retJson;
		Integer numTest = 0;
		Integer numPackets = 0;
		Integer totSevFailures = 0;

		HashMap<String, HashMap<String, Integer>> sevFailuresXService = new HashMap<String, HashMap<String, Integer>>();
		HashMap<String, HashMap<String, Integer>> packetsXService = new HashMap<String, HashMap<String, Integer>>();

		HashMap<String, HashMap<Integer, Integer>> sevFailuresXLevel = new HashMap<String, HashMap<Integer, Integer>>();
		HashMap<String, HashMap<Integer, Integer>> packetsXLevel = new HashMap<String, HashMap<Integer, Integer>>();

		HashMap<String, HashMap<String, Integer>> servLevel = new HashMap<String, HashMap<String, Integer>>();
		HashMap<String, Integer> maxLvl = new HashMap<String, Integer>();

		HashMap<String, HashMap<Integer, Float>> fRateXLevel = new HashMap<String, HashMap<Integer, Float>>();
		
		HashMap<String, HashMap<String, Integer>> failureDetailsXJson = new HashMap<String, HashMap<String, Integer>>();


		HashMap<String, Object> toPrint = new HashMap<String, Object>();

		for (HashMap.Entry<Integer, TestSession> session : sessions.entrySet()) {
			Boolean internalFailure = false;
			Boolean edgeFailure = false;

			numTest++;
			if (session.getKey() != -1) {
				if (session.getValue().getJsonID() != null) {

					String jsonID = session.getValue().getJsonID().toString();

					if (!sevFailuresXService.containsKey(jsonID)) {
						sevFailuresXService.put(jsonID, new HashMap<String, Integer>());
						packetsXService.put(jsonID, new HashMap<String, Integer>());
						servLevel.put(jsonID, new HashMap<String, Integer>());
						maxLvl.put(jsonID, 0);
						failureDetailsXJson.put(jsonID, new HashMap<String, Integer>());
						failureDetailsXJson.get(jsonID).put("Masked", 0);
						failureDetailsXJson.get(jsonID).put("Propagated", 0);
					}


					for (int i = 0; i < session.getValue().getServices().size(); i++) {

						Graph dependencyGraph = new Graph();
						HashMap<Integer, TestSession> tempHash = new HashMap<Integer, TestSession>();

						tempHash.put(session.getKey(), session.getValue());
						dependencyGraph.buildGraph(tempHash);

						if (!servLevel.get(jsonID).containsKey(session.getValue().getServices().get(i).getName())) {
							servLevel.get(jsonID).put(session.getValue().getServices().get(i).getName(), 0);
						}


						int serviceLevel = dependencyGraph.getLevel(session.getValue().getServices().get(i).getName());


						if (serviceLevel > maxLvl.get(jsonID))
							maxLvl.put(jsonID, serviceLevel);

						if (serviceLevel > servLevel.get(jsonID)
								.get(session.getValue().getServices().get(i).getName())) {
							servLevel.get(jsonID).put(session.getValue().getServices().get(i).getName(), serviceLevel);
						}

						if (!sevFailuresXService.get(jsonID)
								.containsKey(session.getValue().getServices().get(i).getName())) {
							sevFailuresXService.get(jsonID).put(session.getValue().getServices().get(i).getName(), 0);
							packetsXService.get(jsonID).put(session.getValue().getServices().get(i).getName(), 0);
						}


						for (int j = 0; j < session.getValue().getServices().get(i).getPackets().size(); j++) {
							numPackets++;

							packetsXService.get(jsonID).put(session.getValue().getServices().get(i).getName(),
									packetsXService.get(jsonID).get(session.getValue().getServices().get(i).getName())
											+ 1);


							if (session.getValue().getServices().get(i).getPackets().get(j).getResponseCode() >= 500) {

								sevFailuresXService.get(jsonID).put(session.getValue().getServices().get(i).getName(),
										sevFailuresXService.get(jsonID)
												.get(session.getValue().getServices().get(i).getName()) + 1);

								totSevFailures++;
								
								if(serviceLevel > 0) {
									internalFailure = true;
								} else if(serviceLevel == 0) {
									edgeFailure = true; 
								}
								

							}

						}
					}
					
					if(edgeFailure && internalFailure) {
						failureDetailsXJson.get(jsonID).put("Propagated", failureDetailsXJson.get(jsonID).get("Propagated") + 1);
					} else if(!edgeFailure && internalFailure) {
						failureDetailsXJson.get(jsonID).put("Masked", failureDetailsXJson.get(jsonID).get("Masked") + 1);
					}

				}
			}
		}

		for (int i = 0; i < maxLvl.size(); i++) {

			int jsonIndex = i + 1;

			for (int j = 0; j < maxLvl.get(String.valueOf(jsonIndex)) + 1; j++) {

				for (HashMap.Entry<String, Integer> entry : servLevel.get(String.valueOf(jsonIndex)).entrySet()) {

					if (entry.getValue() == j) {

						if (!sevFailuresXLevel.containsKey(String.valueOf(jsonIndex))) {
							sevFailuresXLevel.put(String.valueOf(jsonIndex), new HashMap<Integer, Integer>());
							packetsXLevel.put(String.valueOf(jsonIndex), new HashMap<Integer, Integer>());
						}

						if (!sevFailuresXLevel.get(String.valueOf(jsonIndex)).containsKey(j)) {
							sevFailuresXLevel.get(String.valueOf(jsonIndex)).put(j, 0);
							packetsXLevel.get(String.valueOf(jsonIndex)).put(j, 0);
						}

						sevFailuresXLevel.get(String.valueOf(jsonIndex)).put(j,
								sevFailuresXLevel.get(String.valueOf(jsonIndex)).get(j)
										+ sevFailuresXService.get(String.valueOf(jsonIndex)).get(entry.getKey()));
						packetsXLevel.get(String.valueOf(jsonIndex)).put(j,
								packetsXLevel.get(String.valueOf(jsonIndex)).get(j)
										+ packetsXService.get(String.valueOf(jsonIndex)).get(entry.getKey()));

					}

				}

			}

		}

		for (HashMap.Entry<String, HashMap<Integer, Integer>> json : sevFailuresXLevel.entrySet()) {

			if (!fRateXLevel.containsKey(json.getKey())) {
				fRateXLevel.put(json.getKey(), new HashMap<Integer, Float>());
			}

			for (HashMap.Entry<Integer, Integer> level : sevFailuresXLevel.get(json.getKey()).entrySet()) {
				int num = level.getValue();
				int den = packetsXLevel.get(json.getKey()).get(level.getKey());

				float rate = (float) num / den;

				fRateXLevel.get(json.getKey()).put(level.getKey(), rate);
			}

		}

		HashMap<String, Object> basicStats = new HashMap<String, Object>();

		basicStats.put("TOT Number of tests (edge)", numTest);
		basicStats.put("TOT Number of packets", numPackets);
		basicStats.put("TOT Severe failures (5xx Packets)", totSevFailures);

		toPrint.put("Basic stats", basicStats);
		toPrint.put("Severe failures x service (xJson)", sevFailuresXService);
		toPrint.put("Number of packets x service (xJson)", packetsXService);

		toPrint.put("Severe failures x level (xJson)", sevFailuresXLevel);
		toPrint.put("Number of packets x level (xJson)", packetsXLevel);

		toPrint.put("Failure rate x level (xJson)", fRateXLevel);
		
		toPrint.put("Failures masked and propagated (xJson)", failureDetailsXJson);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		retJson = gson.toJson(toPrint);

		return retJson;
	}

	public String clear() {

		this.sessions = new HashMap<Integer, TestSession>();
		this.recordSession = false;
		this.sessionKey = -1;

		return "[INFO] [InfoCollector] Clear done.\n";
	}

	public String getBugReport(String format) {

		String ret = "";
		HashMap<String, HashMap<String, ArrayList<String>>> servicesMethods = new HashMap<String, HashMap<String, ArrayList<String>>>();
		ArrayList<UService> servicesToPrint = new ArrayList<UService>();

		if (format.equalsIgnoreCase("json")) {

			for (HashMap.Entry<Integer, TestSession> session : sessions.entrySet()) {

				for (int i = 0; i < session.getValue().getServices().size(); i++) {

					for (int j = 0; j < session.getValue().getServices().get(i).getPackets().size(); j++) {

						if (session.getValue().getServices().get(i).getPackets().get(j).getResponseCode() >= 500) {

							boolean toAdd = false;

							if (!servicesMethods.containsKey(session.getValue().getServices().get(i).getName())) {
								servicesMethods.put(session.getValue().getServices().get(i).getName(),
										new HashMap<String, ArrayList<String>>());
								servicesToPrint.add(new UService(session.getValue().getServices().get(i).getName()));
							}

							if (servicesMethods.get(session.getValue().getServices().get(i).getName())
									.containsKey(session.getValue().getUrl())) {
								if (!servicesMethods.get(session.getValue().getServices().get(i).getName())
										.get(session.getValue().getUrl()).contains(session.getValue().getMethod())) {
									servicesMethods.get(session.getValue().getServices().get(i).getName())
											.get(session.getValue().getUrl()).add(session.getValue().getMethod());

									toAdd = true;
								}
							} else {
								ArrayList<String> tempAS = new ArrayList<String>();
								tempAS.add(session.getValue().getMethod());
								servicesMethods.get(session.getValue().getServices().get(i).getName())
										.put(session.getValue().getUrl(), tempAS);
								toAdd = true;
							}

							if (toAdd) {
								int k = 0;
								boolean found = false;
								while (k < servicesToPrint.size() && !found) {
									if (servicesToPrint.get(k).getName()
											.equals(session.getValue().getServices().get(i).getName())) {
										found = true;
									} else {
										k++;
									}
								}
								servicesToPrint.get(k)
										.addPacket(session.getValue().getServices().get(i).getPackets().get(j));
							}
						}

					}
				}
			}

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			ret = gson.toJson(servicesToPrint);
		} else {
			ret = "[ERROR] Format selected not supported: " + format;
		}

		return ret;
	}

	public String getDependencies(String option) {

		String ret = "";
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		if (option.equalsIgnoreCase("all")) {
			Graph dependencyGraph = new Graph();
			dependencyGraph.buildGraph(sessions);

			ret = gson.toJson(dependencyGraph.getOnlyNames());

		} else if (option.equalsIgnoreCase("xmicroservice")) {

			HashMap<String, HashMap<Integer, TestSession>> jsonSessions = new HashMap<String, HashMap<Integer, TestSession>>();
			HashMap<String, Object> jsonDependencies = new HashMap<String, Object>();

			for (HashMap.Entry<Integer, TestSession> session : sessions.entrySet()) {
				if (session.getKey() != -1) {
					if (session.getValue().getJsonID() != null) {
						String jsonID = session.getValue().getJsonID().toString();

						if (!jsonSessions.containsKey(jsonID)) {
							jsonSessions.put(jsonID, new HashMap<Integer, TestSession>());
						}
						jsonSessions.get(jsonID).put(session.getKey(), session.getValue());
					}
				}
			}

			for (HashMap.Entry<String, HashMap<Integer, TestSession>> tempJsonSessions : jsonSessions.entrySet()) {

				Graph tempGraph = new Graph();

				tempGraph.buildGraph(tempJsonSessions.getValue());

				jsonDependencies.put(tempJsonSessions.getKey(), tempGraph.getOnlyNames());
			}

			ret = gson.toJson(jsonDependencies);
		}

		return ret;
	}

	public String getDependenciesCov(String format) {

		HashMap<String, HashMap<Integer, TestSession>> jsonSessions = new HashMap<String, HashMap<Integer, TestSession>>();
		HashMap<String, Float> depCovXJson = new HashMap<String, Float>();

		for (HashMap.Entry<Integer, TestSession> session : sessions.entrySet()) {
			if (session.getKey() != -1) {
				if (session.getValue().getJsonID() != null) {
					String jsonID = session.getValue().getJsonID().toString();

					if (!jsonSessions.containsKey(jsonID)) {
						jsonSessions.put(jsonID, new HashMap<Integer, TestSession>());
					}
					jsonSessions.get(jsonID).put(session.getKey(), session.getValue());
				}
			}
		}

		for (HashMap.Entry<String, HashMap<Integer, TestSession>> tempJsonSessions : jsonSessions.entrySet()) {

			int gndTDep = getGndTDep("/" + tempJsonSessions.getKey() + ".json");

			if (gndTDep > 0) {
				Graph tempGraph = new Graph();

				tempGraph.buildGraph(tempJsonSessions.getValue());

				int tempNumDep = tempGraph.getDependencies();
				float cov = (float) tempNumDep / gndTDep;

				depCovXJson.put(tempJsonSessions.getKey(), cov);
			} else {
				depCovXJson.put(tempJsonSessions.getKey(), (float) 1);
			}
		}

		if (format.equalsIgnoreCase("csv")) {
			String retCSV = "\"Microservice\",\"depCov\"" + "\n";

			for (HashMap.Entry<String, Float> entry : depCovXJson.entrySet()) {
				retCSV += entry.getKey() + "," + entry.getValue() + "\n";
			}

			return retCSV;
		} else {
			return "[ERROR] Format passed not valid";
		}

	}
	
	


	private int getGndTDep(String filename) {

		String gndtS = readFile(RequestController.sessionPath, filename);
		if (!gndtS.equals("")) {
			int gndTDep = 0;

			Gson gson = new Gson();

			HashMap<String, ArrayList<String>> gndtGraph = gson.fromJson(gndtS, HashMap.class);

			for (HashMap.Entry<String, ArrayList<String>> entry : gndtGraph.entrySet()) {
				gndTDep += entry.getValue().size();
			}

			return gndTDep;
		} else {
			return 0;
		}
	}

	private String readFile(String path, String filename) {

		String data = "";

		try {
			File myObj = new File(path + filename);
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
