package uSauron.dataStructure;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class InfoPacket {
	
	private String url;
	private String method;
	private Integer testID;
	private String uriSender;
	private String uriReceiver;
	private int responseCode;
	private long responseTime;
	
	private String bodyReq;
	private String bodyResp;
	
	
	public InfoPacket() {}
	
	public InfoPacket(String _jsonPacket) {
		JsonObject obj = new JsonParser().parse(_jsonPacket).getAsJsonObject();

		try {
			this.url = obj.get("url").getAsString();
			this.method = obj.get("method").getAsString();
			String parts[]= obj.get("uriSender").getAsString().replace("/", "").split(":");
			this.uriSender = parts[0];
			this.uriReceiver = obj.get("uriReceiver").getAsString();
			this.responseCode = Integer.parseInt(obj.get("responseCode").getAsString());
			this.responseTime = Long.parseLong(obj.get("responseTime").getAsString());
			
			if(obj.has("bodyReq")) {
				this.bodyReq = obj.get("bodyReq").getAsString();
				this.bodyResp = obj.get("bodyResp").getAsString();
			}
			
		} catch (Exception e) {
			System.out.println("[ERROR] [InfoPacket] Error tryng to parse json, exception: " + e.getClass());
		}
	}

	
	public InfoPacket(String _url, String _method, String _uriSender, String _uriReceiver, int _responseCode, long _responseTime) {
		this.url = _url;
		this.method = _method;
		this.uriSender = _uriSender;
		this.uriReceiver = _uriReceiver;
		this.responseCode = _responseCode;
		this.responseTime = _responseTime;		
	}
	
	public InfoPacket(String _url, String _method, String _uriSender, String _uriReceiver, int _responseCode, long _responseTime, String _bodyReq, String _bodyResp) {
		this.url = _url;
		this.method = _method;
		this.uriSender = _uriSender;
		this.uriReceiver = _uriReceiver;
		this.responseCode = _responseCode;
		this.responseTime = _responseTime;
		
		this.bodyReq = _bodyReq;
		this.bodyResp = _bodyResp;
	}
	
	public String getJson() {		
		return new Gson().toJson(this);
	}
	
	
	
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String geturiSender() {
		return uriSender;
	}
	public void seturiSender(String uriSender) {
		this.uriSender = uriSender;
	}
	public String geturiReceiver() {
		return uriReceiver;
	}
	public void seturiReceiver(String uriReceiver) {
		this.uriReceiver = uriReceiver;
	}
	
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	public long getResponseTime() {
		return responseTime;
	}
	public void setResponseTime(long responseTime) {
		this.responseTime = responseTime;
	}
	public String getBodyReq() {
		return bodyReq;
	}
	public void setBodyReq(String bodyReq) {
		this.bodyReq = bodyReq;
	}
	public String getBodyResp() {
		return bodyResp;
	}
	public void setBodyResp(String bodyResp) {
		this.bodyResp = bodyResp;
	}

	public Integer getTestUri() {
		return testID;
	}

	public void setTestID(Integer testID) {
		this.testID = testID;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	
	
}
