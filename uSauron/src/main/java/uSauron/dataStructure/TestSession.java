package uSauron.dataStructure;

import java.util.ArrayList;

public class TestSession {
	
	private Integer JsonID;
	private Integer pathMethodID;
	private String method;
	private String url;
	private int code;
	private int failureSeverity;
	private ArrayList<UService> services;
	

	
	public TestSession () {
		this.services = new ArrayList<UService>(); 
	}	
	
	public TestSession(TestSession ts) {
		
		this.JsonID = ts.getJsonID();
		this.pathMethodID = ts.getPathMethodID();
		this.method = ts.getMethod();
		this.url = ts.getUrl();
		this.code = ts.getCode();
		this.failureSeverity = ts.getFailureSeverity();
		
		this.services = new ArrayList<UService>();
		for(int i = 0; i < ts.getServices().size(); i++) {
			UService tempServ = new UService(ts.getServices().get(i));
			this.services.add(tempServ);
		}
		
	}

	
	

	public ArrayList<UService> getServices() {
		return services;
	}
	
	
	public int getServiceIndex(String _proxyID) {

		int i = 0;
		boolean found = false;

		while (i < services.size() && !found) {
			if (services.get(i).getName().equals(_proxyID)) {
				found = true;
			} else {
				i++;
			}
		}

		if (found)
			return i;

		return -1;
	}
	
	public int forceGetServiceIndex(String _proxyID) {

		int i = 0;
		boolean found = false;

		while (i < services.size() && !found) {
			if (services.get(i).getName().equals(_proxyID)) {
				found = true;
			} else {
				i++;
			}
		}

		if (!found) {
			services.add(new UService(_proxyID));
			return services.size() - 1;
		}
		return i;
	}



	public void addPacketToService(String proxyID, String infoPacket) {
		
		int serviceIndex = forceGetServiceIndex(proxyID);
		
		services.get(serviceIndex).addPacket(new InfoPacket(infoPacket));
		
		
	}





	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getFailureSeverity() {
		return failureSeverity;
	}

	public void setFailureSeverity(int failureSeverity) {
		this.failureSeverity = failureSeverity;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setPathMethodID(Integer pathMethodID) {
		this.pathMethodID = pathMethodID;
	}
	
	public Integer getPathMethodID() {
		return pathMethodID;
	}


	public void setJsonID(Integer JsonID) {
		this.JsonID = JsonID;
	}
	
	public Integer getJsonID() {
		return JsonID;
	}

}
