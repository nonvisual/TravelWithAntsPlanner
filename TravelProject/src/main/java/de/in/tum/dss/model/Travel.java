package de.in.tum.dss.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.Immutable;

import de.in.tum.dss.optimization.TravelOptimizer;
/**
 * The class is immutable
 * @author nonvi
 *
 */
@Immutable
public final class Travel{
	private final List<Site> destinations;
	private final double timeSpent;
	private final double totalScore;

	public Travel(List<Site> destinations) {
		super();
		this.destinations = Collections.unmodifiableList(destinations);
		this.totalScore = computeTotalScore();
		double timeSpent = 0;
		for (int i = 0; i < destinations.size() - 1; i++) {
			timeSpent += TravelOptimizer.STAY_TIME + destinations.get(i).distanceToSite(destinations.get(i + 1));
		}

		// one stay less
		this.timeSpent = timeSpent- TravelOptimizer.STAY_TIME;
	}

	public synchronized List<Site> getDestinations() {
		return destinations;
	}

	public synchronized double getTimeSpent() {
		return timeSpent;
	}

	/**
	 * Each visited world heritage site counts for 1 point Each country you
	 * visit counts for 2 points Each site that is listed as endangered counts
	 * for 3 points
	 * 
	 * @return total score for the travel
	 */
	public synchronized double getTotalScore() {
		return totalScore;
	}

	private double computeTotalScore() {
		Set<String> countries = new HashSet<String>();
		Set<Site> unique = new HashSet<>();
		double score = 0;
		for (Site site : destinations) {
			unique.add(site);
			if (site.isEndangered()) {
				score += 3;
			}
			countries.add(site.getCountry());
		}
		score +=( countries.size()-1) * 2; // minus start point
		score+=(unique.size()-1); // only unique sites minus start point
		return score;
	}

	
}
