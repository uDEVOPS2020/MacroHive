package uSauron.dataStructure;

import java.util.ArrayList;

public class UService {
	
	private String name;

	private ArrayList<InfoPacket> packets;
	
	public UService() {
		this.packets = new ArrayList<InfoPacket>();
	}
	
	public UService(String _name) {
		this.name = _name;
		this.packets = new ArrayList<InfoPacket>();
	}
	
	
	
	public UService(UService uS) {
		
		this.name = uS.getName();
		
		this.packets = new ArrayList<InfoPacket>();
		
		for(int i = 0; i < uS.getPackets().size(); i++) {
			this.packets.add(uS.getPackets().get(i));
		}
		
		
	}

	public void addPacket(InfoPacket _packet) {
		this.packets.add(_packet);
	}
	
	public ArrayList<InfoPacket> getPackets() {
		return this.packets;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean containsMethod(String url, String method) {
		
		
		
		
		return false;
	}
	
}
