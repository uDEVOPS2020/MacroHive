package com.microservice.uTest.analyzer;

import java.util.ArrayList;

public class MethodAfterSession {
	
	private String method;
	private int statusReq;			
	private int statusSpec;			
	private ArrayList<Integer> returnCodes;
	
	public MethodAfterSession(){
		method = new String();
		returnCodes = new ArrayList<Integer>();
		statusSpec = 0;
		statusReq = 0;
	}
	
	public MethodAfterSession(String _method){
		method = new String(_method);
		returnCodes = new ArrayList<Integer>();
		statusSpec = 0;
		statusReq = 0;
	}
	
	public MethodAfterSession(int _statusReq){
		method = new String();
		returnCodes = new ArrayList<Integer>();
		
		this.statusReq = _statusReq;
	}
	
	public MethodAfterSession(int _statusReq, int _statusSpec){
		method = new String();
		returnCodes = new ArrayList<Integer>();
		
		this.statusReq = _statusReq;
		this.statusReq = _statusSpec;
	}
	
	public String getMethod() {
		return method;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}
	
	public int getStatusReq() {
		return statusReq;
	}
	
	public void setStatusReq(int _statusReq) {
		this.statusReq = _statusReq;
	}
	
	public int getStatusSpec() {
		return statusSpec;
	}
	
	public void setStatusSpec(int _statusSpec) {
		this.statusSpec = _statusSpec;
	}
	
	public ArrayList<Integer> getReturnCodes() {
		return returnCodes;
	}
	
	public void addReturnCode(int _code) {
		if(!returnCodes.contains(_code)) {
			returnCodes.add(_code);
			updateStatusReq();
		}
		
		
	}
	
	public boolean verifySuccessClass() {

		for(int i = 0; i < returnCodes.size(); i++) {
			if(returnCodes.get(i) >= 200 && returnCodes.get(i) < 300)
				return true;
		}
		
		return false;
	}
	
	public boolean verifyClientErrorClass() {
		
		for(int i = 0; i < returnCodes.size(); i++) {
			if(returnCodes.get(i) >= 400 && returnCodes.get(i) < 500)
				return true;
		}
		
		return false;
	}
	
	public boolean verifyServerErrorClass() {
		
		for(int i = 0; i < returnCodes.size(); i++) {
			if(returnCodes.get(i) >= 500 && returnCodes.get(i) < 600)
				return true;
		}
		return false;
	}
	
	public void updateStatusReq() {
		int currentStatus = this.statusReq;
		int nextStatus = this.statusReq;
		
		if(currentStatus < 2) {
			for(int i = 0; i < returnCodes.size(); i++) {
				if(returnCodes.get(i) >= 500 && returnCodes.get(i) < 600) {
					nextStatus = 2;
					break;
				}else if(returnCodes.get(i) >= 400 && returnCodes.get(i) < 500) {
					if(currentStatus == 0)
						nextStatus = 1;
				}
			}
			
			this.statusReq = nextStatus;
		}
	}	
}














