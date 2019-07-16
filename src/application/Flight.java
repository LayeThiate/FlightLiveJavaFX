package application;
//http://www.virtualradarserver.co.uk/Documentation/Formats/AircraftList.aspx

public class Flight {

	public int Id;
	public String Icao;
	public double Spd;
	public double Trak; // angle de l'avion
	public String Type;
	public String From;
	public String To;
	public boolean Mil;
	public String Op; // compagnie
	public double Lat;
	public double Long;
	public boolean TrkH;

	public String Call;
	public double[] Cot = new double[1500];

	public double[] getLat() {

		double[] lat = new double[((Cot.length) / 3)];
		int j = 0;
		for (int i = 0; i < Cot.length; i += 3) {
			lat[j] = Cot[i];
			j++;
		}
		return lat;
	}

	public double[] getLong() {

		double[] lon = new double[((Cot.length/3))];
		int j = 0;
		for (int i = 1; i < Cot.length; i += 3) {
			lon[j] = Cot[i];
			j++;
		}
		return lon;
	}

	public double[] getDir() {

		double[] dir = new double[1500];
		for (int i = 2; i < Cot.length; i += 3) {
			dir[i] = Cot[i];
		}
		return dir;
	}

	public String getCall() {
		return Call;
	}

	public String getCompagnie() {
		return Op;
	}

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}

	public String getIcao() {
		return Icao;
	}

	public void setIcao(String icao) {
		Icao = icao;
	}

	public double getSpd() {
		return Spd;
	}

	public void setSpd(float spd) {
		Spd = spd;
	}

	public double getTrak() {
		return Trak;
	}

	public void setTrak(float trak) {
		Trak = trak;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public String getFrom() {
		return From;
	}

	public void setFrom(String from) {
		From = from;
	}

	public String getTo() {
		return To;
	}

	public void setTo(String to) {
		To = to;
	}

	@Override
	public String toString() {

		String s = "";
		s += this.Id + ",  [FROM : " + getFrom() + "],  [TO :"  + getTo() + "  CALL  :" +getCall() + "  COT : " + Cot.toString() + "\n";		// return "Flight [Id=" + Id + ", Icao=" + Icao + ", Spd=" + Spd + ", Trak=" +
		// Trak + ", Type=" + Type + ", From="
		// + From + ", To=" + To + "]";
		return s;
	}

}
