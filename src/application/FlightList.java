package application;


public class FlightList {
	private Flight[] acList; //List of planes
	private  String lastDv; //Timestamp for further query
	
	public FlightList() {
		super();
	}

	 
	public Flight[] getAcList() {
		return acList;
	}

	public void setAcList(Flight[] acList) {
		this.acList = acList;
	}

	public String getLastDv() {
		return lastDv;
	}

	public void setLastDv(String lastDv) {
		this.lastDv = lastDv;
	}
	
	public int getSize() {
		return acList.length;
	}

	
	@Override
	public String toString() {
		return "FlightList [acList=" + acList + ", lastDv=" + lastDv + "]";
	}
}
