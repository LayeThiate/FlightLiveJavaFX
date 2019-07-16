package application;

public class Ville {

	private String nom;

	public Ville(String nom) {
		this.nom = nom;
	}
	
	public String getNom() {
		return nom;
	}

	
	@Override
	public String toString() {
		return "Ville --> NOM: " + nom ;
	}

	
}
