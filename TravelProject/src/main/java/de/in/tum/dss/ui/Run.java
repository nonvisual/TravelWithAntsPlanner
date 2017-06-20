package de.in.tum.dss.ui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import de.in.tum.dss.model.DBManager;
import de.in.tum.dss.model.Travel;
import de.in.tum.dss.optimization.AntColonyOptimizer;
import de.in.tum.dss.optimization.TravelOptimizer;
import de.in.tum.dss.preparation.DistanceMatrixCreator;
import de.in.tum.dss.preparation.GMapsHTML;

/**
 * Main entry for the app
 * @author nonvi
 *
 */
public class Run {

	public static void main(String[] args) {
		
		if(args.length>0){
			double lookUpRange = Double.parseDouble(args[0]);
			System.out.println("Creating distance matrix with connections up to " + lookUpRange + " hours");
			DistanceMatrixCreator creator = new DistanceMatrixCreator();
			creator.createDistanceMatrix(lookUpRange);
		}
		TravelOptimizer optimizer = new AntColonyOptimizer();

		System.out.println("Welcome to the Travel with Ants planner. Please enter your starting point latitude:");
		double latitude = 0;
		double longitude = 0;
		double hours = 0;
		double timeOut = 0;
		String exportPath = "";
		try {
			String input = System.console().readLine();
			latitude = Double.parseDouble(input);
		} catch (Exception e) {
			System.out.println("Incorrect input for latitude");
			System.exit(0);
		}
		System.out.println("Enter longitude: ");
		try {
			String input = System.console().readLine();
			longitude = Double.parseDouble(input);
		} catch (Exception e) {
			System.out.println("Incorrect input for longitude");
			System.exit(0);
		}

		System.out.println("Enter your time budget (hours): ");
		try {
			String input = System.console().readLine();
			hours = Double.parseDouble(input);
		} catch (Exception e) {
			System.out.println("Incorrect input for time budget");
			System.exit(0);
		}

		System.out.println("Enter how long you want to wait (sec): ");
		try {
			String input = System.console().readLine();
			timeOut = Double.parseDouble(input);
		} catch (Exception e) {
			System.out.println("Incorrect input for time budget");
			System.exit(0);
		}

		System.out.println("Enter a destination for export file (skip if not needed):");
		try {
			exportPath = System.console().readLine();
		} catch (Exception e) {
			System.out.println("Incorrect input for path");
			System.exit(0);
		}

		Travel travel = optimizer.findBestRoute(longitude, latitude, hours, timeOut);
		System.out.println("Your path is computed. \nIt contains: " + travel.getDestinations().size()
				+ " destinations \nScore: " + travel.getTotalScore());
		if (!exportPath.equals("")) {
			GMapsHTML maps = new GMapsHTML();
			try {
				maps.generateHTML(exportPath, travel.getDestinations());
				File htmlFile = new File(exportPath);
				Desktop.getDesktop().browse(htmlFile.toURI());
			} catch (IOException e) {
				System.out.println("Export was unsuccessfull");
				e.printStackTrace();
			}
		}
	}
}
