package com.oose2016.group4.server;

import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;
import org.sqlite.SQLiteDataSource;

import spark.Spark;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MapQuestHandler.class, CrimeAPIHandler.class})
public class ServerTest {
	
	private final Logger logger = LoggerFactory.getLogger(ServerTest.class);
	private final String TESTCRIMES = "TestCrimes";
	
	SQLiteDataSource dSource;
	// ------------------------------------------------------------------------//
	// Setup - based on To-Do server unit tests.
	// ------------------------------------------------------------------------//

	/**
	 * Clears the database of any test tables we may have created and initiates
	 * testing.
	 * @throws Exception
	 */
	@Before
	public void setup() throws Exception {
		// Clear the database and then start the server
		dSource = clearDB();

		// Start the main server
		Bootstrap.main(null);
		Spark.awaitInitialization();
	}
	
	/**
	 * Clears the database of testing tables and stops the tests
	 */
	@After
	public void tearDown() {
		// Stop the server
		clearDB();
		Spark.stop();
	}

	// ------------------------------------------------------------------------//
	// Tests
	// ------------------------------------------------------------------------//

	/**
	 * Tests the getters in the Coordinate class.
	 * @throws Exception when the coordinates are invalid
	 */
	@Test
	public void testGetLatLong() throws Exception {
		Coordinate c = new Coordinate(0.5, 0.7);
		assertEquals(0.5, c.getLatitude(), 0);
		assertEquals(0.7, c.getLongitude(), 0);
	}
	
	/**
	 * Tests the sortAndExpand method in AvoidLinkIds.
	 * @throws Exception
	 */
	@Test
	public void testSortAndExpand() throws Exception {
		//c1 < c2
		Coordinate c1 = new Coordinate(0.5, 0.7);
		Coordinate c2 = new Coordinate(0.9, 1.1);
		Coordinate.sortAndExpand(c1, c2);
		assertEquals(0.49, c1.getLatitude(), 0);
		assertEquals(0.6886, c1.getLongitude(), 0.01);
		assertEquals(0.91, c2.getLatitude(), 0);
		assertEquals(1.11608, c2.getLongitude(), 0.01);

		//c3 > c4
		Coordinate c3 = new Coordinate(0.9, 1.1);
		Coordinate c4 = new Coordinate(0.5, 0.7);
		Coordinate.sortAndExpand(c3, c4);
		assertEquals(0.49, c3.getLatitude(), 0);
		assertEquals(0.6886, c3.getLongitude(), 0.01);
		assertEquals(0.91, c4.getLatitude(), 0);
		assertEquals(1.11608, c4.getLongitude(), 0.01);
		
		//c5.lat < c6.lat; c5.long > c6.long
		Coordinate c5 = new Coordinate(0.5, 1.1);
		Coordinate c6 = new Coordinate(0.9, 0.7);
		Coordinate.sortAndExpand(c5, c6);
		assertEquals(0.49, c5.getLatitude(), 0);
		assertEquals(0.6886, c5.getLongitude(), 0.01);
		assertEquals(0.91, c6.getLatitude(), 0);
		assertEquals(1.11608, c6.getLongitude(), 0.01);
		
		//c7.lat > c8.lat; c5.long < c6.long
		Coordinate c7 = new Coordinate(0.9, 0.7);
		Coordinate c8 = new Coordinate(0.5, 1.1);
		Coordinate.sortAndExpand(c7, c8);
		assertEquals(0.49, c7.getLatitude(), 0);
		assertEquals(0.6886, c7.getLongitude(), 0.01);
		assertEquals(0.91, c8.getLatitude(), 0);
		assertEquals(1.11608, c8.getLongitude(), 0.01);
	}

	/**
	 * Tests the getAvoidLnkIds method in AvoidLinkIds.
	 * @throws Exception
	 */
	@Test
	public void testGetAvoidLinkIds() throws Exception {
		SurvivalService s = new SurvivalService(dSource);
		
		try (Connection conn = s.getDb().open()){
			String sql1 = "CREATE TABLE IF NOT EXISTS " + TESTCRIMES 
					+ " (date INTEGER NOT NULL, linkId INTEGER NOT NULL, address TEXT NOT NULL, "
					+ "latitude REAL NOT NULL, longitude REAL NOT NULL, "
					+ "type TEXT, PRIMARY KEY (date, linkId, type));";
			conn.createQuery(sql1).executeUpdate();
			
			int date = 0, linkid = 0;
			String address = "", type = "";
			double latitude = 0, longitude = 0;
			
			String sql2 = " INSERT INTO " + TESTCRIMES
                    + " VALUES(:date, :linkid, :address, :latitude, :longitude, :type); ";
			
			for (int i = 0; i < 60; i++) {
				Crime c = new Crime(date, address, type, latitude, longitude, linkid);
				conn.createQuery(sql2).bind(c).executeUpdate();
				
				latitude++;
				longitude++;
			}
			
			linkid = 1;
					
			for (int i = 0; i < 40; i++) {
				Crime c = new Crime(date, address, type, latitude, longitude, linkid);
				conn.createQuery(sql2).bind(c).executeUpdate();
				
				latitude++;
				longitude++;
			}
			
			linkid = 2;
			
			for (int i = 0; i < 20; i++) {
				Crime c = new Crime(date, address, type, latitude, longitude, linkid);
				conn.createQuery(sql2).bind(c).executeUpdate();
				
				latitude++;
				longitude++;
			}
			
			Coordinate from = new Coordinate(0, 0);
			Coordinate to = new Coordinate(120, 120);
			
			int[] red = s.getAvoidLinkIds(from, to, TESTCRIMES).getRed();
			int[] yellow = s.getAvoidLinkIds(from, to, TESTCRIMES).getYellow();
		
			int[] redTarget = {0};
			int[] yellowTarget = {1}; 
	
			assertTrue(Arrays.equals(red, redTarget));
			assertTrue(Arrays.equals(yellow, yellowTarget));
			
			Coordinate from1 = null;
			Coordinate to1 = null;
			assertEquals(s.getAvoidLinkIds(from1, to1, TESTCRIMES), null);
		} catch (Sql2oException e) {
			logger.error("Failed to get avoid linkIds in ServerTest", e);
		} catch (Exception e) {
			logger.error("Failed to create Coordinate", e);
		}
	}
	
	/**
	 * Test getting getCrimes method within a specific range of coordinates from the
	 * database.
	 */
	@Test
	public void testGetCrimes() {
		SurvivalService s = new SurvivalService(dSource);
		
		try (Connection conn = s.getDb().open()){
			String sql1 = "CREATE TABLE IF NOT EXISTS TestCrimes "
					+ "(date INTEGER NOT NULL, linkId INTEGER NOT NULL, address TEXT NOT NULL, "
					+ "latitude REAL NOT NULL, longitude REAL NOT NULL, "
					+ "type TEXT, PRIMARY KEY (date, linkId, type));";
			conn.createQuery(sql1).executeUpdate();
			
			List<Crime> crimeList = new LinkedList<>();
			
			// the only ones that should return
			crimeList.add(new Crime(20, "a2", "type2", 200, 200, 1));
			crimeList.add(new Crime(30, "a3", "type3", 300, 300, 2));
			crimeList.add(new Crime(40, "a4", "type4", 400, 400, 3));
			
			// latitude or longitude should be too small
			crimeList.add(new Crime(20, "a1", "type1", 100, 200, 4));
			crimeList.add(new Crime(20, "a12", "type12", 200, 100, 5));			
			
			// latitude or longitude should be too big
			crimeList.add(new Crime(40, "a5", "type5", 500, 400, 6));
			crimeList.add(new Crime(40, "a54", "type54", 400, 500, 7));
			
			// date is too small or too big
			crimeList.add(new Crime(10, "a2", "type2", 200, 200, 8));
			crimeList.add(new Crime(50, "a2", "type2", 200, 200, 9));
			
			for (Crime c : crimeList) {
				String sql = "insert into TestCrimes(date, linkId, address, latitude, longitude, type) "
						+ "values (:dateParam, :linkIdParam, :addressParam, :latitudeParam, :longitudeParam, :typeParam)";

				Query query = conn.createQuery(sql);
				query.addParameter("dateParam", c.getDate()).addParameter("linkIdParam", c.getLinkId())
					.addParameter("addressParam", c.getAddress()).addParameter("latitudeParam", c.getLat())
					.addParameter("longitudeParam", c.getLng()).addParameter("typeParam", c.getType())
					.executeUpdate();
			}
			
			double fromLng = 200;
			double toLng = 400;
			double fromLat = 200;
			double toLat = 400;
			int fromDate = 20;
			int toDate = 40;
			int timeOfDay = 1000;
			
			Crime from = new Crime(fromDate, fromLat, fromLng);
			Crime to = new Crime(toDate, toLat, toLng);
			List<Crime> crimes = s.getCrimes(from, to, timeOfDay, "TestCrimes");
			
			crimes.forEach(crime -> {
				System.out.println(crime);
				assertTrue(crime.getLat() >= fromLat && crime.getLat() <= toLat
				&& crime.getLng() >= fromLng && crime.getLng() <= toLng
				&& crime.getDate() >= fromDate && crime.getDate() <= toDate);
			});
		} catch (Sql2oException e) {
			logger.error("Failed to get crimes in ServerTest", e);
		}	
	}
		
	/**
	 * Tests getSafetyRating method in SurvivalService to make sure we get the right color rating.
	 */
	@Test
	public void testSafetyRating()  {
		SurvivalService s = new SurvivalService(dSource);
		try (Connection conn = s.getDb().open()){
			String table = "TestSafetyRating";
			
			String sqltable = "CREATE TABLE IF NOT EXISTS TestSafetyRating "
		            + "(x INTEGER NOT NULL, y INTEGER NOT NULL, linkId INTEGER NOT NULL, alarm REAL NOT NULL, AADT INTEGER NOT NULL, "
		            + " PRIMARY KEY (x, y));";
			conn.createQuery(sqltable).executeUpdate();
			
			List<Grid> grids = new LinkedList<>();
			
			// TODO create a bunch of grids in this grid list to add to the test table, figure out what needs to be asserted
			
			String sqlInsertCoordinate = "INSERT INTO TestSafetyRating "
                    + " VALUES(:xParam, :yParam, :linkidParam, :alarmParam, :aadtParam); ";
			
			grids.forEach(grid ->
	            conn.createQuery(sqlInsertCoordinate).bind(grid).executeUpdate()
			);
			
			try {
				s.getSafetyRating(new Coordinate(39.5, 76.5), table);
				
			} catch (Exception e) {
				logger.error("Failed to get safety rating", e);
			}
		}
	}
	
	// ------------------------------------------------------------------------//
	// Survival Maps Specific Helper Methods and classes
	// ------------------------------------------------------------------------//
	
	/**
	 * Clears the database of all test tables.
	 * @return the clean database source
	 */
	private SQLiteDataSource clearDB() {
		SQLiteDataSource dataSource = new SQLiteDataSource();
		dataSource.setUrl("jdbc:sqlite:server.db"); 

		Sql2o db = new Sql2o(dataSource);
		
		try (Connection conn = db.open()) {
			String sql = "DROP TABLE IF EXISTS TestCrimes";
			conn.createQuery(sql).executeUpdate();
			String sql2 = "DROP TABLE IF EXISTS TestSafetyRating";
			conn.createQuery(sql2).executeUpdate();
		}
	
		return dataSource;
	}
}
