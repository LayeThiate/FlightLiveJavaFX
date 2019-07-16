package application;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

//LISTE DES VOLS EN PROVENANCE ET À DESTINATION DE DEUX VILLES 

public class Application {
	boolean recu = false;

	private final String navigateur = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36";
	private final String lien = "https://public-api.adsbexchange.com/VirtualRadar/AircraftList.json?";
	private Set<String> listPays = new TreeSet<>();
	private Map<String, List<String>> mapP = new HashMap<>(); // liste des villes de chaque pays
	private Map<String, List<Aeroport>> mapV = new HashMap<>(); // listes des aeroports de chaque villes
	private Map<String, String> codeNomAreo = new HashMap<>(); // correspondance entre nom et iceao de chaque aeroport
	private Map<String, List<Flight>> aeroProv = new HashMap<>(); // les vols en provenanes d'un aeroport
	private Map<String, List<Flight>> aeroDest = new HashMap<>(); // les vols à destination d'un aeroport
	private Map<String, List<Flight>> villeProv = new HashMap<>(); // les vols en provenance d'une ville;
	private Map<String, List<Flight>> villeDest = new HashMap<>(); // les vols à destination d'une ville;

	private FlightList flights = null;
	
	double[] poslat ;
	double[] posLon;

	public Application() {
		parserAeroport("airports.csv");
	}

	// liste des pays
	public List<String> getListPays() {
		return new ArrayList<>(listPays);
	}

	// parser les pays,villes et aéroports
	public void parserAeroport(String csv) {

		List<Aeroport> liste = new ArrayList<>();
		List<String> lv = new ArrayList<>();
		BufferedReader buffread = null;
		try {
			buffread = new BufferedReader(new FileReader(csv));
			String ligne;
			try {
				while ((ligne = buffread.readLine()) != null) {
					String[] tab = ligne.split(",");
					String nom = tab[0];
					Ville ville = new Ville(tab[1]);
					String nomVille = tab[1];
					Pays pays = new Pays(tab[2]);
					String nomPays = tab[2];
					String code = tab[3];
					double lat = new Double(tab[4]);
					double lon = new Double(tab[5]);
					Aeroport aero = new Aeroport(nom, nomVille, nomPays, code, lat, lon);
					String n = nom.toUpperCase();
					codeNomAreo.put(n, code);

					liste.add(aero);
					listPays.add(nomPays);

					if (mapP.containsKey(nomPays)) {
						lv = mapP.get(nomPays);
						lv.add(nomVille);
						// for(Ville v : lv)
						// System.out.println(v.toString());
						mapP.put(nomPays, new ArrayList<String>(lv));
					} else {
						lv.clear();
						lv.add(nomVille);
						// System.out.println(lv.toString());
						// for(Ville v : lv)
						// System.out.println(v.toString());
						mapP.put(nomPays, new ArrayList<String>(lv));
					}

					if (mapV.containsKey(nomVille)) {
						liste = mapV.get(nomVille);
						liste.add(aero);
						mapV.put(nomVille, new ArrayList<Aeroport>(liste));

					} else {
						liste.clear();
						liste.add(aero);
						mapV.put(nomVille, new ArrayList<>(liste));
					}

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			buffread.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// vols en destinatione de l'aeroport prov et destination de l'aeroport dest
	public void volProvEtDestAero(String prov, String dest, Map<String, List<Flight>> listProv,
			Map<String, List<Flight>> listDest) {
		listProv.clear();
		listDest.clear();
		recu = false;
		String icaoProv = trouverIcaoNom(prov); // récuperer l'icao de l'aeroport
		String icaoDest = trouverIcaoNom(dest);
		String type = "fAirQ=" + icaoProv;
		BoundRequestBuilder getRequest = creerRequete(type);

		// Exécuter la requête et récupérer le résultat
		getRequest.execute(new AsyncCompletionHandler<Object>() {
			@Override
			public Object onCompleted(Response response) throws Exception {
				// json: dictionnaire reçu de la requête
				ObjectMapper mapper = new ObjectMapper();
				mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // Ignorer les champs inutiles
				// Créer l'objet de plus haut niveau dans le dictionnaire json
				flights = mapper.readValue(response.getResponseBody(), FlightList.class);

				for (Flight fl : flights.getAcList()) {
					String codeProv = fl.getFrom().split(" ")[0]; // ajouter que les vols en provenance
					String codeDest = fl.getTo().split(" ")[0];
					if (codeProv.equals(icaoProv) && codeDest.equals(icaoDest)) {
						System.out.println(fl);
						ajouterVol(listProv, prov, fl);
						System.out.println(fl);
						ajouterVol(listDest, dest, fl);
					}

				}
				recu = true;
				return flights;
			}
		});
	}

	// vol à destination ou en provenance l'aeropport en parametre
	public void volProvOuDestAero(String nomAero, Map<String, List<Flight>> listProv,
			Map<String, List<Flight>> listDest) {
		listProv.clear();
		listDest.clear();
		recu = false;
		String icao = trouverIcaoNom(nomAero); // récuperer l'icao de l'aeroport
		String type = "fAirQ=" + icao;
		BoundRequestBuilder getRequest = creerRequete(type);

		// Exécuter la requête et récupérer le résultat
		getRequest.execute(new AsyncCompletionHandler<Object>() {
			@Override
			public Object onCompleted(Response response) throws Exception {
				// json: dictionnaire reçu de la requête
				ObjectMapper mapper = new ObjectMapper();
				mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // Ignorer les champs inutiles
				// Créer l'objet de plus haut niveau dans le dictionnaire json
				flights = mapper.readValue(response.getResponseBody(), FlightList.class);

				for (Flight fl : flights.getAcList()) {
					String code = fl.getFrom().split(" ")[0]; // ajouter que les vols en provenance
					if (code.equals(icao)) {
						System.out.println(fl);
						ajouterVol(listProv, nomAero, fl);
					} else {
						code = fl.getTo().split(" ")[0]; // ajouter que les vols à destination
						if (code.equals(icao)) {
							System.out.println(fl);
							ajouterVol(listDest, nomAero, fl);
						}
					}
				}
				recu = true;
				return flights;
			}
		});
	}

	public void volProOuDestvVille(String nomV, String nomPays, Map<String, List<Flight>> mapProv,
			Map<String, List<Flight>> mapDest) {
		recu = false;
		List<Aeroport> listA = ListAeroportVille(nomV, nomPays);
		for (Aeroport a : listA) {
			volProvOuDestAero(a.getNom(), mapProv, mapDest);
			// attendreRequete();
		}

		System.out.println("######### SIZE = " + mapProv.size() + " taille list = " + listA.size());
	}

	public void volProEtDestvVille(String nomVProv, String nomPaysProv, String nomVDest, String nomPaysDest,
			Map<String, List<Flight>> mapProv, Map<String, List<Flight>> mapDest) {
		List<Aeroport> listA = ListAeroportVille(nomVProv, nomPaysProv);
		List<Aeroport> listB = ListAeroportVille(nomVDest, nomPaysDest);
		for (Aeroport a : listA) {
			for (Aeroport b : listB) {
				volProvEtDestAero(a.getNom(), b.getNom(), mapProv, mapDest);
			}
		}
	}

	// trouver l'ICAO d'un aeroport
	private String trouverIcaoNom(String nom) {
		nom = nom.toUpperCase();
		System.out.println();
		if (!codeNomAreo.containsKey(nom))
			throw new IllegalArgumentException("Probleme d'argument");
		return codeNomAreo.get(nom);
	}

	// ajouter un vol dans une map de string
	private void ajouterVol(Map<String, List<Flight>> map, String key, Flight flight) {
		List<Flight> list;
		if (map.containsKey(key)) {
			list = map.get(key);
			list.add(flight);
			map.put(key, new ArrayList<Flight>(list));
		} else {
			list = new ArrayList<Flight>();
			list.add(flight);
			map.put(key, list);
		}
	}

	// construire une requete GET
	public BoundRequestBuilder creerRequete(String type) {
		// Configurer le client http
		DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config().setConnectTimeout(500)
				.setUserAgent(navigateur).setKeepAlive(false);
		AsyncHttpClient client = Dsl.asyncHttpClient(clientBuilder);

		// Créer une requête de type GET
		BoundRequestBuilder getRequest = client.prepareGet(lien + type);
		return getRequest;
	}

	// renvoie la liste des aeroports d'une ville
	public List<Aeroport> ListAeroportVille(String ville, String nomPays) {
		if (!mapV.containsKey(ville))
			throw new IllegalArgumentException(ville + ", CETTE VILLE N'EXISTE PAS DANS " + nomPays);
		List<Aeroport> listA = null;
		listA = mapV.get(ville);

		// pour les villes qui apparaissent dans des pays differents (Ex: Paris )

		for (int i = 0; i < listA.size(); i++) {
			String pays = listA.get(i).getPays();
			if (pays.equals(nomPays) == false) {
				listA.remove(i);
				i--;
			}
		}
		return listA;
	}

	// liste dest vols en provenance et a destination de deux aeroports
	public List<Flight> listVols(String prov, String dest) {
		if (prov != null && dest != null)
			volProvEtDestAero(prov, dest, aeroProv, aeroDest);
		else if (prov != null)
			volProvOuDestAero(prov, aeroProv, aeroDest);
		else if (dest != null)
			volProvOuDestAero(dest, aeroProv, aeroDest);
		attendreRequete();
		Iterator<Entry<String, List<Flight>>> it;
		List<Flight> listF = new ArrayList<>();

		if (prov != null) {
			it = aeroProv.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, List<Flight>> e = it.next();
				for (Flight f : e.getValue())
					listF.add(f);
			}
		}

		if (dest != null) {
			it = aeroDest.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, List<Flight>> e = it.next();
				for (Flight f : e.getValue())
					listF.add(f);
			}
		}

		return listF;
	}

	public List<String> listVolsString(String prov, String dest) {
		List<Flight> listFlight = listVols(prov, dest);
		List<String> listVol = new ArrayList<>();
		// System.out.println(listFlight);
		for (int i = 0; i < listFlight.size(); i++) {
			int id = listFlight.get(i).getId();
			String dep = listFlight.get(i).getFrom();
			String arr = listFlight.get(i).getTo();
			String type = listFlight.get(i).getType();
			String s = id + " " + dep + " " + arr + " " + type + " ";
			listVol.add(s);
		}
		return listVol;
	}

	public void afficheListObjet(List<? extends Object> list) {
		if (list == null || list.size() == 0) {
			System.out.println("LISTE VIDE");
			return;
		}

		for (Object o : list) {
			System.out.println(o.toString());
		}
	}

	public void afficheListVolAero(Map<String, List<Flight>> map) {
		Iterator<Entry<String, List<Flight>>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<Flight>> e = it.next();
			// System.out.println(e.getKey() + " " + e.getValue());
			List<Flight> list = e.getValue();
			afficheListObjet(list);
		}
	}

	// affiche la liste des aeroports
	private void afficheListAreo(List<Aeroport> list) {
		for (Aeroport a : list) {
			System.out.println(a.toString());
		}
	}

	// affiche la liste des vols
	private void afficheListVol(List<Flight> list) {
		if (list == null || list.size() == 0) {
			System.out.println("LISTE VIDE");
			return;
		}

		for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i));
		}
	}

	public List<String> listeVilleDePays(String nomPays) {
		Set<String> listeVille = new TreeSet<>(mapP.get(nomPays));
		return new ArrayList<String>(listeVille);
	}

	public void AfficherListe(List<Ville> listeVille) {

		for (int i = 0; i < listeVille.size(); i++) {
			Ville liste = listeVille.get(i);
			System.out.println(liste);

		}
	}

	public void attendreRequete() {
		while (!recu) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void afficheListDernPos(Flight vol) {

		String call = vol.getCall();

		String req = "trFmt=f&fCallQ=" + call;
		BoundRequestBuilder getRequest = creerRequete(req);

		// Exécuter la requête et récupérer le résultat
		getRequest.execute(new AsyncCompletionHandler<Object>() {
			@Override
			public Object onCompleted(Response response) throws Exception {
				// json: dictionnaire reçu de la requête
				ObjectMapper mapper = new ObjectMapper();
				mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES); // Ignorer
				// Créer l'objet de plus haut niveau dans le dictionnaire json
				flights = mapper.readValue(response.getResponseBody(), FlightList.class);

				for (Flight fl : flights.getAcList()) {

					System.out.println(" ********************** " + fl);
					System.out.println(" ********************** TAILLE COT = "  + fl.Cot.length);
					// for(int j=0; j<fl.Cot.length; j++){
					// System.out.print(fl.Cot[j]);
					// }
					//= new double[1400];
					poslat = fl.getLat();
					
					posLon = fl.getLong();
					System.out.println(" ********************** TAILLE LAT = "  + poslat.length + ",  LON = " + posLon.length);
					// double[] poslon = new double [1400];
					// poslon = fl.getLong();
					// System.out.println(" ********************** ");
					// for(double l : poslat){
					// System.out.print(l + " ");
					// }
					// for (int i = 1; i < fl.Cot.length; i++) {

					for (int j = 0; j < poslat.length; j++) {
						System.out.println("Position numéro " + j + ": " + "latitude "
								+ poslat[j] + "longitude " + posLon[j] );
					}

					// }
				}

				return flights;
			}
		});
	}
	
//	public double[][] getPosition(Flight vol){
//		afficheListDernPos(vol) ;
//		attendreRequete();
//		double [][] postion = new double[2][];
//		position[0] = poslat;
//		
//	}

}
