package de.in.tum.dss.optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.in.tum.dss.model.DBManager;
import de.in.tum.dss.model.Site;
import de.in.tum.dss.model.Travel;

/**
 * Optimizer, which implements one of the version of Ant Colony optimization
 * algorithm
 * 
 * @author nonvi
 *
 */
public class AntColonyOptimizer implements TravelOptimizer {

	private double lookUpTimeRange = 25;
	private Map<Site, Double> pheromones;
	private double pheromoneFadeSpeed = 0.9;
	private double algorithmGreed = 0.5;
	public static final Logger LOG = Logger.getLogger(AntColonyOptimizer.class);

	public Travel findBestRoute(double longitude, double latitude, double timeBudget, double timeOut) {
		// initialize pheromones
		pheromones = new HashMap<Site, Double>();
		List<Site> allSites = DBManager.INSTANCE.loadAllSites();
		for (Site site : allSites) {
			pheromones.put(site, 1.0);
		}

		Site startSite = new Site();
		startSite.setLatitude(latitude);
		startSite.setLongitude(longitude);
		startSite.setSiteName("Starting Point");
		startSite.setCountry("Unknown");
		Travel bestRoute = null;

		// find the closest point
		double smallestDistance = Double.MAX_VALUE;
		Site closestSite = null;
		for (Site site : allSites) {
			if (site.distanceToSite(startSite) < smallestDistance) {
				smallestDistance = site.distanceToSite(startSite);
				closestSite = site;
			}
		}
		// if no enough time to visit even the closest destinations, return only
		// one point
		if (2 * smallestDistance + STAY_TIME > timeBudget) {
			List<Site> destinations = new ArrayList<Site>();
			destinations.add(startSite);
			return new Travel(destinations);
		}
		double startTime = System.currentTimeMillis() / 1000;

		while (System.currentTimeMillis() / 1000 - startTime < timeOut) {
			// make new ant run
			List<Site> destinations = new ArrayList<Site>();
			destinations.add(startSite);
			destinations.add(closestSite);

			double timeLeft = timeBudget-STAY_TIME - closestSite.distanceToSite(startSite);
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
			Travel currentRun = new Travel(destinations);
			if (bestRoute == null || currentRun.getTotalScore() > bestRoute.getTotalScore()) {
				bestRoute = currentRun;
			}
			LOG.info("Ant runs, score: " + currentRun.getTotalScore() + ", time left "
					+ (System.currentTimeMillis() / 1000 - startTime) + " / " + timeOut);

			updatePheromones(currentRun, timeBudget);

		}
		return bestRoute;
	}

	// updating pheromones after the ant's run
	private void updatePheromones(Travel currentRun, double timeBudget) {
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

	// chooses next site stohasticaly given the pheromones values
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

}
