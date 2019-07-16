package application;

public class Aeroport {

	private String nom;
	private String ville;
	private String pays;
	private String code;
	private double lat;
	private double lon;
	
	public Aeroport(String nom, String ville, String pays, String code, double lat, double lon) {
		this.nom = nom;
		this.ville = ville;
		this.pays = pays;
		this.code = code;
		this.lat = lat;
		this.lon = lon;
	}

	public String getPays() {
		return pays;
	}
	
	public String getNom() {
		return nom;
	}

	public String getVille() {
		return ville;
	}

	public String getCode() {
		return code;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	@Override
	public String toString() {
		return "Aeroport [nom=" + nom + ", ville=" + ville + ", pays=" + pays + ", code=" + code + ", lat=" + lat
				+ ", lon=" + lon + "]";
	}

	
	
}
