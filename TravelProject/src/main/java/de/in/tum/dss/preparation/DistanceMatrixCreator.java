package de.in.tum.dss.preparation;

import java.util.ArrayList;
import java.util.List;

import de.in.tum.dss.model.DBManager;
import de.in.tum.dss.model.Site;

/**
 * Utility class, which creates a table with distances between sites 
 * @author nonvi
 *
 */
public class DistanceMatrixCreator {

	/**
	 * Data class for storing distances between sites
	 * @author nonvi
	 *
	 */
	public class Distance {
		private final Site site1;
		private final Site site2;
		private final double distance;

		public Distance(Site site1, Site site2, double distance) {
			super();
			this.site1 = site1;
			this.site2 = site2;
			this.distance = distance;
		}

		public Site getSite1() {
			return site1;
		}

		public Site getSite2() {
			return site2;
		}

		public double getDistance() {
			return distance;
		}

	}

	/**
	 * Creates a table in database with distance matrix
	 * 
	 * @param maxHour
	 *            maximum hour distance from node, which will be entered in
	 *            distance matrix. Needed to avoid large fully connected graph.
	 */
	public void createDistanceMatrix(double maxHour) {
		// load all sites
		List<Site> sites = DBManager.INSTANCE.loadAllSites();
		List<Distance> distances = new ArrayList<DistanceMatrixCreator.Distance>();

		for (int i = 0; i < sites.size()-1; i++) {
			for (int j = i+1; j < sites.size(); j++) {
				Site site1 = sites.get(i);
				Site site2 = sites.get(j);
				double distance = site1.distanceToSite(site2);

				if (distance < maxHour) {
					Distance siteDistance = new Distance(site1, site2, distance);
					distances.add(siteDistance);
				}
			}
		}

		DBManager.INSTANCE.saveDistances(distances);
	}

}
