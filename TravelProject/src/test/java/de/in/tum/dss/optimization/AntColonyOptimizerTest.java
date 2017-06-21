package de.in.tum.dss.optimization;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import de.in.tum.dss.model.Travel;

public class AntColonyOptimizerTest {

	@Test
	public void testFindBestRoute1() throws IOException {
		AntColonyOptimizer optimizer = new AntColonyOptimizer();
		Travel travel = optimizer.findBestRoute(0, 0, 24, 15);

		// only possible to visit next Site, so +2 for country + 1 point for
		// site
		assertTrue(travel.getTotalScore() == 3.0);
		assertTrue(travel.getTimeSpent() < 24.0);
	}

	
	@Test
	public void testFindBestRoute2() throws IOException {
		AntColonyOptimizer optimizer = new AntColonyOptimizer();
		Travel travel = optimizer.findBestRoute(0, 0, 34, 10);

		// only possible to visit next Site, so +2 for country + 2 point for 2
		// sites
		System.out.println(travel.getTotalScore());

		assertTrue(travel.getTotalScore() == 4.0);
		assertTrue(travel.getTimeSpent() < 34.0);
	}

	
}
