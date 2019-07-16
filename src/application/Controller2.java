package application;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;
import com.interactivemesh.jfx.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class Controller2 implements Initializable {
	@FXML
	AnchorPane anchorPane;
	@FXML
	GridPane gridPane2D;
	@FXML
	Pane pane3D;
	@FXML
	Button valide;
	@FXML
	TextField origine;
	@FXML
	TextField destination;
	@FXML
	ComboBox<String> paysDepart;
	@FXML
	ComboBox<String> paysDest;
	@FXML
	ComboBox<String> villeDepart;
	@FXML
	ComboBox<String> villeDest;
	@FXML
	ComboBox<String> aeroportDepart;
	@FXML
	ComboBox<String> aeroportDest;
	@FXML
	Button butRetour;
	@FXML
	ListView<String> listV;
	@FXML
	Pane paneVol;

	Group earth;
	Group root3D = new Group();
	Group filsDep, filsDest;
	Group groupVol = new Group();;
	Group pos = new Group();
	Fx3DGroup plane;

	public String nomPaysDep, nomPaysDest;
	public String nomVilleDep, nomVilleDest;
	public String nomAeroDep, nomAeroDest;
	float latDep, latDest, lonDep, lonDest;

	List<Flight> listFlight;
	List<String> listVol;
	List<Aeroport> listADep = new ArrayList<>();
	List<Aeroport> listADest = new ArrayList<>();
	ObservableList<String> obsListVol;// = FXCollections.observableArrayList(listVol);
	List<String> listPays;
	Ville villeDep;
	Ville villeArr;
	//Flight flight;
	Application app = new Application();
	
	ObjModelImporter objImporter = new ObjModelImporter();
	// VBox bv = new VBox();

	private static final float TEXTURE_LAT_OFFSET = -0.2f;
	private static final float TEXTURE_LON_OFFSET = 2.8f;
	//private final long startNanoTime = System.nanoTime();

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		 listV.setLayoutX(29);
		 listV.setLayoutY(60);
		// n'affiche pas la listeView des vols
		anchorPane.getChildren().removeAll(butRetour, listV, paneVol);

		// obsListVol = FXCollections.observableArrayList(listVol);
		// listV.setItems(obsListVol);
		// mettre les pays
		listPays = app.getListPays();
		paysDepart.setItems(FXCollections.observableArrayList(listPays));
		paysDest.setItems(FXCollections.observableArrayList(listPays));

		// Create a Pane et graph scene root for the 3D content

		// Load geometry
		
		try {
			URL modeUrl = this.getClass().getResource("Earth/earth.obj");
			objImporter.read(modeUrl);
		} catch (ImportException e) {
			System.out.println(e.getMessage());
		}
		MeshView[] meshViews = (MeshView[]) objImporter.getImport();
		earth = new Group((Node[]) meshViews);
		// add the earth
		root3D.getChildren().addAll(earth);

		// Add a camera group
		PerspectiveCamera camera = new PerspectiveCamera(true);
		new CameraManager(camera, root3D, root3D);

		// Add point light
		PointLight light = new PointLight(Color.WHITE);
		light.setTranslateX(-180);
		light.setTranslateY(-90);
		light.setTranslateZ(-120);
		light.getScope().addAll(root3D);
		root3D.getChildren().add(light);

		// Add ambient light
		AmbientLight ambientLight = new AmbientLight(Color.WHITE);
		ambientLight.getScope().addAll(root3D);
		root3D.getChildren().add(ambientLight);

		// root3D.setLayoutX(330);
		// Creer la sous-scene
		SubScene subScene = new SubScene(root3D, 478, 500, true, SceneAntialiasing.BALANCED);
		// subScene.setLayoutX(330);
		subScene.setCamera(camera);
		subScene.setFill(Color.GREY);
		pane3D.getChildren().addAll(subScene);
	}

	@FXML
	public void paysDepEvent() {
		nomPaysDep = null;
		nomVilleDep = null;
		nomAeroDep = null;
		nomPaysDep = paysDepart.getSelectionModel().getSelectedItem();
		// System.out.println(nomPays);
		List<String> listV = app.listeVilleDePays(nomPaysDep);
		ObservableList<String> obs = FXCollections.observableArrayList(listV);
		// System.out.println(obs.toString());
		villeDepart.setDisable(true); // vider la liste des ville de ce pays
		aeroportDepart.setDisable(true);
		villeDepart.setItems(obs); // mettre les villes dans combobox
		villeDepart.setDisable(false);
	}

	@FXML
	public void paysDestEvent() {
		nomPaysDest = null;
		nomVilleDest = null;
		nomAeroDest = null;
		nomPaysDest = paysDest.getSelectionModel().getSelectedItem();
		// System.out.println(nomPays);
		List<String> listV = app.listeVilleDePays(nomPaysDest);
		ObservableList<String> obs = FXCollections.observableArrayList(listV);
		villeDest.setDisable(true);
		aeroportDest.setDisable(true);
		villeDest.setItems(obs);
		villeDest.setDisable(false);
	}

	@FXML
	public void villeDepEvent() {
		if(listADep != null)
			listADep.clear();
		nomVilleDep = villeDepart.getSelectionModel().getSelectedItem();

		if (nomVilleDep != null) {
			System.out.println(nomVilleDep);
			listADep = app.ListAeroportVille(nomVilleDep, nomPaysDep);
			Set<String> list = new TreeSet<>();
			for (Aeroport a : listADep) {
				list.add(a.getNom());
			}
			System.out.println(list);
			// mettre les aeroports dans le combn
			aeroportDepart.setDisable(true);
			aeroportDepart.setItems(FXCollections.observableArrayList(list));
			aeroportDepart.setDisable(false);
		}
	}

	@FXML
	public void villeDestEvent() {
		if(listADest != null)
			listADest.clear();
		nomVilleDest = villeDest.getSelectionModel().getSelectedItem();
		if (nomVilleDest != null) {
			listADest = app.ListAeroportVille(nomVilleDest, nomPaysDest);
			Set<String> list = new TreeSet<>();
			for (Aeroport a : listADest) {
				list.add(a.getNom());
			}
			System.out.println(list);
			aeroportDest.setDisable(true);
			// mettre les aeroports dans le combn
			aeroportDest.setItems(FXCollections.observableArrayList(list));
			aeroportDest.setDisable(false);
		}
	}

	@FXML
	public void aeroDepEvent() {
		nomAeroDep = aeroportDepart.getSelectionModel().getSelectedItem();
		System.out.println("aeroport depart : " + nomAeroDep);
	}

	@FXML
	public void aeroDestEvent() {
		nomAeroDest = aeroportDest.getSelectionModel().getSelectedItem();
		System.out.println("aeroport d'arrivee : " + nomAeroDest);
	}

	@FXML
	public void rechercherVols() {
		System.out.println("dep = " + nomAeroDep + "  arrivee  = " + nomAeroDest);
		if (nomAeroDep != null || nomAeroDest != null) {
			System.out.println("dep = " + nomAeroDep + "  arrivee  = " + nomAeroDest);
			Aeroport aDep = trouverAero(nomAeroDep, listADep);
			Aeroport aDest = trouverAero(nomAeroDest, listADest);
			if(aDep != null) {
				latDep = (float) aDep.getLat();
				lonDep = (float) aDep.getLon();
			}
			if(aDest != null) {
				latDest = (float) aDest.getLat();
				lonDest = (float) aDest.getLon();
			}
			
			listFlight = app.listVols(nomAeroDep, nomAeroDest);
			System.out.println("{{{{{{{{{{ Latitude logitude depart :  " + latDep + "    " + lonDep);
			System.out.println("{{{{{{{{{{ Latitude logitude destination :  " + latDest + "    " + lonDest);
			// listVol = app.listVolsString(nomAeroDep, nomAeroDest);
			// System.out.println("LIIIIIIIIIIIIIIIIST : " + listVol);
			listVol = new ArrayList<>();
			// System.out.println(listFlight);
			for (int i = 0; i < listFlight.size(); i++) {
				int id = listFlight.get(i).getId();
				String dep = listFlight.get(i).getFrom();
				String arr = listFlight.get(i).getTo();
				String comp = listFlight.get(i).Op;
				String type = listFlight.get(i).getType();
				String s = id + " " + " " + comp + " " + dep + " " + arr;// + " " + type + " ";
				System.out.println("'''''''''''''''''''''''''' TRAK : " + listFlight.get(i).Trak);
				afficheVol(listFlight.get(i));
				listVol.add(s);
			}
			// mettre la liste des vols dans la listView
			obsListVol = FXCollections.observableArrayList(listVol);
			listV.setItems(obsListVol);
			// if(listV != null) {
			// System.out.println(listV.getItems().toString());
			// }
			// listV.setDisable(false);
			// bv.getChildren().add(listV);
			// enlever la dialogue
			anchorPane.getChildren().remove(gridPane2D);
			anchorPane.getChildren().remove(valide);
			// afficher les vols
			anchorPane.getChildren().addAll(butRetour, listV, paneVol);

			filsDep = new Group();
			filsDest = new Group();

			displayAirport(root3D, filsDep, nomAeroDep, latDep, lonDep, Color.WHITE);
			displayAirport(root3D, filsDest, nomAeroDest, latDest, lonDest, Color.RED);
			
			root3D.getChildren().add(groupVol);
		}
	}

	@FXML
	public void retourEvent() {
		// listVol.clear();
		// listV.getItems().clear();
		// effacerList();
		if(paneVol.getChildren() != null)
			paneVol.getChildren().clear();
		System.out.println("*********** " + listVol.toString());
		// System.out.println("########### " + listV.getItems().toString());
		anchorPane.getChildren().removeAll(butRetour, listV, paneVol);
		anchorPane.getChildren().addAll(gridPane2D, valide);
		root3D.getChildren().removeAll(filsDep, filsDest, groupVol);
		groupVol.getChildren().clear();
	}
	
	@FXML
	public void afficherDetailVol() {
		if(paneVol.getChildren() != null)
			paneVol.getChildren().clear();
		
		int i = listV.getSelectionModel().getSelectedIndex();
		System.out.println("SELECTIONEEEEEEEEEEEEE :    " + i);
		Flight flight = listFlight.get(i);
		Text text = new Text(flight.getId() + "\nfrom : " + flight.From + "\nTo : " + flight.To + "\n");
		text.setText(text.getText() + "type : " + flight.Type + "\nmilitaire : " + flight.Mil + "\n");
		text.setText(text.getText());
		paneVol.getChildren().add(text);
		
		//afficher les dernieres positions du vol
		app.afficheListDernPos(flight);
		for(int j=0; j<flight.getLat().length; j++) {
			Integer id = new Integer(flight.Id);
			displayAirport(root3D, pos, id.toString(), (float)flight.getLat()[i], (float)flight.getLong()[0], Color.YELLOW);
		}
		
	}

	private Aeroport trouverAero(String nom, List<Aeroport> listA) {
		for (Aeroport a : listA) {
			if (a.getNom().equals(nom)) {
				return a;
			}
		}
		return null;
	}

	// From Rahel Lüthy : https://netzwerg.ch/blog/2015/03/22/javafx-3d-line/
	public Cylinder createLine(Point3D origin, Point3D target) {
		Point3D yAxis = new Point3D(0, 1, 0);
		Point3D diff = target.subtract(origin);
		double height = diff.magnitude();

		Point3D mid = target.midpoint(origin);
		Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

		Point3D axisOfRotation = diff.crossProduct(yAxis);
		double angle = Math.acos(diff.normalize().dotProduct(yAxis));
		Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);

		Cylinder line = new Cylinder(0.01f, height);

		line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);

		return line;
	}

	public static Point3D geoCoordTo3dCoord(float lat, float lon) {
		float lat_cor = lat + TEXTURE_LAT_OFFSET;
		float lon_cor = lon + TEXTURE_LON_OFFSET;
		return new Point3D(
				-java.lang.Math.sin(java.lang.Math.toRadians(lon_cor))
						* java.lang.Math.cos(java.lang.Math.toRadians(lat_cor)),
				-java.lang.Math.sin(java.lang.Math.toRadians(lat_cor)),
				java.lang.Math.cos(java.lang.Math.toRadians(lon_cor))
						* java.lang.Math.cos(java.lang.Math.toRadians(lat_cor)));
	}

	public void displayAirport(Group parent, Group fils, String name, float latitude, float longitude, Color color) {
		// fils = new Group();
		// une petite sphere representant la ville
		Sphere sphere = new Sphere(0.01);
		PhongMaterial material = new PhongMaterial(color);
		sphere.setMaterial(material);

		// convertir les coordonnees GPS de la ville en un point3D
		Point3D point = geoCoordTo3dCoord(latitude, longitude);
		fils.setId(name);
		// translater le group fils à la bonne position
		fils.setTranslateX(point.getX());
		fils.setTranslateY(point.getY());
		fils.setTranslateZ(point.getZ());
		fils.getChildren().add(sphere);
		parent.getChildren().add(fils);
	}
	

	public void afficheVol(Flight fliht) {
		//root3D.getChildren().remove(plane);
		ObjModelImporter objImporterPlane = new ObjModelImporter();
		
		
		try {
			URL modeUrl = this.getClass().getResource("Plane/plane.obj");
			objImporterPlane.read(modeUrl);
		} catch (ImportException e) {
			System.out.println(e.getMessage());
		}
		// ...
		MeshView[] planeMeshViews = objImporterPlane.getImport();
		Fx3DGroup planeScale = new Fx3DGroup(planeMeshViews);
		Fx3DGroup planeOffset = new Fx3DGroup(planeScale);
		plane = new Fx3DGroup(planeOffset);

		Point3D position = geoCoordTo3dCoord((float)fliht.Lat, (float)fliht.Long);

		planeScale.set3DScale(0.015);
		planeOffset.set3DTranslate(0, -0.01, 0);
		if(fliht.TrkH)
			planeOffset.set3DRotate(0, 180 - fliht.Trak, 0);
		else
			planeOffset.set3DRotate(0, 180 + fliht.Trak, 0);
		
		plane.set3DTranslate(position.getX(), position.getY(), position.getZ());
		plane.set3DRotate(-java.lang.Math.toDegrees(java.lang.Math.asin(position.getY())) - 90,
				java.lang.Math.toDegrees(java.lang.Math.atan2(position.getX(), position.getZ())), 0);
		groupVol.getChildren().addAll(plane);
	}

}
