package de.in.tum.dss.model;

import java.util.List;

import org.junit.Test;

import de.in.tum.dss.preparation.DistanceMatrixCreator;
import de.in.tum.dss.preparation.DistanceMatrixCreator.Distance;
import junit.framework.TestCase;

public class DBManagerTest extends TestCase {

	@Test
	public void testGetFishers() {
		DBManager manager = DBManager.INSTANCE;
		List<Site> sites = manager.loadAllSites();
		assertTrue(sites.size() == 1052);
	}

	@Test
	public void testGetNeighborSites() {
		DBManager manager = DBManager.INSTANCE;
		List<Site> sites = manager.loadAllSites();
		Site start = sites.get(0);
		List<Site> neighbors = manager.getNeighborSites(15, start);
		System.out.println(neighbors.size());
		assertTrue(neighbors.size() == 2);
	}

}
