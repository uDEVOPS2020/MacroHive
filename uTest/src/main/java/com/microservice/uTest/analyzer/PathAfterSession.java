package com.microservice.uTest.analyzer;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.RED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PathAfterSession {
	
	private int pathID;
	private int jsonID;
	private String url;
	public ArrayList<MethodAfterSession> methods;
	
	
	
	
	public PathAfterSession(int _pathID){
		this.pathID = _pathID;
		url = new String();
		methods = new ArrayList<MethodAfterSession>();
	}
	
	public PathAfterSession(String _url){
		url = new String();
		url = _url;
		methods = new ArrayList<MethodAfterSession>();
	}
	
	public PathAfterSession(int _pathID, String _url){
		this.pathID = _pathID;
		url = new String(_url);
		methods = new ArrayList<MethodAfterSession>();
	}
	
	public PathAfterSession(int _jsonID, int _pathID, String _url){
		this.pathID = _pathID;
		this.setJsonID(_jsonID);
		url = new String(_url);
		methods = new ArrayList<MethodAfterSession>();
	}
	
	public PathAfterSession(PathAfterSession path){
		this.pathID = path.getPathID();
		url = new String(path.getUrl());
		methods = new ArrayList<MethodAfterSession>(path.getMethods());
	}
	
	
	public void setFailedMethodStatusSpec(String _method) {
		
		boolean existentMethod = false;
		int index = 0;
		
		while (index < methods.size() && !existentMethod) {
			if(methods.get(index).getMethod().equals(_method)) {
				existentMethod = true;
			}else {
				index++;
			}
		}
		
		if(existentMethod) {
			if(methods.get(index).getStatusSpec() <2)
				methods.get(index).setStatusSpec(1);
		}else {
			System.out.println("["+ansi().fgBright(RED).a("ERROR").reset()+"] [PathAfterSession] Trying to set failed status of no existing method..");
		}
		
	}
	
	
	public void setSevereFailedMethodStatusSpec(String _method) {
		boolean existentMethod = false;
		int index = 0;
		
		while (index < methods.size() && !existentMethod) {
			if(methods.get(index).getMethod().equals(_method)) {
				existentMethod = true;
			}else {
				index++;
			}
		}
		
		if(existentMethod) {
			methods.get(index).setStatusSpec(2);
		}else {
			System.out.println("["+ansi().fgBright(RED).a("ERROR").reset()+"] [PathAfterSession] Trying to set severe failed status of no existing method..");
		}
	}
	
	
	public void addReturnCodeToMethod(String _method, int _code) {
		
		boolean existentMethod = false;
		int index = 0;
		
		while (index < methods.size() && !existentMethod) {
			if(methods.get(index).getMethod().equals(_method)) {
				existentMethod = true;
			}else {
				index++;
			}
		}
		
		if(existentMethod) {
			methods.get(index).addReturnCode(_code);
			
		}else {
			methods.add(new MethodAfterSession(_method));
			methods.get(methods.size()-1).addReturnCode(_code);	
		}
		
	}
	
	public void addMethod(String _method) {
		
		boolean existentMethod = false;
		int index = 0;
		
		while (index < methods.size() && !existentMethod) {
			if(methods.get(index).getMethod().equals(_method)) {
				existentMethod = true;
			}else {
				index++;
			}
		}
		
		if(!existentMethod) {
			methods.add(new MethodAfterSession(_method));
		}	
	}
	
	
	
	public int getPathID() {
		return this.pathID;
	}
	
	public int getFailedMethodsCountSpec() {
		int count = 0;
		
		for(int i = 0; i < methods.size(); i++) {
			if(methods.get(i).getStatusSpec() > 0) {
				
				count++;
			}
		}
		return count;
	}
	
	public int getSevereFailedMethodsCountSpec() {
		int count = 0;
		
		for(int i = 0; i < methods.size(); i++) {
			if(methods.get(i).getStatusSpec() > 1) {
				count++;
			}
		}
		return count;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}
	
	
	public ArrayList<MethodAfterSession> getMethods() {
		return this.methods;
	}

	public int getJsonID() {
		return jsonID;
	}

	public void setJsonID(int jsonID) {
		this.jsonID = jsonID;
	}
	
}
