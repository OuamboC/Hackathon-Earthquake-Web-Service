package uk.ac.mmu.advprog.hackathon;

import static spark.Spark.get;
import static spark.Spark.notFound;
import static spark.Spark.port;

import java.io.StringWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handles the setting up and starting of the web service You will be adding
 * additional routes to this class, and it might get quite large You should push
 * some of the work into additional child classes, like I did with DB
 * 
 * @author 22561383,Canis Breal Ouambo
 */
public class EarthquakeWebService {

	/**
	 * Main program entry point, starts the web service
	 * 
	 * @param args not used
	 */
	public static void main(String[] args) {
		port(8088);

		// You can check the web service is working by loading
		// http://localhost:8088/test in your browser
		get("/test", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				try (DB db = new DB()) {
					return "Number of entries: " + db.getNumberOfEntries();
				}
			}
		});

		/**
		 * Here , we will create a "/quakesbyyear" route You can check the web service
		 * is working by loading http://localhost:8088/quakecount in your browser
		 */
		get("/quakecount", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				try (DB db = new DB()) {
					String mag = request.queryParams("magnitude");
					double magnitude = Double.parseDouble(mag);

					return "Number of earthquakes: " + db.getNumberOFEarthQuakes(magnitude);
				}
			}
		});

		/**
		 * Here , we will create a "/quakesbyyear" route You can check the web service
		 * is working by loading http://localhost:8088/quakesbyyear in your browser
		 */
		
		get("/quakesbyyear", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception{
				try (DB db = new DB()) {
				String year= request.queryParams("year");
				Double y;
				try {
				 y= Double.parseDouble(year);}
				catch(NumberFormatException e) {
					return "invalid Year";
				}
				
				String mag= request.queryParams("magnitude");
				double magnitude= Double.parseDouble(mag);
				
				JSONArray earthquakes= new JSONArray();
				ResultSet result= db.getNumberOfEarthquakesByYearMagnitude(magnitude, year);
				while(result.next()) {
//					System.out.println(rs1.getDouble("magnitude"));
					JSONObject earthquake=new JSONObject();
					earthquake.put("date", result.getString("time").substring(0,10));
					earthquake.put("magnitude", result.getDouble("mag"));
					JSONObject earthquake2=new JSONObject();
					earthquake2.put("latitude",result.getDouble("latitude"));
					earthquake2.put("latitude",result.getDouble("longitude"));
					earthquake2.put("latitude",result.getString("place"));
					earthquake.put("location", earthquake2);
					earthquake.put("id", result.getString("id"));
					earthquake.put("time", result.getString("time").substring(12,22));
					earthquakes.put(earthquake);
				}
				response.type("application/json");
				return earthquakes;
				
				}
				
	            
				catch(NumberFormatException e) {
					return "invalid Magnitude";
				}
				catch(SQLException e) {
					e.printStackTrace();
				}
				return null;
				
			}
		});
		
	
		/**
		 * Here , we will create a "/quakesbylocation" route You can check the web
		 * service is working by loading http://localhost:8088/quakesbylocation in your
		 * browser
		 */
		get("/quakesbylocation", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				try (DB db = new DB()) {
					String mag = request.queryParams("magnitude");
					double magnitude = Double.parseDouble(mag);
					System.out.println(magnitude);
					String latitude = request.queryParams("latitude");
					double lat = Double.parseDouble(latitude);
					System.out.println(lat);
					String longitude = request.queryParams("longitude");
					double lon = Double.parseDouble(longitude);
					ResultSet result = db.getquakesByLocation(lat, lon, magnitude);

					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					Document doc = dbf.newDocumentBuilder().newDocument();
					// Create the tree
					Element earthquakes = doc.createElement("Earthquakes");
					doc.appendChild(earthquakes);
					while (result.next()) {

						String time = result.getString("time");
						String dAte = result.getString("time");

						String id = result.getString("id");
						String magnitude1 = result.getString("mag");
						String latitude1 = result.getString("latitude");
						String longitude1 = result.getString("longitude");
						String description = result.getString("place");

						Element earthquake = doc.createElement("Earthquake");
						earthquake.setAttribute("id", id);
						Element date1 = doc.createElement("Date");
						date1.setTextContent(dAte);
						Element time1 = doc.createElement("Time");
						time1.setTextContent(time);
						Element magnitudeEl = doc.createElement("Magnitude");
						magnitudeEl.setTextContent(magnitude1);
						earthquake.appendChild(date1);
						earthquake.appendChild(time1);
						earthquake.appendChild(magnitudeEl);

						Element location = doc.createElement("Location");
						Element latitudeEl = doc.createElement("Latitude");
						latitudeEl.setTextContent(latitude1);
						Element longitudeEl = doc.createElement("Latitude");
						latitudeEl.setTextContent(longitude1);
						Element descriptionEl = doc.createElement("Description");
						descriptionEl.setTextContent(description);
						location.appendChild(latitudeEl);
						location.appendChild(longitudeEl);
						location.appendChild(descriptionEl);
						earthquake.appendChild(location);

						earthquakes.appendChild(earthquake);

						System.out.println("hi");
					}

					//
					Transformer transformer = TransformerFactory.newInstance().newTransformer();
					Writer output = new StringWriter();
					transformer.setOutputProperty(OutputKeys.INDENT, "yes");
					transformer.transform(new DOMSource(doc), new StreamResult(output));
					response.type("application/xml");
					System.out.println(output);
					return output;
				} catch (ParserConfigurationException | TransformerException ioe) {
					ioe.printStackTrace();
					return ioe;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}

			}
		});

		// Handle requests that don't match any of the preceding routing rules
		notFound("<html><head></head><body><h1>404 Not Found</h1><p>Canis Breal Ouambo didn't make that page!</p></body></html>");

		System.out.println("Web Service Started. Don't forget to kill it when done testing!");

	}
	
}
