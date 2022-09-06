package com.microservice.uTest.executor;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.CYAN;
import static org.fusesource.jansi.Ansi.Color.MAGENTA;
import static org.fusesource.jansi.Ansi.Color.YELLOW;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import com.microservice.uTest.analyzer.TestFrameAnalyzer;
import com.microservice.uTest.dataStructure.TestFrame;
import com.microservice.uTest.main.UTestApplication;
import com.microservice.uTest.prioritizer.PrioritySorter;
import com.microservice.uTest.prioritizer.TestFramePrioritizer;

public class TestExecutor {
	private static boolean DEBUG_MODE = false;
	private static boolean NOTIFY_MODE = false;
	private String executionMode;
	private String priorityMode;
	private String priorityFocus;
	private String authToken;
	private int dynamicRequestBuffer;

	public TestExecutor(boolean debug, boolean notify, String exec, String token) {
		DEBUG_MODE = debug;
		NOTIFY_MODE = notify;
		this.authToken = token;
		this.executionMode = exec;
		this.dynamicRequestBuffer = 0;
		this.priorityMode = "all_failure";
		this.priorityFocus = "method";
	}

	public TestExecutor(boolean debug, boolean notify, String exec, String token, String _priorityMode,
			String _priorityFocus) {
		DEBUG_MODE = debug;
		this.priorityMode = _priorityMode;
		this.priorityFocus = _priorityFocus;
		this.authToken = token;
		this.executionMode = exec;
		this.dynamicRequestBuffer = 0;
	}

	public TestExecutor(boolean debug, boolean notify, String exec, String token, String _priorityMode,
			String _priorityFocus, int buffer) {
		DEBUG_MODE = debug;
		this.priorityMode = _priorityMode;
		this.priorityFocus = _priorityFocus;
		this.authToken = token;
		this.dynamicRequestBuffer = buffer;
		this.executionMode = exec;
	}

	public long ExecuteAllTest(ArrayList<TestFrame> testFrames, ArrayList<Integer> testSelected,
			PrintWriter logWriter) {

		for (int i = 0; i < testFrames.size(); i++) {
			testSelected.add(i);
		}

		long startReq = System.currentTimeMillis();

		startRequesting(testFrames, testSelected, testFrames.size(), logWriter);

		return System.currentTimeMillis() - startReq;
	}

	public long ExecuteRandomTest(ArrayList<TestFrame> testFrames, ArrayList<Integer> testSelected, int testNum,
			PrintWriter logWriter) {

		Random random = new Random();

		if (testNum == testFrames.size()) {
			System.out.println("\n[INFO] [Executor] All dataset selected..");
			logWriter.println("\n[INFO] [Executor] All dataset selected..");
			for (int i = 0; i < testFrames.size(); i++) {
				testSelected.add(i);
			}
		} else {
			System.out.println("\n[INFO] [Executor] Selecting " + testNum + " random test..");
			logWriter.println("\n[INFO] [Executor] Selecting " + testNum + " random test..");

			for (int i = 0; i < testNum; i++) {
				int rNum = random.nextInt(testFrames.size() - 1);
				while (testSelected.contains(rNum)) {
					rNum = random.nextInt(testFrames.size() - 1);
				}
				testSelected.add(rNum);
			}
		}
		long startReq = System.currentTimeMillis();

		startRequesting(testFrames, testSelected, testNum, logWriter);

		return System.currentTimeMillis() - startReq;
	}

	public long ExecuteDynamicPrioritizationTest(ArrayList<TestFrame> testFrames, ArrayList<Integer> testSelected,
			int testNum, PrintWriter logWriter) {

		long startReq = System.currentTimeMillis();

		if (priorityFocus.equals("method")) {
			startRequestingDynamicPrioritization(testFrames, testSelected, testNum, true, logWriter);
		} else {
			startRequestingDynamicPrioritization(testFrames, testSelected, testNum, false, logWriter);
		}

		return System.currentTimeMillis() - startReq;
	}

	public long ExecuteDynamicPrioritizationTestWeightsFromFile(ArrayList<TestFrame> testFrames,
			ArrayList<Integer> testSelected, int testNum, String filename, PrintWriter logWriter) {

		TestFramePrioritizer prioritizer;

		if (priorityMode.equals("only_severe")) {
			prioritizer = new TestFramePrioritizer(true);
		} else {
			prioritizer = new TestFramePrioritizer(false);
		}

		prioritizer.getWeightsFromFile(filename);
		prioritizer.printWeights();
		prioritizer.prioritizeTest(testFrames);

		testFrames.sort(new PrioritySorter());
		Collections.reverse(testFrames);

		for (int i = 0; i < testNum; i++) {
			testSelected.add(i);
		}

		long startReq = System.currentTimeMillis();

		startRequesting(testFrames, testSelected, testNum, logWriter);

		return System.currentTimeMillis() - startReq;
	}

	public void ExecuteForAPFD(ArrayList<TestFrame> testFrames, ArrayList<Integer> testSelected,
			ArrayList<Float> AllMPerc, ArrayList<Float> SevMPerc, ArrayList<Float> APFDs, int faultNumAll,
			int faultNumSev, boolean randomSel) {

		ArrayList<TestFrame> testFramesToExecute = new ArrayList<TestFrame>(testFrames);
		ArrayList<Integer> allFault = new ArrayList<Integer>();
		ArrayList<Integer> sevFault = new ArrayList<Integer>();

		if (!randomSel) {

			startRequestingDynamicPrioritizationForAPFD(testFramesToExecute, testSelected, testFramesToExecute.size(),
					true, allFault, sevFault, APFDs, faultNumAll, faultNumSev);

		} else {
			Random random = new Random();

			for (int i = 0; i < testFramesToExecute.size() - 1; i++) {

				int rNum = random.nextInt(testFramesToExecute.size() - 1);
				while (testSelected.contains(rNum)) {
					rNum = random.nextInt(testFramesToExecute.size() - 1);
				}
				testSelected.add(rNum);
			}

			for (int i = 0; i < testFramesToExecute.size(); i++) {
				if (!testSelected.contains(i)) {
					testSelected.add(i);
				}
			}

			startRequestingForAPFD(testFramesToExecute, testSelected, testFramesToExecute.size(), allFault, sevFault,
					APFDs, faultNumAll, faultNumSev);
		}

		for (int i = 0; i < allFault.size(); i++) {
			AllMPerc.add((float) ((float) allFault.get(i) / faultNumAll) * 100);
			SevMPerc.add((float) ((float) sevFault.get(i) / faultNumSev) * 100);
		}

	}

	private void startRequestingDynamicPrioritization(ArrayList<TestFrame> testFrames, ArrayList<Integer> testSelected,
			int testNum, boolean methodExclusive, PrintWriter logWriter) {

		boolean e = false;
		int internalErrorCount = 0;
		int noFailureFoundCount = 0;
		TestFrameAnalyzer tfaInternal = new TestFrameAnalyzer(DEBUG_MODE);
		TestFrameAnalyzer tfa = new TestFrameAnalyzer(DEBUG_MODE);
		TestFramePrioritizer tfp;
		ArrayList<String> failedUrls = new ArrayList<String>();
		ArrayList<Integer> indexPick = new ArrayList<Integer>();
		ArrayList<TestFrame> testNotDone = new ArrayList<TestFrame>(testFrames);
		ArrayList<Integer> indexesToAnalyze = new ArrayList<Integer>();

		int failCount = 0;
		int severeCount = 0;
		int countReset = 1;
		int changeLoading = 0;
		Random random = new Random();
		int bufferIndex = 0;

		if (priorityMode.equals("only_severe")) {
			tfp = new TestFramePrioritizer(true);
		} else {
			tfp = new TestFramePrioritizer(false);
		}

		while (testSelected.size() < dynamicRequestBuffer) {

			indexPick.clear();

			for (int i = 0; i < testNotDone.size(); i++) {
				indexPick.add(i);
			}

			int indexTemp = random.nextInt(indexPick.size() - 1);
			bufferIndex = indexPick.remove(indexTemp);

			if (methodExclusive) {

				while (failedUrls.contains(testFrames.get(bufferIndex).getName()) && indexPick.size() > 0) {

					if (indexPick.size() == 1) {
						indexTemp = 0;
					} else {
						indexTemp = random.nextInt(indexPick.size() - 1);
					}

					bufferIndex = indexPick.remove(indexTemp);

					if (DEBUG_MODE)
						System.out
								.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset() + "] Searching random index..");
				}

				if (indexPick.size() == 0) {
					System.out.println("[" + ansi().fgBright(YELLOW).a("WARNING").reset()
							+ "] [Executor] It was not possible to choose random different method from already failed");
					logWriter.println(
							"[WARNING] [Executor] It was not possible to choose random different method from already failed");
				}
			}

			testSelected.add(bufferIndex);

			testFrames.get(bufferIndex).setFinalToken(authToken);
			e = testFrames.get(bufferIndex).extractAndExecuteTestCase();

			if (!e) {
				if (testFrames.get(bufferIndex).getResponseCode() >= 0) {
					failCount++;

					failedUrls.add(testFrames.get(bufferIndex).getName());

					if (testFrames.get(bufferIndex).getFailureSeverity() == 2) {
						severeCount++;

					}
				} else {
					internalErrorCount++;
				}
				e = false;
			}
			indexesToAnalyze.add(bufferIndex);
			testNotDone.remove(bufferIndex);

		}

		while (testSelected.size() < testNum) {
			tfaInternal.analyzeTest(testFrames, indexesToAnalyze);
			tfp.calculateWeights(tfaInternal);
			tfp.prioritizeTest(testNotDone);

			testNotDone.sort(new PrioritySorter());
			Collections.reverse(testNotDone);

			int tfgIndex = 0;
			int notDoneIndex = 0;

			if (noFailureFoundCount > (dynamicRequestBuffer / 2)) {
				notDoneIndex = random.nextInt(testNotDone.size() - 1);
				noFailureFoundCount = 0;
			}

			if (methodExclusive) {
				while (failedUrls.contains(testNotDone.get(notDoneIndex).getName())
						&& notDoneIndex < testNotDone.size() - 1) {
					if (DEBUG_MODE)
						System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
								+ "] [Executor] Searching prioritized index..");

					notDoneIndex++;
				}

				if (notDoneIndex >= testNotDone.size()) {
					notDoneIndex = 0;
					System.out.println("[" + ansi().fgBright(YELLOW).a("WARNING").reset()
							+ "] [Executor] It was not possible to choose prioritized different method from already failed");
					logWriter.println(
							"[WARNING] [Executor] It was not possible to choose prioritized different method from already failed");

				}
			}

			for (int i = 0; i < testFrames.size(); i++) {

				if (testNotDone.get(notDoneIndex).getTfID() == testFrames.get(i).getTfID()) {
					tfgIndex = i;
				}
			}

			testFrames.get(tfgIndex).setFinalToken(authToken);
			e = testFrames.get(tfgIndex).extractAndExecuteTestCase();
			if (!e) {
				if (testFrames.get(tfgIndex).getResponseCode() >= 0) {
					failCount++;

					failedUrls.add(testFrames.get(tfgIndex).getName());

					if (testFrames.get(tfgIndex).getFailureSeverity() == 2) {
						severeCount++;

					} else if (priorityMode.equals("only_severe")) {
						noFailureFoundCount++;
					}
				} else {
					internalErrorCount++;
				}
				e = false;
			} else {
				noFailureFoundCount++;
			}

			testNotDone.remove(notDoneIndex);
			indexesToAnalyze.add(tfgIndex);
			testSelected.add(tfgIndex);

			changeLoading = loadingPrint(countReset++, changeLoading);

		}

		int execTest = testNum - internalErrorCount;
		int successCount = execTest - failCount;

		tfa.analyzeTest(testFrames, testSelected);

		System.out.println("\n[INFO] [Executor] Results:");
		System.out.println("[INFO] [Executor] Test executed: " + execTest);
		System.out.println("[INFO] [Executor] Success: " + successCount);
		System.out.println("[INFO] [Executor] Test Failures: " + failCount + " (Severe: " + severeCount + ")");
		System.out.println("[INFO] [Executor] Method failures: " + tfa.getFailedMethods() + " (Severe: "
				+ tfa.getSevereFailedMethods() + ")");
		System.out.println("[INFO] [Executor] Coverage (3 class): " + tfa.getCoverage());
		System.out.println("[INFO] [Executor] Coverage (2 class): " + tfa.getCoverageTwoClass());

		logWriter.println("\n[INFO] [Executor] Results:");
		logWriter.println("[INFO] [Executor] Test executed: " + execTest);
		logWriter.println("[INFO] [Executor] Success: " + successCount);
		logWriter.println("[INFO] [Executor] Test Failures: " + failCount + " (Severe: " + severeCount + ")");
		logWriter.println("[INFO] [Executor] Method failures: " + tfa.getFailedMethods() + " (Severe: "
				+ tfa.getSevereFailedMethods() + ")");
		logWriter.println("[INFO] [Executor] Coverage (3 class): " + tfa.getCoverage());
		logWriter.println("[INFO] [Executor] Coverage (2 class): " + tfa.getCoverageTwoClass());

		logWriter.flush();
	}

	private void startRequesting(ArrayList<TestFrame> testFrames, ArrayList<Integer> testSelected, int testNum,
			PrintWriter logWriter) {

		boolean e = false;
		boolean skipTimeouts = false;
		ArrayList<String> bannedUrl = new ArrayList<String>();
		TestFrameAnalyzer tfa = new TestFrameAnalyzer(DEBUG_MODE);

		int failCount = 0;
		int severeCount = 0;
		int internalErrorCount = 0;


		if (executionMode.contains("skip_timeout")) {
			System.out.println("[INFO] [Executor] skipTimeouts mode set..");
			logWriter.println("[INFO] [Executor] skipTimeouts mode set..");
			skipTimeouts = true;
		}

		for (int i = 0; i < testNum; i++) {

			if (!(skipTimeouts && bannedUrl.contains(testFrames.get(testSelected.get(i)).getName()))) {

				testFrames.get(testSelected.get(i)).setFinalToken(authToken);

				if (NOTIFY_MODE)
					UTestApplication.notifier.notifyStartTest(testFrames.get(testSelected.get(i)).getTfID());

				e = testFrames.get(testSelected.get(i)).extractAndExecuteTestCase();

				if (NOTIFY_MODE)
					UTestApplication.notifier.notifyEndTest(testFrames.get(testSelected.get(i)).getJsonID(),
							testFrames.get(testSelected.get(i)).getPathMethodID(),
							testFrames.get(testSelected.get(i)).getTfID(),
							testFrames.get(testSelected.get(i)).getReqType(),
							testFrames.get(testSelected.get(i)).getUrl(),
							testFrames.get(testSelected.get(i)).getFailureSeverity(),
							testFrames.get(testSelected.get(i)).getResponseCode());

				if (!e) {
					if (testFrames.get(testSelected.get(i)).getResponseCode() >= 0) {
						failCount++;
						if (testFrames.get(testSelected.get(i)).getFailureSeverity() == 2)
							severeCount++;
					} else {
						if (skipTimeouts && testFrames.get(testSelected.get(i)).getResponseCode() == -1) {
							System.out.println("[INFO] [Executor] Adding url to timeout banned list ("
									+ testFrames.get(testSelected.get(i)).getUrl() + ")");
							logWriter.println("[INFO] [Executor] Adding url to timeout banned list ("
									+ testFrames.get(testSelected.get(i)).getUrl() + ")");
							bannedUrl.add(new String(testFrames.get(testSelected.get(i)).getName()));
						}

						internalErrorCount++;
					}
					e = false;
				}
			}
		}


		logWriter.flush();
	}

	private void startRequestingDynamicPrioritizationForAPFD(ArrayList<TestFrame> testFrames,
			ArrayList<Integer> testSelected, int testNum, boolean methodExclusive, ArrayList<Integer> allFault,
			ArrayList<Integer> SevFault, ArrayList<Float> APFDs, int faultNumAll, int faultNumSev) {
		System.out.println();
		System.out.println(ansi().fgBright(MAGENTA).a(" [Executor] Requesting..").reset());
		System.out.println();

		boolean e = false;
		int internalErrorCount = 0;
		int noFailureFoundCount = 0;
		int iterAllFault = 0;
		int iterSevFault = 0;
		ArrayList<Integer> apfdAllIndexes = new ArrayList<Integer>();
		ArrayList<Integer> apfdSevIndexes = new ArrayList<Integer>();

		TestFrameAnalyzer tfaInternal = new TestFrameAnalyzer(DEBUG_MODE);
		TestFramePrioritizer tfp;

		ArrayList<String> failedUrls = new ArrayList<String>();
		HashMap<String, ArrayList<String>> bannedMethods = new HashMap<String, ArrayList<String>>();

		ArrayList<Integer> indexPick = new ArrayList<Integer>();
		ArrayList<TestFrame> testNotDone = new ArrayList<TestFrame>(testFrames);
		ArrayList<Integer> indexesToAnalyze = new ArrayList<Integer>();
		ArrayList<TestFrame> testFramesForAPFD = new ArrayList<TestFrame>();

		int failCount = 0;
		int severeCount = 0;
		int countReset = 1;
		int changeLoading = 0;
		Random random = new Random();
		int bufferIndex = 0;

		if (priorityMode.equals("only_severe")) {
			tfp = new TestFramePrioritizer(true);
		} else {
			tfp = new TestFramePrioritizer(false);
		}

		while (testSelected.size() <= dynamicRequestBuffer) {

			indexPick.clear();

			for (int i = 0; i < testNotDone.size(); i++) {
				indexPick.add(i);
			}

			int indexTemp = random.nextInt(indexPick.size() - 1);
			bufferIndex = indexPick.remove(indexTemp);

			if (methodExclusive) {

				while (bannedMethods.containsKey(testFrames.get(bufferIndex).getName()) && indexPick.size() > 0) {
					if (bannedMethods.get(testFrames.get(bufferIndex).getName())
							.contains(testFrames.get(bufferIndex).getReqType())) {
						if (indexPick.size() == 1) {
							indexTemp = 0;
						} else {
							indexTemp = random.nextInt(indexPick.size() - 1);
						}

						bufferIndex = indexPick.remove(indexTemp);

						if (DEBUG_MODE)
							System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
									+ "] [Executor] Searching random index..");
					} else {
						break;
					}
				}

				if (indexPick.size() == 0)
					System.out.println("[" + ansi().fgBright(YELLOW).a("WARNING").reset()
							+ "] [Executor] It was not possible to choose random different method from already failed..");
			}

			testSelected.add(bufferIndex);

			testFrames.get(bufferIndex).setFinalToken(authToken);
			e = testFrames.get(bufferIndex).extractAndExecuteTestCase();

			TestFrameAnalyzer tfa = new TestFrameAnalyzer(DEBUG_MODE);

			testFramesForAPFD.add(testFrames.get(bufferIndex));
			tfa.analyzeTest(testFramesForAPFD);

			if (iterAllFault < tfa.getFailedMethods()) {
				apfdAllIndexes.add(testFramesForAPFD.size());
				iterAllFault = tfa.getFailedMethods();
			}
			if (iterSevFault < tfa.getSevereFailedMethods()) {
				apfdSevIndexes.add(testFramesForAPFD.size());
				iterSevFault = tfa.getSevereFailedMethods();
			}

			allFault.add(iterAllFault);
			SevFault.add(iterSevFault);

			if (!e) {
				if (testFrames.get(bufferIndex).getResponseCode() >= 0) {
					failCount++;

					failedUrls.add(testFrames.get(bufferIndex).getName());
					if (bannedMethods.containsKey(testFrames.get(bufferIndex).getName())) {
						bannedMethods.get(testFrames.get(bufferIndex).getName())
								.add(testFrames.get(bufferIndex).getReqType());
					} else {
						bannedMethods.put(testFrames.get(bufferIndex).getName(), new ArrayList<String>());
						bannedMethods.get(testFrames.get(bufferIndex).getName())
								.add(testFrames.get(bufferIndex).getReqType());
					}

					if (testFrames.get(bufferIndex).getFailureSeverity() == 2) {
						severeCount++;

					}
				} else {
					internalErrorCount++;
				}
				e = false;
			}
			indexesToAnalyze.add(bufferIndex);
			testNotDone.remove(bufferIndex);

		}

		while (testSelected.size() < testNum) {
			tfaInternal.analyzeTest(testFrames, indexesToAnalyze);
			tfp.calculateWeights(tfaInternal);
			tfp.prioritizeTest(testNotDone);

			testNotDone.sort(new PrioritySorter());
			Collections.reverse(testNotDone);

			int tfgIndex = 0;
			int notDoneIndex = 0;

			if (noFailureFoundCount > (dynamicRequestBuffer / 2)) {
				if (testNotDone.size() > 1) {
					notDoneIndex = random.nextInt(testNotDone.size() - 1);
				}
				noFailureFoundCount = 0;
			}

			if (methodExclusive) {
				while (bannedMethods.containsKey(testNotDone.get(notDoneIndex).getName())
						&& notDoneIndex < testNotDone.size() - 1) {
					if (bannedMethods.get(testNotDone.get(notDoneIndex).getName())
							.contains(testFrames.get(notDoneIndex).getReqType())) {

						if (DEBUG_MODE)
							System.out.println("[" + ansi().fgBright(CYAN).a("DEBUG").reset()
									+ "] [Executor] Searching prioritized index..");

						notDoneIndex++;
					} else {
						break;
					}
				}

				if (notDoneIndex >= testNotDone.size()) {
					notDoneIndex = 0;
					System.out.println("[" + ansi().fgBright(YELLOW).a("WARNING").reset()
							+ "] [Executor] It was not possible to choose prioritized different method from already failed..");
				}
			}

			for (int i = 0; i < testFrames.size(); i++) {

				if (testNotDone.get(notDoneIndex).getTfID() == testFrames.get(i).getTfID()) {
					tfgIndex = i;
				}
			}

			testFrames.get(tfgIndex).setFinalToken(authToken);
			e = testFrames.get(tfgIndex).extractAndExecuteTestCase();

			TestFrameAnalyzer tfa = new TestFrameAnalyzer(DEBUG_MODE);
			testFramesForAPFD.add(testFrames.get(tfgIndex));

			tfa.analyzeTest(testFramesForAPFD);

			if (iterAllFault < tfa.getFailedMethods()) {
				apfdAllIndexes.add(testFramesForAPFD.size());
				iterAllFault = tfa.getFailedMethods();
			}
			if (iterSevFault < tfa.getSevereFailedMethods()) {
				apfdSevIndexes.add(testFramesForAPFD.size());
				iterSevFault = tfa.getSevereFailedMethods();
			}

			allFault.add(iterAllFault);
			SevFault.add(iterSevFault);

			if (!e) {
				if (testFrames.get(tfgIndex).getResponseCode() >= 0) {
					failCount++;

					failedUrls.add(testFrames.get(tfgIndex).getName());
					if (bannedMethods.containsKey(testFrames.get(tfgIndex).getName())) {
						bannedMethods.get(testFrames.get(tfgIndex).getName())
								.add(testFrames.get(tfgIndex).getReqType());
					} else {
						bannedMethods.put(testFrames.get(tfgIndex).getName(), new ArrayList<String>());
						bannedMethods.get(testFrames.get(tfgIndex).getName())
								.add(testFrames.get(tfgIndex).getReqType());
					}

					if (testFrames.get(tfgIndex).getFailureSeverity() == 2) {
						severeCount++;

					} else if (priorityMode.equals("only_severe")) {
						noFailureFoundCount++;
					}
				} else {
					internalErrorCount++;
				}
				e = false;
			} else {
				noFailureFoundCount++;
			}

			testNotDone.remove(notDoneIndex);
			indexesToAnalyze.add(tfgIndex);
			testSelected.add(tfgIndex);

		}

		int AllSum = 0;
		int SevSum = 0;

		for (int i = 0; i < apfdAllIndexes.size(); i++) {
			AllSum += apfdAllIndexes.get(i);
		}
		for (int i = 0; i < apfdSevIndexes.size(); i++) {
			SevSum += apfdSevIndexes.get(i);
		}

		int divAll = testFramesForAPFD.size() * faultNumAll;
		int divSev = testFramesForAPFD.size() * faultNumSev;

		float apfdAll = 1 - ((float) AllSum / divAll) + ((float) 1 / (2 * testFramesForAPFD.size()));
		float apfdSev = 1 - ((float) SevSum / divSev) + ((float) 1 / (2 * testFramesForAPFD.size()));

		APFDs.add(apfdAll);
		APFDs.add(apfdSev);

		System.out.println();
		System.out.println("[INFO] [Executor] ***************** Requests End");

	}

	private void startRequestingForAPFD(ArrayList<TestFrame> testFrames, ArrayList<Integer> testSelected, int testNum,
			ArrayList<Integer> allFault, ArrayList<Integer> SevFault, ArrayList<Float> APFDs, int faultNumAll,
			int faultNumSev) {

		boolean e = false;
		boolean skipTimeouts = false;
		ArrayList<String> bannedUrl = new ArrayList<String>();
		TestFrameAnalyzer tfa = new TestFrameAnalyzer(DEBUG_MODE);
		ArrayList<TestFrame> testFramesForAPFD = new ArrayList<TestFrame>();

		int countReset = 0;
		int failCount = 0;
		int severeCount = 0;
		int changeLoading = 0;
		int internalErrorCount = 0;
		int iterAllFault = 0;
		int iterSevFault = 0;
		ArrayList<Integer> apfdAllIndexes = new ArrayList<Integer>();
		ArrayList<Integer> apfdSevIndexes = new ArrayList<Integer>();

		if (executionMode.contains("skip_timeout")) {
			System.out.println();
			System.out.println("[INFO] [Executor] skipTimeouts mode set..");
			skipTimeouts = true;
		}

		System.out.println();
		System.out.println(ansi().fgBright(MAGENTA).a("Requesting..").reset());
		System.out.println();

		for (int i = 0; i < testNum; i++) {

			if (!(skipTimeouts && bannedUrl.contains(testFrames.get(testSelected.get(i)).getName()))) {

				testFrames.get(testSelected.get(i)).setFinalToken(authToken);
				e = testFrames.get(testSelected.get(i)).extractAndExecuteTestCase();

				tfa = new TestFrameAnalyzer(DEBUG_MODE);

				testFramesForAPFD.add(testFrames.get(testSelected.get(i)));

				tfa.analyzeTest(testFramesForAPFD);

				if (iterAllFault < tfa.getFailedMethods()) {
					apfdAllIndexes.add(testFramesForAPFD.size());
					iterAllFault = tfa.getFailedMethods();
				}
				if (iterSevFault < tfa.getSevereFailedMethods()) {
					apfdSevIndexes.add(testFramesForAPFD.size());
					iterSevFault = tfa.getSevereFailedMethods();
				}

				allFault.add(iterAllFault);
				SevFault.add(iterSevFault);

				if (!e) {
					if (testFrames.get(testSelected.get(i)).getResponseCode() >= 0) {
						failCount++;
						if (testFrames.get(testSelected.get(i)).getFailureSeverity() == 2)
							severeCount++;
					} else {
						if (skipTimeouts && testFrames.get(testSelected.get(i)).getResponseCode() == -1) {
							System.out.println("[INFO] [Executor] Adding url to timeout banned list ("
									+ testFrames.get(testSelected.get(i)).getUrl() + ")");
							bannedUrl.add(new String(testFrames.get(testSelected.get(i)).getName()));
						}

						internalErrorCount++;
					}
					e = false;
				}
			}
		}

		int AllSum = 0;
		int SevSum = 0;

		for (int i = 0; i < apfdAllIndexes.size(); i++) {
			AllSum += apfdAllIndexes.get(i);
		}
		for (int i = 0; i < apfdSevIndexes.size(); i++) {
			SevSum += apfdSevIndexes.get(i);
		}

		int divAll = testFramesForAPFD.size() * faultNumAll;
		int divSev = testFramesForAPFD.size() * faultNumSev;

		float apfdAll = 1 - ((float) AllSum / divAll) + ((float) 1 / (2 * testFramesForAPFD.size()));
		float apfdSev = 1 - ((float) SevSum / divSev) + ((float) 1 / (2 * testFramesForAPFD.size()));

		APFDs.add(apfdAll);
		APFDs.add(apfdSev);

		System.out.println("\n***************** Requests End");

	}

	private int loadingPrint(int countReset, int changeLoading) {

		if (countReset % 25 == 0) {
			if (changeLoading > 8) {
				changeLoading = 1;
			} else {
				changeLoading++;
			}

			if (changeLoading == 1) {
				System.out.print("\b\b\b\b\b\b" + ansi().fgBright(MAGENTA).a("||||||").reset());
			} else if (changeLoading == 2) {
				System.out.print("\b\b\b\b\b\b" + ansi().fgBright(MAGENTA).a("//////").reset());
			} else if (changeLoading == 3) {
				System.out.print("\b\b\b\b\b\b" + ansi().fgBright(MAGENTA).a("------").reset());
			} else if (changeLoading == 4) {
				System.out.print("\b\b\b\b\b\b" + ansi().fgBright(MAGENTA).a("\\\\\\\\\\\\").reset());
			} else if (changeLoading == 5) {
				System.out.print("\b\b\b\b\b\b" + ansi().fgBright(MAGENTA).a("||||||").reset());
			} else if (changeLoading == 6) {
				System.out.print("\b\b\b\b\b\b" + ansi().fgBright(MAGENTA).a("//////").reset());
			} else if (changeLoading == 7) {
				System.out.print("\b\b\b\b\b\b" + ansi().fgBright(MAGENTA).a("------").reset());
			} else if (changeLoading == 8) {
				System.out.print("\b\b\b\b\b\b" + ansi().fgBright(MAGENTA).a("\\\\\\\\\\\\").reset());
			}

		}
		return changeLoading;
	}

	public String getPriorityMode() {
		return priorityMode;
	}

	public void setPriorityMode(String priorityMode) {
		this.priorityMode = priorityMode;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public int getDynamicRequestBuffer() {
		return dynamicRequestBuffer;
	}

	public void setDynamicRequestBuffer(int dynamicRequestBuffer) {
		this.dynamicRequestBuffer = dynamicRequestBuffer;
	}

	public String getExecutionMode() {
		return executionMode;
	}

	public void setExecutionMode(String executionMode) {
		this.executionMode = executionMode;
	}

}
