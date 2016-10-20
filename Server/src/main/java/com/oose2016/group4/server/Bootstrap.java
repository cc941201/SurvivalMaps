//-------------------------------------------------------------------------------------------------------------//
// Code based on a tutorial by Shekhar Gulati of SparkJava at
// https://blog.openshift.com/developing-single-page-web-applications-using-java-8-spark-mongodb-and-angularjs/
//-------------------------------------------------------------------------------------------------------------//

package com.oose2016.group4.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;

import static spark.Spark.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Bootstrap {
	public static final String IP_ADDRESS = "0.0.0.0";
	public static final int PORT = 8080;

	private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

	public static void main(String[] args) throws Exception {
		// Check if the database file exists in the current directory. Abort if
		// not
		DataSource dataSource = configureDataSource();
		if (dataSource == null) {
			System.out.printf("Could not find server.db in the current directory (%s). Terminating\n",
					Paths.get(".").toAbsolutePath().normalize());
			System.exit(1);
		}

		// Specify the IP address and Port at which the server should be run
		ipAddress(IP_ADDRESS);
		port(PORT);

		// Create the model instance and then configure and start the web
		// service
		new SurvivalController(new SurvivalService(dataSource));
	}

	/**
	 * Check if the database file exists in the current directory. If it does
	 * create a DataSource instance for the file and return it.
	 * 
	 * @return javax.sql.DataSource corresponding to the server database
	 */
	private static DataSource configureDataSource() {
		Path serverPath = Paths.get(".", "server.db");
		if (!(Files.exists(serverPath))) {
			try {
				Files.createFile(serverPath);
			} catch (java.io.IOException ex) {
				logger.error("Failed to create server.db file in current directory. Aborting");
			}
		}

		SQLiteDataSource dataSource = new SQLiteDataSource();
		dataSource.setUrl("jdbc:sqlite:server.db");
		return dataSource;
	}
}