package de.in.tum.dss.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import de.in.tum.dss.preparation.DistanceMatrixCreator.Distance;

/**
 * Singleton class responsible for hibernate-based dialog with DB
 * 
 * @author fux
 *
 */
public enum DBManager {
	INSTANCE;

	private int batchSize = 100;
	public static final Logger LOG = Logger.getLogger(DBManager.class);
	private SessionFactory factory;
	private ServiceRegistry serviceRegistry;

	private DBManager() {
		try {
			Configuration configuration = new org.hibernate.cfg.Configuration();
			configuration.configure();
			serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
			factory = configuration.buildSessionFactory(serviceRegistry);
		} catch (Throwable ex) {
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	/**
	 * Loads all sites from the database
	 * 
	 * @return
	 */
	public List<Site> loadAllSites() {
		Session session = null;
		List<Site> sites = null;
		try {
			session = factory.openSession();
			Transaction tx = session.getTransaction();
			tx.begin();
			sites = session.createQuery("SELECT site from Site as site ORDER BY id").list();
			tx.commit();
		} catch (Exception e) {
			LOG.error("Problem with retrieving list of sites \n");
			e.printStackTrace();
		} finally {
			if (session != null)
				session.close();
		}
		return sites;
	}

	/**
	 * Creates a distance matrix from list of distances (rewrites table in
	 * database)
	 * 
	 * @param distances
	 */
	public boolean saveDistances(List<Distance> distances) {
		Session session = null;

		try {
			session = factory.openSession();
			Transaction tx = session.getTransaction();
			tx.begin();
			session.createSQLQuery("DROP TABLE IF EXISTS distance").executeUpdate();
			session.createSQLQuery("CREATE TABLE distance(  `id` int(11) NOT NULL AUTO_INCREMENT,"
					+ "site1_id int(11) NOT NULL, site2_id int(11) NOT NULL, distance decimal (7,2), PRIMARY KEY (`id`));")
					.executeUpdate();
			tx.commit();

			tx = session.getTransaction();
			tx.begin();
			StringBuilder batchStatement = new StringBuilder();
			for (int i = 0; i < distances.size(); i++) {
				Distance dist = distances.get(i);
				BigDecimal value = new BigDecimal(dist.getDistance());
				value = value.setScale(2, RoundingMode.HALF_EVEN);
				session.createSQLQuery(
						"INSERT INTO distance (site1_id, site2_id, distance) VALUES(" + dist.getSite1().getId() + ", "
								+ dist.getSite2().getId() + ", " + value.doubleValue() + ");\n")
						.executeUpdate();
				session.createSQLQuery(
						"INSERT INTO distance (site1_id, site2_id, distance) VALUES(" + dist.getSite2().getId() + ", "
								+ dist.getSite1().getId() + ", " + value.doubleValue() + ");\n")
						.executeUpdate();
			}
			tx.commit();

		} catch (Exception e) {
			LOG.error("Problem with saving distances \n");
			e.printStackTrace();
			return false;
		} finally {
			if (session != null)
				session.close();
		}
		return true;
	}

	/**
	 * Returns sites which are in maxTime hours from the start site?
	 * 
	 * @param maxTime
	 * @return
	 */
	public List<Site> getNeighborSites(double maxTime, Site startSite) {
		Session session = null;
		List<Site> sites = null;
		try {
			session = factory.openSession();
			Transaction tx = session.getTransaction();
			tx.begin();
			SQLQuery query = session
					.createSQLQuery("SELECT * FROM site as site RIGHT JOIN distance ON distance.site1_id=site.id"
							+ " where site2_id=" + startSite.getId() + " and distance.distance< " + maxTime);
			query.addEntity(Site.class);
			sites = query.list();
			tx.commit();

		} catch (Exception e) {
			LOG.error("Problem with retrieving list of neighbors \n");
			e.printStackTrace();
		} finally {
			if (session != null)
				session.close();
		}
		return sites;
	}

	/**
	 * Returns map with neighbor sites which are in maxTime hours from the start
	 * site?
	 * 
	 * @param maxTime
	 * @return
	 */
	public Map<Site, List> getNeighborsMap(double maxTime) {
		Session session = null;
		List<Site> allSites = loadAllSites();
		Map<Site, List> result = new HashMap<Site, List>();

		try {
			session = factory.openSession();
			Transaction tx = session.getTransaction();
			tx.begin();
			for (Site site : allSites) {
				SQLQuery query = session
						.createSQLQuery("SELECT * FROM site as site RIGHT JOIN distance ON distance.site1_id=site.id"
								+ " where site2_id=" + site.getId() + " and distance.distance< " + maxTime);
				query.addEntity(Site.class);
				List<Site> neighbors = query.list();
				result.put(site, neighbors);
			}

			tx.commit();

		} catch (Exception e) {
			LOG.error("Problem with retrieving list of neighbors \n");
			e.printStackTrace();
		} finally {
			if (session != null)
				session.close();
		}
		return result;
	}
}
