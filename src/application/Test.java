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

public class Test {
	static boolean recu = false;
	
	private static final String navigateur = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36";
	private static final String lien = "https://public-api.adsbexchange.com/VirtualRadar/AircraftList.json?";
	
	private static Map<String, List<Ville>> mapP = new HashMap<>(); // liste des villes de chaque pays
	private static Map<String, List<Aeroport>> mapV = new HashMap<>(); // listes des aeroports de chaque villes
	private static Map<String, String> codeNomAreo = new HashMap<>(); // correspondance entre nom et iceao de chaque aeroport
	private static Map<String, List<Flight>> aeroProv = new HashMap<>(); // les vols en provenanes d'un aeroport
	private static Map<String, List<Flight>> aeroDest = new HashMap<>(); // les vols à destination d'un aeroport
	private static Map<String, List<Flight>> villeProv = new HashMap<>(); // les vols en provenance d'une ville;
	private static Map<String, List<Flight>> villeDest = new HashMap<>(); // les vols à destination d'une ville;
	
	private static FlightList flights = null ;

	// parser les pays,villes et aéroports
	public static void parserAeroport(String csv) {
		
		List<Aeroport> liste = new ArrayList<>();
		List<Ville> lv = new ArrayList<>();
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

					if (mapP.containsKey(nomPays)) {
						lv = mapP.get(nomPays);
						lv.add(ville);
						// for(Ville v : lv)
						// System.out.println(v.toString());
						mapP.put(nomPays, new ArrayList<Ville>(lv));
					} else {
						lv.clear();
						lv.add(ville);
						// System.out.println(lv.toString());
						// for(Ville v : lv)
						// System.out.println(v.toString());
						mapP.put(nomPays, new ArrayList<Ville>(lv));
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
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			buffread.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	// vols en destinatione de l'aeroport prov et destination de l'aeroport dest
	public static void volProvEtDestAero(String prov, String dest, Map<String, List<Flight>> listProv, Map<String, List<Flight>> listDest) {
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
				
				for(Flight fl : flights.getAcList()){
					String codeProv = fl.getFrom().split(" ")[0]; // ajouter que les vols en provenance
					String codeDest = fl.getTo().split(" ")[0];
					if(codeProv.equals(icaoProv) && codeDest.equals(icaoDest)) {
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
	
	//vol à destination ou en provenance l'aeropport en parametre
	public static void volProvOuDestAero(String nomAero, Map<String, List<Flight>> listProv, Map<String, List<Flight>> listDest) {
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
				
				for(Flight fl : flights.getAcList()){
					String code = fl.getFrom().split(" ")[0]; // ajouter que les vols en provenance
					if(code.equals(icao)) {
						System.out.println(fl);
						ajouterVol(listProv, nomAero, fl);
					}
					else {
						code = fl.getTo().split(" ")[0]; // ajouter que les vols à destination
						if(code.equals(icao)) {
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
	
	public static void volProOuDestvVille(String nomV, String nomPays, Map<String, List<Flight>> mapProv, Map<String, List<Flight>> mapDest) {
		recu = false;
		List<Aeroport> listA = ListAeroportVille(nomV, nomPays);
		for(Aeroport a : listA) {
			volProvOuDestAero(a.getNom(), mapProv, mapDest);
			attendreRequete();
		}
		//attendreRequete();
		System.out.println("######### SIZE = " + mapProv.size() + " taille list = " + listA.size());
	}
	
	public static void volProEtDestvVille(String nomVProv, String nomPaysProv, String nomVDest, String nomPaysDest, Map<String, List<Flight>> mapProv, Map<String, List<Flight>> mapDest) {
		List<Aeroport> listA = ListAeroportVille(nomVProv, nomPaysProv);
		List<Aeroport> listB = ListAeroportVille(nomVDest, nomPaysDest);
		for(Aeroport a : listA) {
			for(Aeroport b : listB) {
				volProvEtDestAero(a.getNom(), b.getNom(), mapProv, mapDest);
				//attendreRequete();
			}
		}
	}
	
	// trouver l'ICAO d'un aeroport
	private static String trouverIcaoNom(String nom) {
		nom = nom.toUpperCase();
		System.out.println();
		if(!codeNomAreo.containsKey(nom))
			throw new IllegalArgumentException("Probleme d'argument");
		return codeNomAreo.get(nom);
	}
	
	//ajouter un vol dans une map de string
	private static void ajouterVol(Map<String, List<Flight>> map, String key, Flight flight){
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
	
	//construire une requete GET
	public static BoundRequestBuilder creerRequete(String type) {
		// Configurer le client http
		DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config().setConnectTimeout(500)
				.setUserAgent(navigateur).setKeepAlive(false);
		AsyncHttpClient client = Dsl.asyncHttpClient(clientBuilder);

		// Créer une requête de type GET
		BoundRequestBuilder getRequest = client.prepareGet(lien + type);
		return getRequest;
	}

	//renvoie la liste des aeroports d'une ville
	public static List<Aeroport> ListAeroportVille(String ville, String nomPays) {
		
		if(!mapV.containsKey(ville))
			throw new IllegalArgumentException(ville + ", CETTE VILLE N'EXISTE PAS DANS LA BASE");
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
	
	//liste dest vols en provenance et a destination de deux aeroports
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
	
	public static void afficheListObjet(List<? extends Object> list) {
		if(list == null || list.size() == 0) {
			System.out.println("LISTE VIDE");
			return;
		}
		
		for(Object o : list) {
			System.out.println(o.toString());
		}
	}
	
	public static void afficheListVolAero(Map<String, List<Flight>> map) {
		Iterator<Entry<String, List<Flight>>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<Flight>> e = it.next();
			//System.out.println(e.getKey() + "  " + e.getValue());
			List<Flight> list = e.getValue();
			afficheListObjet(list);
		}
	}

	//affiche la liste des aeroports
	private static void afficheListAreo(List<Aeroport> list) {
		for (Aeroport a : list) {
			System.out.println(a.toString());
		}
	}

	//affiche la liste des vols
	private static void afficheListVol(List<Flight> list ) {
		if(list == null || list.size() == 0) {
			System.out.println("LISTE VIDE");
			return;
		}
			
		for(int i=0; i<list.size(); i++) {
			System.out.println(list.get(i));
		}
	}
	
	public static List<Ville> listeVilleDePays(String nomPays) {

		List<Ville> listeVille = new ArrayList<>();
		listeVille = mapP.get(nomPays);
		return listeVille;
	}

	public static void AfficherListe(List<Ville> listeVille) {

		for (int i = 0; i < listeVille.size(); i++) {
			Ville liste = listeVille.get(i);
			System.out.println(liste);

		}
	}

	public static void attendreRequete() {
		while(!recu) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void afficheListDernPos(Flight vol) {

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
					double[] poslat ;//= new double[1400];
					poslat = fl.getLat();
					
					double[] posLon = fl.getLong();
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
	
public static List<Flight > listvols () {
		
		List <Flight> liste = new ArrayList <Flight> ();
		Iterator<Entry<String, List<Flight>>> it = aeroProv.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<Flight>> e = it.next();
			// System.out.println(e.getKey() + " " + e.getValue());
			liste.addAll(e.getValue());
		}
		Iterator<Entry<String, List<Flight>>> ite = aeroDest.entrySet().iterator();
		while (ite.hasNext()) {
			Entry<String, List<Flight>> e = ite.next();
			// System.out.println(e.getKey() + " " + e.getValue());
			liste.addAll(e.getValue());
			
		}

		return liste;
		
	}
	
	public static void main(String[] args) {

		parserAeroport("airports.csv");

		// ******** VILLE D'UN PAYS
		//OK
//		 List<Ville> listeVille = listeVilleDePays("Morocco");
//		 //AfficherListe(listeVille);
//		 afficheListObjet(listeVille);

		// ********* AEROPORT D'UNE VILLE
		//OK
		//List <Aeroport> listeAero = ListAeroportVille("New York", "United States");
//		// afficheListAreo(listeAero);
		//afficheListObjet(listeAero);
		
		
		//************* LISTE DES VOLS EN PROVENANCE OU A DESTINATION D'UN AEROPORT
		//OK
//		volProvOuDestAero("Charles de Gaulle International Airport", aeroProv, aeroDest);
//		volProvOuDestAero("Mohammed V International Airport", aeroProv, aeroDest);
		
		
		
		//************ LISTE DES VOLS EN PROVENANCE ET DESTINATION DE DEUX  AEROPORTS
		//OK
//		volProvEtDestAero("Charles de Gaulle International Airport", "John F Kennedy International Airport", aeroProv, aeroDest);
//		List<Flight> listFli = listVols("Charles de Gaulle International Airport", "John F Kennedy International Airport");
//		System.out.println("{{{{{{{' list flight");
//		afficheListVol(listFli);
		
		// ************AFFICHAGE D'UNE LISTE DE VOLS	
		//OK
//		attendreRequete();
//		System.out.println("########### VOL EN PROVENANCE DE  ");
//		afficheListVolAero(aeroProv);
//		System.out.println("########### VOL À DESTINATION");
//		afficheListVolAero(aeroDest);
		
		
		//************ LISTE DES VOLS EN PROVENANCE OU A DESTINATION D'UNE VILLE 
		//OK
//		volProOuDestvVille("Paris", "France", villeProv, villeDest);
////		attendreRequete();
//		System.out.println("########### VOL EN PROVENANCE DE  ");
//		afficheListVolAero(villeProv);
//		System.out.println("########### VOL À DESTINATION");
//		afficheListVolAero(villeDest);
		
		
		//************ LISTE DES VOLS EN PROVENANCE ET A DESTINATION DE DEUX VILLES
		//OK
		//volProEtDestvVille("Paris", "France", "New York", "United States", villeProv, villeDest);
		
		//*********** LES DERNIERES POSITIONS D'UN VOL
		volProvOuDestAero("Charles de Gaulle International Airport", aeroProv, aeroDest);
		attendreRequete();
		System.out.println("################# AFFICHAGE :");
		List<Flight> listF = listvols();
		//afficheListObjet(listF);
		//System.out.println(listF.get(0).Call);
		afficheListDernPos(listF.get(0));
		
		 
//		Iterator<Entry<String, String>> it = codeNomAreo.entrySet().iterator();
//		while (it.hasNext()) {
//			Entry<String, String> e = it.next();
////			if(e.getKey().equals("Charles de Gaulle International Airport"))
//			System.out.println(e.getKey() + "  " + e.getValue());
//		}
		
		
	}
}
