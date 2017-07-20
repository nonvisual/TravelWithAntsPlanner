package de.in.tum.dss.optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.apache.log4j.Logger;

import de.in.tum.dss.model.DBManager;
import de.in.tum.dss.model.Site;
import de.in.tum.dss.model.Site.Category;
import de.in.tum.dss.model.Travel;

/**
 * Optimizer, which implements one of the version of Ant Colony optimization
 * algorithm
 * 
 * @author nonvi
 *
 */
public class AntColonyOptimizer implements TravelOptimizer {

	private Map<Site, Double> pheromones;
	private double pheromoneFadeSpeed = 0.9;
	private double algorithmGreed = 0.5;
	private double lookUpTimeRange = 25;
	private int threadsNumber = 4;

	public static final Logger LOG = Logger.getLogger(AntColonyOptimizer.class);

	public Travel findBestRoute(double longitude, double latitude, double timeBudget, double timeOut) {
		// initialize pheromones
		pheromones = new HashMap<Site, Double>();
		List<Site> allSites = DBManager.INSTANCE.loadAllSites();
		for (Site site : allSites) {
			pheromones.put(site, 1.0);
		}

		final Site startSite = new Site("Starting Point", false, latitude, longitude, Category.Cultural, "Unknown");

		// find the closest point
		double smallestDistance = Double.MAX_VALUE;
		Site closest = null;
		for (Site site : allSites) {
			if (site.distanceToSite(startSite) < smallestDistance) {
				smallestDistance = site.distanceToSite(startSite);
				closest = site;
			}
		}
		final Site closestSite = closest;

		// if no enough time to visit even the closest destinations, return only
		// one point
		if (2 * smallestDistance + STAY_TIME > timeBudget) {
			List<Site> destinations = new ArrayList<Site>();
			destinations.add(startSite);
			return new Travel(destinations);
		}
		double startTime = System.currentTimeMillis() / 1000;

		ExecutorService ex = Executors.newFixedThreadPool(threadsNumber);
		BestRoute bestRoute = new BestRoute();
		while (System.currentTimeMillis() / 1000 - startTime < timeOut) {
			try {
//				LOG.info("Adding new thread to executor");
				ex.execute(new AntRun(startSite, closestSite, timeBudget,bestRoute));
			} catch (RejectedExecutionException e) {
				if (!ex.isShutdown())
					LOG.info("Task is rejected", e);
			}
		}ex.shutdown();

		return bestRoute.getBestRoute();
	}

	// updating pheromones after the ant's run
	private synchronized void updatePheromones(Travel currentRun, double timeBudget) {
		// all fade
		// TODO use lambda expressions
		for (Entry<Site, Double> entry : pheromones.entrySet()) {
			pheromones.replace(entry.getKey(), pheromoneFadeSpeed * entry.getValue());
		}

		// put pheromones on the path
		for (int i = 0; i < currentRun.getDestinations().size() - 2; i++) {
			Site current = currentRun.getDestinations().get(i);
			Site next = currentRun.getDestinations().get(i + 1);

			double distance = current.distanceToSite(next);
			double pheromoneIncrease = currentRun.getTotalScore() / distance;
			pheromones.replace(next, pheromones.get(next) + pheromoneIncrease);
		}

	}

	// chooses next site stochastically given the pheromones values
	private Site chooseNextSite(double timeLeft, List<Site> neighbors, Site currentSite, Site startSite) {

		// list of probabilities
		List<Double> probs = new ArrayList<Double>();
		double normalization = 0;
		for (Site site : neighbors) {
			double prob = Math.pow(1 / currentSite.distanceToSite(site), algorithmGreed)
					* Math.pow(pheromones.get(site), 1 - algorithmGreed);
			probs.add(prob);
			normalization += prob;
		}

		// normalize
		for (int i = 0; i < probs.size(); i++) {
			probs.set(i, probs.get(i) / normalization);
		}

		int index = chooseIndex(probs);
		// no unvisited places in lookUpRange
		if (index == -1)
			return null;

		// if can not go home after this node, return start site
		Site potentialSite = neighbors.get(index);
		if (potentialSite.distanceToSite(startSite) + STAY_TIME
				+ currentSite.distanceToSite(potentialSite) > timeLeft) {
			return startSite;
		}
		return potentialSite;
	}

	
	// chooses index using probabilities
	private int chooseIndex(List<Double> probs) {
		double draw = Math.random();
		double sum = 0;
		for (int i = 0; i < probs.size(); i++) {

			sum += probs.get(i);
			if (sum > draw)
				return i;
		}
		// if probs list is empty
		return -1;
	}

	/**
	 * 
	 * @return parameter with which pheromone value is decreasing
	 */
	public double getPheromoneFadeSpeed() {
		return pheromoneFadeSpeed;
	}

	/**
	 * 
	 * @param pheromoneFadeSpeed
	 *            pheromone decrease value
	 */
	public void setPheromoneFadeSpeed(double pheromoneFadeSpeed) {
		this.pheromoneFadeSpeed = pheromoneFadeSpeed;
	}

	/**
	 * 
	 * @return greed parameter of the ant colony algorithm
	 */
	public double getAlgorithmGreed() {
		return algorithmGreed;
	}

	/**
	 * 
	 * @param algorithmGreed
	 *            greed parameter of the ant colony algorithm
	 */
	public void setAlgorithmGreed(double algorithmGreed) {
		this.algorithmGreed = algorithmGreed;
	}

	/**
	 * 
	 * @return current hour range in which search for the next site will be done
	 */
	public double getLookUpTimeRange() {
		return lookUpTimeRange;
	}

	/**
	 * Sets look up time value. While the higher values do not restric
	 * optimality, the do decrease computation speed since we have fully
	 * connected graph.
	 * 
	 * @param lookUpTimeRange
	 */
	public void setLookUpTimeRange(double lookUpTimeRange) {
		this.lookUpTimeRange = lookUpTimeRange;
	}

	private class BestRoute {
		// guarded by this, immutable
		private Travel travel;

		public synchronized void updateBestRoute(Travel newTravel) {
			if (travel == null || newTravel.getTotalScore() > travel.getTotalScore()) {
				travel = newTravel;
			}
		}

		public synchronized Travel getBestRoute() {
			return travel;
		}
	}

	private class AntRun extends Thread {
		private Travel route;
		private Site startSite;
		private Site closestSite;
		private double timeBudget;
		private BestRoute bestRoute;
		
		public AntRun(Site startSite, Site closestSite, double timeBudget, BestRoute bestRoute) {
			super();
			this.startSite = startSite;
			this.closestSite = closestSite;
			this.timeBudget = timeBudget;
			this.bestRoute=bestRoute;
		}
		
		@Override
		public void run() {
			start();
		}

		@Override
		public void start() {
			LOG.info("Ant run started ");
			System.out.println("Ant started");
			List<Site> destinations = new ArrayList<Site>();
			destinations.add(startSite);
			destinations.add(closestSite);

			double timeLeft = timeBudget - STAY_TIME - closestSite.distanceToSite(startSite);
			Site currentSite = closestSite;
			Site nextSite = null;
			do {
				List<Site> neighbors = DBManager.INSTANCE.getNeighborSites(lookUpTimeRange, currentSite);
				neighbors.removeAll(destinations);
				nextSite = chooseNextSite(timeLeft, neighbors, currentSite, startSite);
				if (nextSite == null) {
					// increased lookup range
					neighbors = DBManager.INSTANCE.getNeighborSites(100, currentSite);
					neighbors.removeAll(destinations);
					nextSite = chooseNextSite(timeLeft, neighbors, currentSite, startSite);
				}
				destinations.add(nextSite);
				timeLeft -= (nextSite.distanceToSite(currentSite) + STAY_TIME);
				currentSite = nextSite;
			} while (nextSite != startSite);

			// after run count TravelScore
			route = new Travel(destinations);
			LOG.info("Ant run thread " + route.getTotalScore());
			updatePheromones(route, timeBudget);
			bestRoute.updateBestRoute(route);

		}

	}

}
