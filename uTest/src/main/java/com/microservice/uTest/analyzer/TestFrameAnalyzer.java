package com.microservice.uTest.analyzer;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.CYAN;
import static org.fusesource.jansi.Ansi.Color.MAGENTA;
import static org.fusesource.jansi.Ansi.Color.RED;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.microservice.uTest.dataStructure.TestFrame;
import com.microservice.uTest.generator.TestFrameGenerator;

public class TestFrameAnalyzer {

	private static boolean DEBUG_MODE = true;

	public static final int TOTAL_INDEX = 0;
	public static final int FAILURE_INDEX = 1;
	public static final int SEVERE_FAILURE_INDEX = 2;

	public static final ArrayList<String> implementedMethods = new ArrayList<String>(
			Arrays.asList("GET", "PUT", "POST", "DELETE", "HEAD"));
	public static final ArrayList<String> names = new ArrayList<String>(
			Arrays.asList("payload", "date", "dateTime", "int32", "int64", "float", "double", "empty", "range",
					"symbol", "greater", "lower", "bTrue", "bFalse", "sRange", "lang"));
	public static final int FIELDS_NUM = 16;
	public static final int PAYLOAD_IDX = 0;
	public static final int DATE_IDX = 1;
	public static final int DATETIME_IDX = 2;
	public static final int INT32_IDX = 3;
	public static final int INT64_IDX = 4;
	public static final int FLOAT_IDX = 5;
	public static final int DOUBLE_IDX = 6;
	public static final int EMPTY_IDX = 7;
	public static final int RANGE_IDX = 8;
	public static final int SYMBOL_IDX = 9;
	public static final int GREATER_IDX = 10;
	public static final int LOWER_IDX = 11;
	public static final int BTRUE_IDX = 12;
	public static final int BFALSE_IDX = 13;
	public static final int SRANGE_IDX = 14;
	public static final int LANG_IDX = 15;

	public HashMap<String, ArrayList<Integer>> methodCount;
	public ArrayList<ArrayList<Integer>> hasFieldCount;

	public ArrayList<Integer> failedIndexes;
	public ArrayList<Integer> severeIndexes;
	public ArrayList<Integer> successIndexes;
	public int internalErrorCount;

	public ArrayList<Integer> codes;
	public ArrayList<Integer> codeCount;
	public ArrayList<Integer> succCodes;
	public ArrayList<Integer> succCodeCount;

	public ArrayList<Integer> severity;
	public ArrayList<Integer> countSeverity;

	public ArrayList<PathAfterSession> pathArray;

	public ArrayList<Long> respTimes;
	public long min;
	public long max;
	public long avg;
	public int maxIndex;
	public int minIndex;

	public TestFrameAnalyzer(boolean _debug) {
		super();

		DEBUG_MODE = _debug;

		methodCount = new HashMap<String, ArrayList<Integer>>();

		hasFieldCount = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < FIELDS_NUM; i++) {
			hasFieldCount.add(new ArrayList<Integer>(Arrays.asList(0, 0, 0)));
		}

		codes = new ArrayList<Integer>();
		succCodes = new ArrayList<Integer>();
		severity = new ArrayList<Integer>();
		countSeverity = new ArrayList<Integer>();
		codeCount = new ArrayList<Integer>();
		succCodeCount = new ArrayList<Integer>();
		failedIndexes = new ArrayList<Integer>();
		severeIndexes = new ArrayList<Integer>();
		successIndexes = new ArrayList<Integer>();
		pathArray = new ArrayList<PathAfterSession>();
		respTimes = new ArrayList<Long>();
		minIndex = -1;
		maxIndex = -1;
		avg = 0;
		max = 0;
		min = 0;
		internalErrorCount = 0;
	}

	public void analyzeTest(ArrayList<TestFrame> testFrames, ArrayList<Integer> tfs) {
		if (tfs.size() > 0) {
			avg = 0;
			max = 0;
			min = testFrames.get(tfs.get(0)).getResponseTime();

			for (int i = 0; i < tfs.size(); i++) {

				String url = testFrames.get(tfs.get(i)).getUrl();
				String method = testFrames.get(tfs.get(i)).getReqType();
				int code = testFrames.get(tfs.get(i)).getResponseCode();
				int pathID = testFrames.get(tfs.get(i)).getPathID();
				int jsonID = testFrames.get(tfs.get(i)).getJsonID();
				boolean existentPath = false;
				int pathIndex = 0;

				ArrayList<Boolean> hasFieldB = new ArrayList<Boolean>();
				for (int x = 0; x < FIELDS_NUM; x++) {
					hasFieldB.add(false);
				}

				if (!methodCount.containsKey(method)) {
					ArrayList<Integer> counts = new ArrayList<Integer>();
					counts.add(1); // TOTAL_INDEX
					counts.add(0); // FAILURE_INDEX
					counts.add(0); // SEVERE_FAILURE_INDEX
					methodCount.put(method, counts);
				} else {
					ArrayList<Integer> counts = new ArrayList<Integer>(methodCount.get(method));
					counts.set(TOTAL_INDEX, counts.get(TOTAL_INDEX) + 1);
					methodCount.put(method, counts);
				}

				if (testFrames.get(tfs.get(i)).getPayload() != null) {
					hasFieldB.set(PAYLOAD_IDX, true);
				}

				for (int j = 0; j < testFrames.get(tfs.get(i)).ic.size(); j++) {
					String typeTemp = testFrames.get(tfs.get(i)).ic.get(j).type;

					if (typeTemp.equals("date"))
						hasFieldB.set(DATE_IDX, true);
					else if (typeTemp.equals("date-time"))
						hasFieldB.set(DATETIME_IDX, true);
					else if (typeTemp.equals("int32"))
						hasFieldB.set(INT32_IDX, true);
					else if (typeTemp.equals("int64"))
						hasFieldB.set(INT64_IDX, true);
					else if (typeTemp.equals("float"))
						hasFieldB.set(FLOAT_IDX, true);
					else if (typeTemp.equals("double"))
						hasFieldB.set(DOUBLE_IDX, true);
					else if (typeTemp.equals("empty"))
						hasFieldB.set(EMPTY_IDX, true);
					else if (typeTemp.equals("range"))
						hasFieldB.set(RANGE_IDX, true);
					else if (typeTemp.equals("symbol"))
						hasFieldB.set(SYMBOL_IDX, true);
					else if (typeTemp.equals("greater"))
						hasFieldB.set(GREATER_IDX, true);
					else if (typeTemp.equals("lower"))
						hasFieldB.set(LOWER_IDX, true);
					else if (typeTemp.equals("b_true"))
						hasFieldB.set(BTRUE_IDX, true);
					else if (typeTemp.equals("b_false"))
						hasFieldB.set(BFALSE_IDX, true);
					else if (typeTemp.equals("s_range"))
						hasFieldB.set(SRANGE_IDX, true);
					else if (typeTemp.equals("lang"))
						hasFieldB.set(LANG_IDX, true);
				}

				while (pathIndex < pathArray.size() && !existentPath) {
					if (pathArray.get(pathIndex).getPathID() == pathID) {
						existentPath = true;
					} else {
						pathIndex++;
					}
				}

				if (!existentPath) {
					PathAfterSession newP = new PathAfterSession(jsonID, pathID, url);
					pathArray.add(newP);
					pathIndex = pathArray.size() - 1;
				}

				if (code > 0) {
					pathArray.get(pathIndex).addReturnCodeToMethod(method, code);
				}

				if (testFrames.get(tfs.get(i)).getState().equals("failed")) {

					int severityT = testFrames.get(tfs.get(i)).getFailureSeverity();

					if (codes.contains(code)) {
						int index = codes.indexOf(code);
						codeCount.set(index, codeCount.get(index) + 1);
					} else {
						codes.add(code);
						codeCount.add(1);
					}

					if (severity.contains(severityT)) {
						int index = severity.indexOf(severityT);
						countSeverity.set(index, countSeverity.get(index) + 1);
					} else {
						severity.add(severityT);
						countSeverity.add(1);
					}

					if (testFrames.get(tfs.get(i)).getResponseCode() >= 0) {
						ArrayList<Integer> counts = new ArrayList<Integer>(methodCount.get(method));
						counts.set(FAILURE_INDEX, counts.get(FAILURE_INDEX) + 1);
						if (testFrames.get(tfs.get(i)).getFailureSeverity() == 2) {
							counts.set(SEVERE_FAILURE_INDEX, counts.get(SEVERE_FAILURE_INDEX) + 1);
						}
						methodCount.put(method, counts);
					}

					for (int j = 0; j < FIELDS_NUM; j++) {
						if (hasFieldB.get(j)) {
							hasFieldCount.get(j).set(FAILURE_INDEX, hasFieldCount.get(j).get(FAILURE_INDEX) + 1);
							if (testFrames.get(tfs.get(i)).getFailureSeverity() == 2) {
								hasFieldCount.get(j).set(SEVERE_FAILURE_INDEX,
										hasFieldCount.get(j).get(SEVERE_FAILURE_INDEX) + 1);
							}
						}
					}

					if (code >= 0) {
						failedIndexes.add(tfs.get(i));

						if (code >= 500) {

							severeIndexes.add(tfs.get(i));
							pathArray.get(pathIndex).setSevereFailedMethodStatusSpec(method);
						} else {
							pathArray.get(pathIndex).setFailedMethodStatusSpec(method);
						}

					} else {
						internalErrorCount++;
					}

				} else {

					if (succCodes.contains(code)) {
						int index = succCodes.indexOf(code);
						succCodeCount.set(index, succCodeCount.get(index) + 1);
					} else {
						succCodes.add(code);
						succCodeCount.add(1);
					}

					successIndexes.add(tfs.get(i));
				}

				for (int j = 0; j < FIELDS_NUM; j++) {
					if (hasFieldB.get(j))
						hasFieldCount.get(j).set(TOTAL_INDEX, hasFieldCount.get(j).get(TOTAL_INDEX) + 1);
				}

				respTimes.add(testFrames.get(tfs.get(i)).getResponseTime());

				avg += respTimes.get(i);
				if (respTimes.get(i) > max) {
					max = respTimes.get(i);
					maxIndex = tfs.get(i);
				}

				if (respTimes.get(i) < min && testFrames.get(tfs.get(i)).getResponseCode() > 0) {
					min = respTimes.get(i);
					minIndex = tfs.get(i);
				}
			}

			avg = avg / respTimes.size();
		}

	}

	public void printTest(ArrayList<TestFrame> testFrames, ArrayList<Integer> indexes) {
		for (int i = 0; i < indexes.size(); i++) {
			System.out.println("\n[Analyzer] ----------------------------------------\nurl: "
					+ testFrames.get(indexes.get(i)).getSelUrl());
			System.out.println("[Analyzer] Method: " + testFrames.get(indexes.get(i)).getReqType());
			if (testFrames.get(indexes.get(i)).getSelPayload() != "null")
				System.out.println("[Analyzer] payload: " + testFrames.get(indexes.get(i)).getSelPayload());
			System.out.print("[Analyzer] Expected Responses: ");
			testFrames.get(indexes.get(i)).printExpectedResponses();
			System.out.println("\n[Analyzer] Returned Response: " + testFrames.get(indexes.get(i)).getResponseCode());
			System.out.println("[Analyzer] Response Time: " + testFrames.get(indexes.get(i)).getResponseTime() + " ms");
			System.out.println("[Analyzer] Priority: " + testFrames.get(i).getPriority());
		}
	}

	public void printTest(ArrayList<TestFrame> testFrames, ArrayList<Integer> indexes, String filename) {

		try {
			PrintWriter myWriter = new PrintWriter(new BufferedWriter(new FileWriter(filename)));

			for (int i = 0; i < indexes.size(); i++) {
				myWriter.println("\n----------------------------------------\nurl: "
						+ testFrames.get(indexes.get(i)).getSelUrl());
				myWriter.println("Method: " + testFrames.get(indexes.get(i)).getReqType());
				if (testFrames.get(indexes.get(i)).getSelPayload() != "null")
					myWriter.write("Payload: " + testFrames.get(indexes.get(i)).getSelPayload());
				myWriter.println("Expected Responses: ");
				testFrames.get(indexes.get(i)).printExpectedResponses(myWriter);
				myWriter.println("\nReturned Response: " + testFrames.get(indexes.get(i)).getResponseCode());
				myWriter.println("Response Time: " + testFrames.get(indexes.get(i)).getResponseTime() + " ms");
				myWriter.println("Priority: " + testFrames.get(i).getPriority());
			}

			myWriter.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void printMinMax(TestFrameGenerator tfg) {
		if (maxIndex != -1 && minIndex != -1) {
			ArrayList<Integer> indexes = new ArrayList<Integer>();
			indexes.add(maxIndex);
			indexes.add(minIndex);

			for (int i = 0; i < 2; i++) {
				System.out.println("\n[Analyzer] ----------------------------------------\nurl: "
						+ tfg.testFrames.get(indexes.get(i)).getSelUrl());
				System.out.println("[Analyzer] Method: " + tfg.testFrames.get(indexes.get(i)).getReqType());
				if (tfg.testFrames.get(indexes.get(i)).getSelPayload() != "null")
					System.out.println("payload: " + tfg.testFrames.get(indexes.get(i)).getSelPayload());
				System.out.print("[Analyzer] Expected Responses: ");
				tfg.testFrames.get(indexes.get(i)).printExpectedResponses();
				System.out.println(
						"\n[Analyzer] Returned Response: " + tfg.testFrames.get(indexes.get(i)).getResponseCode());
				System.out.println(
						"[Analyzer] Response Time: " + tfg.testFrames.get(indexes.get(i)).getResponseTime() + " ms");
			}
		} else {
			System.out.println("[" + ansi().fgBright(RED).a("ERROR").reset() + "] Failed to print min max..");
		}
	}

	public void printStatistics(TestFrameGenerator tfg, ArrayList<Integer> tfs) {
		int execTest = tfs.size() - internalErrorCount;

		System.out.println("[Analyzer] Executed test: " + execTest);
		System.out.println("[Analyzer] Success: " + successIndexes.size());
		System.out.println(
				"[Analyzer] Test Failures: " + failedIndexes.size() + " (Severe: " + severeIndexes.size() + ")");
		System.out.println(
				"[Analyzer] Method failures: " + getFailedMethods() + " (Severe: " + getSevereFailedMethods() + ")");
		System.out.println("[Analyzer] Coverage (3 class): " + getCoverage());
		System.out.println("[Analyzer] Coverage (2 class): " + getCoverageTwoClass());

		// CODICI E count
		System.out.println("\n----- FCode ----- # -----");
		boolean triggerOss = false;
		for (int i = 0; i < codes.size(); i++) {
			if (codes.get(i) < 100) {
				if (codes.get(i) == -1 || codes.get(i) == -2) {
					System.out.print("-      " + codes.get(i) + "*");
					triggerOss = true;
				} else {
					System.out.print("-       " + codes.get(i) + " ");
				}
			} else {
				System.out.print("-      " + codes.get(i));
			}

			if (codeCount.get(i) < 100) {
				System.out.print("       " + codeCount.get(i) + "\n");
			} else {
				System.out.print("      " + codeCount.get(i) + "\n");
			}
		}

		System.out.println("\n----- SCode ----- # -----");
		for (int i = 0; i < succCodes.size(); i++) {
			if (succCodes.get(i) < 100) {
				if (succCodes.get(i) == -1 || succCodes.get(i) == -2) {
					System.out.print("-      " + succCodes.get(i) + "*");
					triggerOss = true;
				} else {
					System.out.print("-       " + succCodes.get(i) + " ");
				}
			} else {
				System.out.print("-      " + succCodes.get(i));
			}

			if (succCodeCount.get(i) < 100) {
				System.out.print("       " + succCodeCount.get(i) + "\n");
			} else {
				System.out.print("      " + succCodeCount.get(i) + "\n");
			}
		}

		if (triggerOss)
			System.out.println("\n*code -1 = SocketTimeoutException \n*code -2 = HTTP method not yet implemented");

		System.out.println("\n----- Method ----- # -----");
		for (Map.Entry<String, ArrayList<Integer>> method : methodCount.entrySet()) {

			if (method.getValue().get(FAILURE_INDEX) > 0) {

				System.out.print("-      " + method.getKey());

				if (method.getKey().length() < 3) {
					if (method.getValue().get(FAILURE_INDEX) < 100) {
						System.out.print("         " + method.getValue().get(FAILURE_INDEX) + "\n");
					} else {
						System.out.print("       " + method.getValue().get(FAILURE_INDEX) + "\n");
					}
				} else if (method.getKey().length() == 3) {
					if (method.getValue().get(FAILURE_INDEX) < 100) {
						System.out.print("        " + method.getValue().get(FAILURE_INDEX) + "\n");
					} else {
						System.out.print("       " + method.getValue().get(FAILURE_INDEX) + "\n");
					}
				} else if (method.getKey().length() == 4) {
					if (method.getValue().get(FAILURE_INDEX) < 100) {
						System.out.print("       " + method.getValue().get(FAILURE_INDEX) + "\n");
					} else {
						System.out.print("      " + method.getValue().get(FAILURE_INDEX) + "\n");
					}
				} else if (method.getKey().length() == 5) {
					if (method.getValue().get(FAILURE_INDEX) < 100) {
						System.out.print("      " + method.getValue().get(FAILURE_INDEX) + "\n");
					} else {
						System.out.print("     " + method.getValue().get(FAILURE_INDEX) + "\n");
					}
				} else if (method.getKey().length() == 6) {
					if (method.getValue().get(FAILURE_INDEX) < 100) {
						System.out.print("     " + method.getValue().get(FAILURE_INDEX) + "\n");
					} else {
						System.out.print("    " + method.getValue().get(FAILURE_INDEX) + "\n");
					}
				} else {
					if (method.getValue().get(FAILURE_INDEX) < 100) {
						System.out.print("    " + method.getValue().get(FAILURE_INDEX) + "\n");
					} else {
						System.out.print("   " + method.getValue().get(FAILURE_INDEX) + "\n");
					}
				}
			}
		}

		System.out.println("\n\n----- Severity ----- # -----");
		for (int i = 0; i < severity.size(); i++) {

			System.out.print("-        " + severity.get(i));

			if (countSeverity.get(i) < 100) {
				System.out.print("           " + countSeverity.get(i) + "\n");
			} else {
				System.out.print("          " + countSeverity.get(i) + "\n");
			}
		}

		System.out.println("\n----- Response times (success and failures)");
		System.out.println("- max        " + max + " ms");
		System.out.println("- min        " + min + " ms");
		System.out.println("- avg        " + avg + " ms");

	}

	public void printStatistics(TestFrameGenerator tfg, ArrayList<Integer> tfs, PrintWriter statFile) {
		int execTest = tfs.size() - internalErrorCount;

		statFile.println("Executed test: " + execTest);
		statFile.println("Success: " + successIndexes.size());
		statFile.println("Test Failures: " + failedIndexes.size() + " (Severe: " + severeIndexes.size() + ")");
		statFile.println("Method failures: " + getFailedMethods() + " (Severe: " + getSevereFailedMethods() + ")");
		statFile.println("Coverage (3 class): " + getCoverage());
		statFile.println("Coverage (2 class): " + getCoverageTwoClass());

		// CODICI E count
		statFile.println("\n----- FailCode ----- # -----");
		boolean triggerOss = false;
		for (int i = 0; i < codes.size(); i++) {
			if (codes.get(i) < 100) {
				if (codes.get(i) == -1 || codes.get(i) == -2) {
					statFile.write("-      " + codes.get(i) + "*");
					triggerOss = true;
				} else {
					statFile.write("-       " + codes.get(i) + " ");
				}
			} else {
				statFile.write("-      " + codes.get(i));
			}

			if (codeCount.get(i) < 100) {
				statFile.write("       " + codeCount.get(i) + "\n");
			} else {
				statFile.write("      " + codeCount.get(i) + "\n");
			}
		}

		statFile.println("\n----- SuccCode ----- # -----");
		for (int i = 0; i < succCodes.size(); i++) {
			if (succCodes.get(i) < 100) {
				if (succCodes.get(i) == -1 || succCodes.get(i) == -2) {
					statFile.print("-      " + succCodes.get(i) + "*");
					triggerOss = true;
				} else {
					statFile.print("-       " + succCodes.get(i) + " ");
				}
			} else {
				statFile.print("-      " + succCodes.get(i));
			}

			if (succCodeCount.get(i) < 100) {
				statFile.print("       " + succCodeCount.get(i) + "\n");
			} else {
				statFile.print("      " + succCodeCount.get(i) + "\n");
			}
		}

		if (triggerOss)
			statFile.println("\n*code -1 = SocketTimeoutException \n*code -2 = HTTP method not yet implemented");

		statFile.println("\n----- Method ----- # -----");
		for (Map.Entry<String, ArrayList<Integer>> method : methodCount.entrySet()) {

			if (method.getValue().get(FAILURE_INDEX) > 0) {

				statFile.write("-      " + method.getKey());

				if (method.getKey().length() < 3) {
					if (method.getValue().get(FAILURE_INDEX) < 100) {
						statFile.write("         " + method.getValue().get(FAILURE_INDEX) + "\n");
					} else {
						statFile.write("       " + method.getValue().get(FAILURE_INDEX) + "\n");
					}
				} else if (method.getKey().length() == 3) {
					if (method.getValue().get(FAILURE_INDEX) < 100) {
						statFile.write("        " + method.getValue().get(FAILURE_INDEX) + "\n");
					} else {
						statFile.write("       " + method.getValue().get(FAILURE_INDEX) + "\n");
					}
				} else if (method.getKey().length() == 4) {
					if (method.getValue().get(FAILURE_INDEX) < 100) {
						statFile.write("       " + method.getValue().get(FAILURE_INDEX) + "\n");
					} else {
						statFile.write("      " + method.getValue().get(FAILURE_INDEX) + "\n");
					}
				} else if (method.getKey().length() == 5) {
					if (method.getValue().get(FAILURE_INDEX) < 100) {
						statFile.write("      " + method.getValue().get(FAILURE_INDEX) + "\n");
					} else {
						statFile.write("     " + method.getValue().get(FAILURE_INDEX) + "\n");
					}
				} else if (method.getKey().length() == 6) {
					if (method.getValue().get(FAILURE_INDEX) < 100) {
						statFile.write("     " + method.getValue().get(FAILURE_INDEX) + "\n");
					} else {
						statFile.write("    " + method.getValue().get(FAILURE_INDEX) + "\n");
					}
				} else {
					if (method.getValue().get(FAILURE_INDEX) < 100) {
						statFile.write("    " + method.getValue().get(FAILURE_INDEX) + "\n");
					} else {
						statFile.write("   " + method.getValue().get(FAILURE_INDEX) + "\n");
					}
				}
			}
		}

		statFile.println("\n\n----- Severity ----- # -----");
		for (int i = 0; i < severity.size(); i++) {

			statFile.write("-        " + severity.get(i));

			if (countSeverity.get(i) < 100) {
				statFile.write("           " + countSeverity.get(i) + "\n");
			} else {
				statFile.write("          " + countSeverity.get(i) + "\n");
			}
		}

		statFile.println("\n----- Response times (success and failures)");
		statFile.println("- max        " + max + " ms");
		statFile.println("- min        " + min + " ms");
		statFile.println("- avg        " + avg + " ms");

	}

	public void printTestToFile(TestFrameGenerator tfg, ArrayList<Integer> tfs, String filename) {

		try {
			FileWriter myWriter = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(myWriter);

			out.write(
					"state,code,severity,method,ic,hasPayload,hasDate,hasDateTime,hasInt32,hasInt64,hasFloat,hasDouble,hasEmpty,hasRange,hasSymbol,hasGreater,hasLower,hasBTrue,hasBFalse,hasSRange,hasLang");
			out.newLine();

			for (int i = 0; i < tfs.size(); i++) {
				boolean hasDate = false, hasDateTime = false, hasInt32 = false, hasInt64 = false, hasFloat = false,
						hasDouble = false, hasEmpty = false, hasRange = false, hasSymbol = false, hasGreater = false,
						hasLower = false, hasBTrue = false, hasBFalse = false, hasSRange = false, hasLang = false;
				out.write(tfg.testFrames.get(tfs.get(i)).getState() + ","
						+ tfg.testFrames.get(tfs.get(i)).getResponseCode() + ","
						+ tfg.testFrames.get(tfs.get(i)).getFailureSeverity() + ","
						+ tfg.testFrames.get(tfs.get(i)).getReqType() + "," + tfg.testFrames.get(tfs.get(i)).ic.size()
						+ ",");
				if (tfg.testFrames.get(tfs.get(i)).getPayload() != null) {
					out.write("y,");
				}

				for (int j = 0; j < tfg.testFrames.get(tfs.get(i)).ic.size(); j++) {
					String typeTemp = tfg.testFrames.get(tfs.get(i)).ic.get(j).type;

					if (typeTemp.equals("date"))
						hasDate = true;
					else if (typeTemp.equals("date-time"))
						hasDateTime = true;
					else if (typeTemp.equals("int32"))
						hasInt32 = true;
					else if (typeTemp.equals("int64"))
						hasInt64 = true;
					else if (typeTemp.equals("float"))
						hasFloat = true;
					else if (typeTemp.equals("double"))
						hasDouble = true;
					else if (typeTemp.equals("empty"))
						hasEmpty = true;
					else if (typeTemp.equals("range"))
						hasRange = true;
					else if (typeTemp.equals("symbol"))
						hasSymbol = true;
					else if (typeTemp.equals("greater"))
						hasGreater = true;
					else if (typeTemp.equals("lower"))
						hasLower = true;
					else if (typeTemp.equals("b_true"))
						hasBTrue = true;
					else if (typeTemp.equals("b_false"))
						hasBFalse = true;
					else if (typeTemp.equals("s_range"))
						hasSRange = true;
					else if (typeTemp.equals("lang"))
						hasLang = true;
				}

				if (hasDate) {
					out.write("y,");
				} else {
					out.write("n,");
				}
				if (hasDateTime) {
					out.write("y,");
				} else {
					out.write("n,");
				}
				if (hasInt32) {
					out.write("y,");
				} else {
					out.write("n,");
				}
				if (hasInt64) {
					out.write("y,");
				} else {
					out.write("n,");
				}
				if (hasFloat) {
					out.write("y,");
				} else {
					out.write("n,");
				}
				if (hasDouble) {
					out.write("y,");
				} else {
					out.write("n,");
				}
				if (hasEmpty) {
					out.write("y,");
				} else {
					out.write("n,");
				}
				if (hasRange) {
					out.write("y,");
				} else {
					out.write("n,");
				}
				if (hasSymbol) {
					out.write("y,");
				} else {
					out.write("n,");
				}
				if (hasGreater) {
					out.write("y,");
				} else {
					out.write("n,");
				}
				if (hasLower) {
					out.write("y,");
				} else {
					out.write("n,");
				}
				if (hasBTrue) {
					out.write("y,");
				} else {
					out.write("n,");
				}
				if (hasBFalse) {
					out.write("y,");
				} else {
					out.write("n,");
				}
				if (hasSRange) {
					out.write("y,");
				} else {
					out.write("n,");
				}
				if (hasLang) {
					out.write("y");
				} else {
					out.write("n");
				}

				out.newLine();
			}

			out.close();
			myWriter.close();
		} catch (Exception e) {
			System.out.println("[" + ansi().fgBright(RED).a("ERROR").reset() + "] File writing error..");

		}
	}

	public void printStatisticsByJson(ArrayList<TestFrame> testFrames, ArrayList<Integer> tfs) {
		int jsonIndex = 1;
		boolean finished = false;

		int failCount = 0;
		int failSevereCount = 0;
		int methodFailCount = 0;
		int methodFailSevereCount = 0;
		int testCount = 0;
		float coverage = 0;
		DecimalFormat df = new DecimalFormat("0.00");

		while (!finished) {
			for (int i = 0; i < tfs.size(); i++) {

				if (jsonIndex == testFrames.get(tfs.get(i)).getJsonID()) {
					if (testFrames.get(tfs.get(i)).getResponseCode() > 0) {
						testCount++;
						if (testFrames.get(tfs.get(i)).getState().equals("failed")) {
							failCount++;
							if (testFrames.get(tfs.get(i)).getFailureSeverity() == 2)
								failSevereCount++;
						}
					}
				}
			}

			if (testCount == 0) {
				finished = true;
			} else {

				methodFailCount = getFailedMethods(jsonIndex);
				methodFailSevereCount = getSevereFailedMethods(jsonIndex);
				coverage = getCoverage(jsonIndex);

				System.out.println("\n- Json # " + jsonIndex);
				System.out.println("	- # Test:			" + testCount);
				if (failCount > 0) {
					System.out
							.println("	- # Failure (Severe):		" + ansi().fgBright(MAGENTA).a(failCount).reset()
									+ " (" + ansi().fgBright(MAGENTA).a(failSevereCount).reset() + ")");
				} else {
					System.out.println("	- # Failure (Severe):		" + failCount + " (" + failSevereCount + ")");
				}

				if (methodFailCount > 0) {
					System.out.println(
							"	- # MethodFailure (Severe):	" + ansi().fgBright(MAGENTA).a(methodFailCount).reset()
									+ " (" + ansi().fgBright(MAGENTA).a(methodFailSevereCount).reset() + ")");
				} else {
					System.out.println(
							"	- # MethodFailure (Severe):	" + methodFailCount + " (" + methodFailSevereCount + ")");
				}
				System.out.println("	- Coverage:			" + df.format(coverage));

				jsonIndex++;

				failCount = 0;
				failSevereCount = 0;
				testCount = 0;
			}

		}
	}

	public void printStatisticsByJsonToFile(ArrayList<TestFrame> testFrames, ArrayList<Integer> tfs, String filename) {
		int jsonIndex = 1;
		boolean finished = false;

		int failCount = 0;
		int failSevereCount = 0;
		int methodFailCount = 0;
		int methodFailSevereCount = 0;
		int testCount = 0;
		float coverage = 0;
		DecimalFormat df = new DecimalFormat("0.00");

		try {
			FileWriter myWriter = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(myWriter);

			out.write("Executed	TFailed	TSFailed	MFailed	MSFailed	Coverage");
			out.newLine();

			while (!finished) {
				for (int i = 0; i < tfs.size(); i++) {

					if (jsonIndex == testFrames.get(tfs.get(i)).getJsonID()) {
						if (testFrames.get(tfs.get(i)).getResponseCode() > 0) {
							testCount++;
							if (testFrames.get(tfs.get(i)).getState().equals("failed")) {
								failCount++;
								if (testFrames.get(tfs.get(i)).getFailureSeverity() == 2)
									failSevereCount++;
							}
						}
					}
				}

				if (testCount == 0) {
					finished = true;
				} else {

					methodFailCount = getFailedMethods(jsonIndex);
					methodFailSevereCount = getSevereFailedMethods(jsonIndex);
					coverage = getCoverage(jsonIndex);

					out.write(testCount + "	" + failCount + "	" + failSevereCount + "	" + methodFailCount + "	"
							+ methodFailSevereCount + "	" + df.format(coverage));
					out.newLine();

					jsonIndex++;

					failCount = 0;
					failSevereCount = 0;
					testCount = 0;
				}

			}

			out.close();
			myWriter.close();

		} catch (Exception e) {
			System.out.println(
					"[" + ansi().fgBright(RED).a("ERROR").reset() + "] Failed to print statistcs by json to file..");
		}
	}
	
	public void printReliabilities(ArrayList<TestFrame> testFrames, ArrayList<Integer> tfs, String filename) {
		int jsonIndex = 1;
		boolean finished = false;

		int testCount = 0;
		int failSevereCount = 0;
		int methodFailSevereCount = 0;
		int methodCount = 0;
		float testRel = 0;
		float metRel = 0;

		try {
			FileWriter myWriter = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(myWriter);

			out.write("JsonID,Reliability,ReliabilityXM");
			out.newLine();

			while (!finished) {
				
				for (int i = 0; i < tfs.size(); i++) {

					if (jsonIndex == testFrames.get(tfs.get(i)).getJsonID()) {
						testCount++;
						if (testFrames.get(tfs.get(i)).getResponseCode() > 0) {
							if (testFrames.get(tfs.get(i)).getState().equals("failed")) {
								if (testFrames.get(tfs.get(i)).getFailureSeverity() == 2)
									failSevereCount++;
							}
						}
					}
				}

				if (testCount == 0) {
					finished = true;
				} else {

					methodFailSevereCount = getSevereFailedMethods(jsonIndex);
					methodCount = getMethodCount(jsonIndex);
					metRel = 1 - (float) methodFailSevereCount/methodCount;
					
					testRel = 1 - (float) failSevereCount/testCount;

					out.write(jsonIndex + "," + testRel + "," + metRel);
					out.newLine();

					jsonIndex++;

					failSevereCount = 0;
					testCount = 0;
				}

			}

			out.close();
			myWriter.close();

		} catch (Exception e) {
			System.out.println(
					"[" + ansi().fgBright(RED).a("ERROR").reset() + "] Failed to print statistcs by json to file..");
		}

	}

	public int getFailedMethods() {
		int fm = 0;

		for (int i = 0; i < pathArray.size(); i++) {
			fm += pathArray.get(i).getFailedMethodsCountSpec();
		}

		return fm;
	}

	public int getSevereFailedMethods() {
		int sfm = 0;

		for (int i = 0; i < pathArray.size(); i++) {
			sfm += pathArray.get(i).getSevereFailedMethodsCountSpec();
		}

		return sfm;
	}

	public int getFailedMethods(int _jsonID) {
		int fm = 0;

		for (int i = 0; i < pathArray.size(); i++) {
			if (pathArray.get(i).getJsonID() == _jsonID)
				fm += pathArray.get(i).getFailedMethodsCountSpec();
		}

		return fm;
	}

	public int getSevereFailedMethods(int _jsonID) {
		int sfm = 0;

		for (int i = 0; i < pathArray.size(); i++) {
			if (pathArray.get(i).getJsonID() == _jsonID)
				sfm += pathArray.get(i).getSevereFailedMethodsCountSpec();
		}

		return sfm;
	}
	
	public int getMethodCount(int _jsonID) {
		int count = 0;

		for (int i = 0; i < pathArray.size(); i++) {
			if (pathArray.get(i).getJsonID() == _jsonID)
				count += pathArray.get(i).methods.size();
		}

		return count;
	}

	public float getCoverage() {
		float cov = 0;
		int methodNum = getTotalMethodsCount();
		int successCount = 0;
		int clienErrorCount = 0;
		int serverErrorCount = 0;

		for (int i = 0; i < pathArray.size(); i++) {
			for (int j = 0; j < pathArray.get(i).methods.size(); j++) {
				if (implementedMethods.contains(pathArray.get(i).methods.get(j).getMethod())) {

					if (pathArray.get(i).methods.get(j).verifySuccessClass())
						successCount++;

					if (pathArray.get(i).methods.get(j).verifyClientErrorClass())
						clienErrorCount++;

					if (pathArray.get(i).methods.get(j).verifyServerErrorClass())
						serverErrorCount++;
				}
			}
		}

		if (DEBUG_MODE) {
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [Analyzer] Calculating coverage...");
			System.out.println(
					"[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [Analyzer] Total methods count: " + methodNum);
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
					+ "] [Analyzer] 2xx count (1x method max): " + successCount);
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
					+ "] [Analyzer] 4xx count (1x method max): " + clienErrorCount);
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
					+ "][Analyzer] 5xx count (1x method max): " + serverErrorCount);
		}

		cov = ((float) 1 / 3) * ((float) successCount / methodNum)
				+ ((float) 1 / 3) * ((float) clienErrorCount / methodNum)
				+ ((float) 1 / 3) * ((float) serverErrorCount / methodNum);

		return cov;
	}

	public float getCoverageTwoClass() {
		float cov = 0;
		int methodNum = getTotalMethodsCount();
		int successCount = 0;
		int errorCount = 0;

		for (int i = 0; i < pathArray.size(); i++) {
			for (int j = 0; j < pathArray.get(i).methods.size(); j++) {
				if (implementedMethods.contains(pathArray.get(i).methods.get(j).getMethod())) {

					if (pathArray.get(i).methods.get(j).verifySuccessClass())
						successCount++;

					if (pathArray.get(i).methods.get(j).verifyClientErrorClass()
							|| pathArray.get(i).methods.get(j).verifyServerErrorClass())
						errorCount++;
				}
			}
		}

		if (DEBUG_MODE) {
			System.out.println(
					"[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [Analyzer] Calculating coverage two class...");
			System.out.println(
					"[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [Analyzer] Total methods count: " + methodNum);
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
					+ "] [Analyzer] 2xx count (1x method max): " + successCount);
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
					+ "] [Analyzer] 4xx-5xx count (1x method max): " + errorCount);

		}

		cov = ((float) 1 / 2) * ((float) successCount / methodNum) + ((float) 1 / 2) * ((float) errorCount / methodNum);

		return cov;
	}

	public float getCoverage(int _jsonID) {
		float cov = 0;
		int methodNum = getTotalMethodsCount(_jsonID);
		int successCount = 0;
		int clienErrorCount = 0;
		int serverErrorCount = 0;

		for (int i = 0; i < pathArray.size(); i++) {
			if (pathArray.get(i).getJsonID() == _jsonID) {
				for (int j = 0; j < pathArray.get(i).methods.size(); j++) {
					if (implementedMethods.contains(pathArray.get(i).methods.get(j).getMethod())) {

						if (pathArray.get(i).methods.get(j).verifySuccessClass())
							successCount++;

						if (pathArray.get(i).methods.get(j).verifyClientErrorClass())
							clienErrorCount++;

						if (pathArray.get(i).methods.get(j).verifyServerErrorClass())
							serverErrorCount++;
					}
				}
			}
		}

		if (DEBUG_MODE) {
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [Analyzer] Calculating coverage...");
			System.out.println(
					"[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] [Analyzer] Total methods count: " + methodNum);
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
					+ "] [Analyzer]	2xx count (1x method max): " + successCount);
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
					+ "] [Analyzer]	4xx count (1x method max): " + clienErrorCount);
			System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
					+ "] [Analyzer]	5xx count (1x method max): " + serverErrorCount);
		}

		cov = ((float) 1 / 3) * ((float) successCount / methodNum)
				+ ((float) 1 / 3) * ((float) clienErrorCount / methodNum)
				+ ((float) 1 / 3) * ((float) serverErrorCount / methodNum);

		return cov;
	}

	public int getTotalMethodsCount() {
		int count = 0;

		for (int i = 0; i < pathArray.size(); i++) {
			int implMethodsCount = 0;

			for (int j = 0; j < pathArray.get(i).methods.size(); j++) {
				if (implementedMethods.contains(pathArray.get(i).methods.get(j).getMethod())) {
					implMethodsCount++;
				}
			}

			count += implMethodsCount;
		}

		return count;
	}

	public int getTotalMethodsCount(int _jsonID) {
		int count = 0;

		for (int i = 0; i < pathArray.size(); i++) {
			if (pathArray.get(i).getJsonID() == _jsonID) {
				int implMethodsCount = 0;

				for (int j = 0; j < pathArray.get(i).methods.size(); j++) {
					if (implementedMethods.contains(pathArray.get(i).methods.get(j).getMethod())) {
						implMethodsCount++;
					}
				}

				count += implMethodsCount;
			}
		}

		return count;
	}

	public void printAllPathAndMethods() {

		for (int i = 0; i < pathArray.size(); i++) {

			System.out.println(
					"\n[INFO] [Analyzer] Path: " + pathArray.get(i).getPathID() + " - " + pathArray.get(i).getUrl());

			for (int j = 0; j < pathArray.get(i).methods.size(); j++) {

				System.out.println("[INFO] [Analyzer]	" + pathArray.get(i).methods.get(j).getMethod()
						+ ", statusHTTP " + pathArray.get(i).methods.get(j).getStatusReq() + ", statusSPEC "
						+ pathArray.get(i).methods.get(j).getStatusSpec());

			}
		}
	}

	public void printAllPathAndMethods(PrintWriter statsWriter) {
		statsWriter.println();
		for (int i = 0; i < pathArray.size(); i++) {

			statsWriter.println("\nPath: " + pathArray.get(i).getPathID() + " - " + pathArray.get(i).getUrl());

			for (int j = 0; j < pathArray.get(i).methods.size(); j++) {

				statsWriter.println("	" + pathArray.get(i).methods.get(j).getMethod() + ", statusHTTP "
						+ pathArray.get(i).methods.get(j).getStatusReq() + ", statusSPEC "
						+ pathArray.get(i).methods.get(j).getStatusSpec());
			}
		}
	}

	public float getAPFD(ArrayList<TestFrame> testFrames, int faultNum, boolean onlySevere) {
		System.out.println();
		float apfd = 0;
		int partialSum = 0;
		ArrayList<PathAfterSession> pathArrAll = new ArrayList<PathAfterSession>();
		ArrayList<PathAfterSession> pathArrSev = new ArrayList<PathAfterSession>();

		for (int i = 0; i < testFrames.size(); i++) {

			if (testFrames.get(i).getState().equals("failed") && testFrames.get(i).getResponseCode() > 0) {
				boolean newMethod = false;
				boolean existentPath = false;
				int pathIndex = 0;

				if (onlySevere) {

					if (testFrames.get(i).getFailureSeverity() == 2) {

						for (int j = 0; j < pathArrSev.size(); j++) {

							if (pathArrSev.get(j).getUrl().equals(testFrames.get(i).getName())) {
								pathIndex = j;
								existentPath = true;

								for (int x = 0; x < pathArrSev.get(j).getMethods().size(); x++) {

									if (!pathArrSev.get(j).getMethods().get(x).getMethod()
											.equals(testFrames.get(i).getReqType())) {
										newMethod = true;
										break;
									}
								}
							}
						}

						if (!existentPath) {
							partialSum += (i + 1);
							pathArrSev.add(new PathAfterSession(testFrames.get(i).getName()));
							pathArrSev.get(pathArrSev.size() - 1).addMethod(testFrames.get(i).getReqType());
						} else if (existentPath && newMethod) {
							partialSum += (i + 1);
							pathArrSev.get(pathIndex).addMethod(testFrames.get(i).getReqType());
						}
					}

				} else {

					for (int j = 0; j < pathArrAll.size(); j++) {

						if (pathArrAll.get(j).getUrl().equals(testFrames.get(i).getName())) {
							pathIndex = j;
							existentPath = true;
							for (int x = 0; x < pathArrAll.get(j).getMethods().size(); x++) {

								if (!pathArrAll.get(j).getMethods().get(x).getMethod()
										.equals(testFrames.get(i).getReqType())) {
									newMethod = true;
									break;
								}
							}
						}
					}

					if (!existentPath) {
						partialSum += (i + 1);
						pathArrAll.add(new PathAfterSession(testFrames.get(i).getName()));
						pathArrAll.get(pathArrAll.size() - 1).addMethod(testFrames.get(i).getReqType());
					} else if (existentPath && newMethod) {
						partialSum += (i + 1);
						pathArrAll.get(pathIndex).addMethod(testFrames.get(i).getReqType());
					}
				}
			}

		}

		System.out.println(
				"[INFO] #Test = " + testFrames.size() + " #fault = " + faultNum + " PartialSum = " + partialSum);
		int div = testFrames.size() * faultNum;
		apfd = 1 - ((float) partialSum / div) + ((float) 1 / (2 * testFrames.size()));

		return apfd;
	}

	public void analyzeTest(ArrayList<TestFrame> testFrames) {

	}

	public void printExecutionStatistics(ArrayList<Integer> tfs) {

		int execTest = tfs.size() - internalErrorCount;

		// printAllPathAndMethods();

		System.out.println("[Analyzer] Executed test: " + execTest);
		System.out.println("[Analyzer] Success: " + successIndexes.size());
		System.out.println(
				"[Analyzer] Test Failures: " + failedIndexes.size() + " (Severe: " + severeIndexes.size() + ")");
		System.out.println(
				"[Analyzer] Method failures: " + getFailedMethods() + " (Severe: " + getSevereFailedMethods() + ")");
		System.out.println("[Analyzer] Coverage (3 class): " + getCoverage());
		System.out.println("[Analyzer] Coverage (2 class): " + getCoverageTwoClass());
	}

}
