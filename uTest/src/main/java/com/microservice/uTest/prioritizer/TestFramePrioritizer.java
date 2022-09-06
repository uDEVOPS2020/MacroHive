package com.microservice.uTest.prioritizer;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.RED;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.microservice.uTest.dataStructure.TestFrame;
import com.microservice.uTest.analyzer.TestFrameAnalyzer;

public class TestFramePrioritizer {
	
	public static boolean ONLY_SEVERE = true;
	
	private HashMap<String, Float> methodWeights;
	private HashMap<String, Float> methodOnlySevereWeights;
	
	private ArrayList<Float> fieldWeights;
	private ArrayList<Float> fieldOnlySevereWeights;


	
	
	public TestFramePrioritizer() {
		super();
		
		methodWeights = new  HashMap<String, Float>();
		methodOnlySevereWeights = new  HashMap<String, Float>();
		
		fieldWeights = new  ArrayList<Float>();
		fieldOnlySevereWeights = new  ArrayList<Float>();
		
		for(int i=0;i<TestFrameAnalyzer.FIELDS_NUM;i++) {
			fieldWeights.add((float)0);
		}
		
		for(int i=0;i<TestFrameAnalyzer.FIELDS_NUM;i++) {
			fieldOnlySevereWeights.add((float)0);
		}
	}
	
	public TestFramePrioritizer(boolean onlySevere) {
		super();
		
		methodWeights = new  HashMap<String, Float>();
		methodOnlySevereWeights = new  HashMap<String, Float>();
		
		fieldWeights = new  ArrayList<Float>();
		fieldOnlySevereWeights = new  ArrayList<Float>();
		
		for(int i=0;i<TestFrameAnalyzer.FIELDS_NUM;i++) {
			fieldWeights.add((float)0);
		}
		
		for(int i=0;i<TestFrameAnalyzer.FIELDS_NUM;i++) {
			fieldOnlySevereWeights.add((float)0);
		}
		
		ONLY_SEVERE = onlySevere;
	}
	
	
	public void calculateWeights(TestFrameAnalyzer tfa) {
		
		for(Map.Entry<String, ArrayList<Integer>>  method : tfa.methodCount.entrySet()) {
			float weight = 0;
			float onlySevereWeight = 0;
			float penalizingMethodFailure = 0;
			float penalizingMethodSevere = 0;

			if(method.getValue().get(TestFrameAnalyzer.TOTAL_INDEX) != 0) {
				
				penalizingMethodFailure = (float)((float)method.getValue().get(TestFrameAnalyzer.TOTAL_INDEX) - method.getValue().get(TestFrameAnalyzer.FAILURE_INDEX))/method.getValue().get(TestFrameAnalyzer.TOTAL_INDEX);
				penalizingMethodSevere = (float)((float)method.getValue().get(TestFrameAnalyzer.TOTAL_INDEX) - method.getValue().get(TestFrameAnalyzer.SEVERE_FAILURE_INDEX))/method.getValue().get(TestFrameAnalyzer.TOTAL_INDEX);

				
				weight = ((float)method.getValue().get(TestFrameAnalyzer.FAILURE_INDEX)/method.getValue().get(TestFrameAnalyzer.TOTAL_INDEX))-penalizingMethodFailure;
				onlySevereWeight = ((float)method.getValue().get(TestFrameAnalyzer.SEVERE_FAILURE_INDEX)/method.getValue().get(TestFrameAnalyzer.TOTAL_INDEX))-penalizingMethodSevere;
				
			}

			methodWeights.put(method.getKey(),weight);
			methodOnlySevereWeights.put(method.getKey(),onlySevereWeight);
		}
		
		
		
		for(int i = 0;i< TestFrameAnalyzer.FIELDS_NUM; i++) {
			float penalizingFieldFailure = 0;
			float penalizingFieldSevere = 0;
			
			if(tfa.hasFieldCount.get(i).get(TestFrameAnalyzer.TOTAL_INDEX) != 0) {
				
				penalizingFieldFailure = (float)((float)tfa.hasFieldCount.get(i).get(TestFrameAnalyzer.TOTAL_INDEX) - tfa.hasFieldCount.get(i).get(TestFrameAnalyzer.FAILURE_INDEX))/tfa.hasFieldCount.get(i).get(TestFrameAnalyzer.TOTAL_INDEX);
				penalizingFieldSevere = (float)((float)tfa.hasFieldCount.get(i).get(TestFrameAnalyzer.TOTAL_INDEX) - tfa.hasFieldCount.get(i).get(TestFrameAnalyzer.SEVERE_FAILURE_INDEX))/tfa.hasFieldCount.get(i).get(TestFrameAnalyzer.TOTAL_INDEX);
				
				fieldWeights.set(i,((float)tfa.hasFieldCount.get(i).get(TestFrameAnalyzer.FAILURE_INDEX)/tfa.hasFieldCount.get(i).get(TestFrameAnalyzer.TOTAL_INDEX))-penalizingFieldFailure);
				fieldOnlySevereWeights.set(i,((float)tfa.hasFieldCount.get(i).get(TestFrameAnalyzer.SEVERE_FAILURE_INDEX)/tfa.hasFieldCount.get(i).get(TestFrameAnalyzer.TOTAL_INDEX))-penalizingFieldSevere);
								
			}
		}
		
		
	}

	public void prioritizeTest(ArrayList<TestFrame> testFrames) {
		
		for (int i=0; i<testFrames.size(); i++) {
			
			ArrayList<Integer> hasFieldB = new ArrayList<Integer>();
			int hasWeightenedMethod = 0;
			int hasFieldCount = 0;
			
			for(int x=0;x<TestFrameAnalyzer.FIELDS_NUM;x++) {
				hasFieldB.add(0);
			}
			
			for(int j = 0; j<testFrames.get(i).ic.size(); j++) {
				String typeTemp = testFrames.get(i).ic.get(j).type;
	    		  
				if(typeTemp.equals("date")) hasFieldB.set(TestFrameAnalyzer.DATE_IDX,1);
				else if(typeTemp.equals("date-time")) hasFieldB.set(TestFrameAnalyzer.DATETIME_IDX,1);
				else if(typeTemp.equals("int32")) hasFieldB.set(TestFrameAnalyzer.INT32_IDX,1);
				else if(typeTemp.equals("int64")) hasFieldB.set(TestFrameAnalyzer.INT64_IDX,1);
				else if(typeTemp.equals("float")) hasFieldB.set(TestFrameAnalyzer.FLOAT_IDX,1);
				else if(typeTemp.equals("double")) hasFieldB.set(TestFrameAnalyzer.DOUBLE_IDX,1);
				else if(typeTemp.equals("empty")) hasFieldB.set(TestFrameAnalyzer.EMPTY_IDX,1);
				else if(typeTemp.equals("range")) hasFieldB.set(TestFrameAnalyzer.RANGE_IDX,1);
				else if(typeTemp.equals("symbol")) hasFieldB.set(TestFrameAnalyzer.SYMBOL_IDX,1);
				else if(typeTemp.equals("greater")) hasFieldB.set(TestFrameAnalyzer.GREATER_IDX,1);
				else if(typeTemp.equals("lower")) hasFieldB.set(TestFrameAnalyzer.LOWER_IDX,1);
				else if(typeTemp.equals("b_true")) hasFieldB.set(TestFrameAnalyzer.BTRUE_IDX,1);
				else if(typeTemp.equals("b_false")) hasFieldB.set(TestFrameAnalyzer.BFALSE_IDX,1);
				else if(typeTemp.equals("s_range")) hasFieldB.set(TestFrameAnalyzer.SRANGE_IDX,1);
				else if(typeTemp.equals("lang")) hasFieldB.set(TestFrameAnalyzer.LANG_IDX,1);
			}
			
			float  beforeNormalizationPriority = 0;
			
			if(ONLY_SEVERE) {
				for(Map.Entry<String, Float>  method : methodOnlySevereWeights.entrySet()) {
					if(testFrames.get(i).getReqType().equals(method.getKey())) {
						beforeNormalizationPriority += method.getValue();
						hasWeightenedMethod ++;
					}
				}
				
				for(int x=0; x < fieldOnlySevereWeights.size(); x++) {
					beforeNormalizationPriority += hasFieldB.get(x) * fieldOnlySevereWeights.get(x);
					if(hasFieldB.get(x) == 1)
						hasFieldCount++;
				}
				
			}else {
				for(Map.Entry<String, Float>  method : methodWeights.entrySet()) {
					if(testFrames.get(i).getReqType().equals(method.getKey())) {
						beforeNormalizationPriority += method.getValue();
						hasWeightenedMethod ++;
					}
				}
				
				for(int x=0; x < fieldWeights.size(); x++) {
					beforeNormalizationPriority += hasFieldB.get(x) * fieldWeights.get(x);
					if(hasFieldB.get(x) == 1)
						hasFieldCount++;
				}
			}
			
			
			
			float normalizedPriority = beforeNormalizationPriority/(hasWeightenedMethod+hasFieldCount);
			

			testFrames.get(i).setPriority(normalizedPriority);
			
		}
	}
	
	
	
	public void printWeights() {
		System.out.println("\n[INFO] [Prioritizer]	Weights: \n");
		for(Map.Entry<String, Float>  method : methodWeights.entrySet()) {
			System.out.println("[INFO] [Prioritizer]	"+method.getKey() +" (allFailure, onlySevere): ("+ method.getValue()+", "+methodOnlySevereWeights.get(method.getKey())+")");
		}
		for(int i = 0;i< fieldWeights.size(); i++) {
			System.out.println("[INFO] [Prioritizer]	"+TestFrameAnalyzer.names.get(i) +" (allFailure, onlySevere): ("+ fieldWeights.get(i)+", "+fieldOnlySevereWeights.get(i)+")");
		}
	}
	
	
	
	public void getWeightsFromFile(String fileToRead) {
		
		try {
	        File myObj = new File(fileToRead);
	        Scanner myReader = new Scanner(myObj);
	        int index = 0;
	        
	        while (myReader.hasNextLine()) {
	          String data = myReader.nextLine();
	          String[] splittedBothWeights = data.split(";");
	          String[] splittedWeight = splittedBothWeights[0].split("=");
	          String[] splittedOnlySevereWeight = splittedBothWeights[1].split("=");
	          String name = splittedWeight[0];
	          String weight = splittedWeight[1];
	          String weightOnlySevere = splittedOnlySevereWeight[1];
	          
	          if(!data.contains("**")) {
	        	  if (!TestFrameAnalyzer.names.contains(name)){
	        		  methodWeights.put(name,Float.parseFloat(weight));
	        		  methodOnlySevereWeights.put(name,Float.parseFloat(weightOnlySevere));
	        	  }else {
	        		  fieldWeights.set(index, Float.parseFloat(weight));
	        		  fieldOnlySevereWeights.set(index, Float.parseFloat(weightOnlySevere));
	        		  index++;
	        	  }
	          }
	        }
	        myReader.close();
	      } catch (Exception e) {
	    	  System.out.println("["+ansi().fgBright(RED).a("ERROR").reset()+"] GetWeightsFromFile: File read failed.");
	      }
	}
	
	
	public void printWeightsToFile(String fileToWrite) {
		
		try {
		      FileWriter myWriter = new FileWriter(fileToWrite); 
		      BufferedWriter out = new BufferedWriter(myWriter);
		      
		      for(Map.Entry<String, Float>  method : methodWeights.entrySet()) {
		    	  out.write(method.getKey() +"="+ method.getValue());
		    	  out.write(";OnlySevere="+ methodOnlySevereWeights.get(method.getKey()));
		    	  out.newLine();
		      }
					
		      for(int i=0; i < fieldWeights.size(); i++) {
		    	  out.write(TestFrameAnalyzer.names.get(i) +"="+ fieldWeights.get(i));
		    	  out.write(";OnlySevere="+ fieldOnlySevereWeights.get(i));
		    	  out.newLine();
		      }
		      
		      out.close();
		      myWriter.close();
	      } catch (Exception e) {
	        System.out.println("["+ansi().fgBright(RED).a("ERROR").reset()+"] File writing error..");
	        
	      }
	}
	
	
	public void setOnlySevere(boolean onlySevere) {
		ONLY_SEVERE = onlySevere;
	}
	
}
