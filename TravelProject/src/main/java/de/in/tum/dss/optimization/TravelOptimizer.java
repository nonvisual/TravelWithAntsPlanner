package de.in.tum.dss.optimization;

import de.in.tum.dss.model.Travel;
/**
 * Main interface for solving travelling optimization problem 
 * @author nonvi
 *
 */
public interface TravelOptimizer {
	// constant, defining how long one need per each site
	public static final double STAY_TIME = 6;
	
	/**
	 * Computes the best travel route in specified timeout
	 * 
	 * @param longitude start point longitude
	 * @param latitude  start point latitude
	 * @param timeBudget total time one can spend for the travel
	 * @param timeOut time reserved for optimization algorithm run
	 * @return best route
	 */
	public Travel findBestRoute(double longitude, double latitude, double timeBudget, double timeOut);
	
}
