package uk.ac.mmu.advprog.hackathon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;



/**
 * Handles database access from within your web service
 * @author 22561383,Canis Breal Ouambo
 */
public class DB implements AutoCloseable {

	//allows us to easily change the database used
	private static final String JDBC_CONNECTION_STRING = "jdbc:sqlite:./data/earthquakes.db";
	
	//allows us to re-use the connection between queries if desired
	private Connection connection = null;
	
	/**
	 * Creates an instance of the DB object and connects to the database
	 */
	public DB() {
		try {
			connection = DriverManager.getConnection(JDBC_CONNECTION_STRING);
		}
		catch (SQLException sqle) {
			error(sqle);
		}
	}
	
	/**
	 * Returns the number of entries in the database, by counting rows
	 * @return The number of entries in the database, or -1 if empty
	 */
	public int getNumberOfEntries() {
		int result = -1;
		try {
			Statement s = connection.createStatement();
			ResultSet results = s.executeQuery("SELECT COUNT(*) AS count FROM earthquakes");
			while(results.next()) { //will only execute once, because SELECT COUNT(*) returns just 1 number
				result = results.getInt(results.findColumn("count"));
			}
		}
		catch (SQLException sqle) {
			error(sqle);
			
		}
		return result;
	}
	/**
	 * Return the number of earthquakes in the database  , by counting rows
	 * @return the number of earthquakes in the database, or 0 if empty
	 */
	public int  getNumberOFEarthQuakes(Double mag) {
		int result = 0;
		try {			
			PreparedStatement s = connection.prepareStatement("SELECT COUNT(*) AS Number FROM earthquakes WHERE mag >= ?;");
			s.setDouble(1, mag);
			ResultSet rs = s.executeQuery();
			//Iterate over results
			while (rs.next()) {
				result = rs.getInt(rs.findColumn("Number"));
			}
		}
		catch (SQLException sqle) {
			error(sqle);
			
		}
		return result;
	}
	
	
	
	/**
	 * Return the number of earthquakes bt Year and magnitude  in the database  
	 * Here, 'getNumberOfEarthquakesByYearMagnitude'is the method to retrieve Earthquakes  by Year & Magnitude 
	 */
	public ResultSet getNumberOfEarthquakesByYearMagnitude(Double mag, String year) {
		int result = 0;
		
		try {
            
			PreparedStatement s= connection.prepareStatement("SELECT * From earthquakes Where time Like ? And mag>= ? Order by time ASC");
			
			s.setString(1, year + "%");
			s.setDouble(2, mag);
			ResultSet rs= s.executeQuery();
			return rs;
		}
		catch (SQLException sqle) {
			error(sqle);
		}
		return null;
		
	}
	/**
	 * Return the quakes by location  in the database 
	 * Here, 'getquakesByLocation'is the method to retrieve Earthquakes  by Year & Magnitude 
	 */
	public ResultSet getquakesByLocation(Double latitude, Double longitude, Double mag) {
		//int result = 0;
		try {			
			PreparedStatement s = connection.prepareStatement("SELECT * \r\n"
					+ "FROM earthquakes\r\n"
					+ "WHERE\r\n"
					+ " mag >= ?\r\n"
					+ "ORDER BY\r\n"
					+ " (\r\n"
					+ " ((? - Latitude) * (? - Latitude)) + \r\n"
					+ " (0.595 * ((? - Longitude) * (? - Longitude)))\r\n"
					+ " ) \r\n"
					+ " ASC\r\n"
					+ "LIMIT 10;\r\n"
					+ "");
			s.setDouble(1, mag);
			s.setDouble(2, latitude);
			s.setDouble(3, latitude);
			s.setDouble(4, longitude);
			s.setDouble(5, longitude);
			ResultSet rs = s.executeQuery();
			
			
			return rs;
		}
		catch (SQLException sqle) {
			error(sqle);
			
		}
		return null;
	}
	
	/**
	 * Closes the connection to the database, required by AutoCloseable interface.
	 */
	@Override
	public void close() {
		try {
			if ( !connection.isClosed() ) {
				connection.close();
			}
		}
		catch(SQLException sqle) {
			error(sqle);
		}
	}
	
	/**
	 * Prints out the details of the SQL error that has occurred, and exits the programme
	 * @param sqle Exception representing the error that occurred
	 */
	private void error(SQLException sqle) {
		System.err.println("Problem Accessing Database! " + sqle.getClass().getName());
		sqle.printStackTrace();
		System.exit(1);
	}
	
	


}
