package de.in.tum.dss.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "site")

public class Site {
	public static final double HELI_SPEED = 80; // km per hour
	public static final int R = 6371; // earth radius

	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "site_name")
	private String siteName;

	@Column(name = "is_endangered")
	private boolean isEndangered;

	@Column(name = "latitude")
	private double latitude;

	@Column(name = "longitude")
	private double longitude;

	@Column(name = "category")
	@Enumerated(EnumType.STRING)
	private Category category;

	@Column(name = "country")
	private String country;

	@Transient
	private double baseScore;

	@Transient
	private double pheromones;

	public enum Category {
		Cultural, Natural, Mixed
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public boolean isEndangered() {
		return isEndangered;
	}

	public void setEndangered(boolean isEndangered) {
		this.isEndangered = isEndangered;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public double getBaseScore() {
		return baseScore;
	}

	public void setBaseScore(double baseScore) {
		this.baseScore = baseScore;
	}

	public double getPheromones() {
		return pheromones;
	}

	public void setPheromones(double pheromones) {
		this.pheromones = pheromones;
	}

	/**
	 * Computes distance in hours to another site
	 * 
	 * @param anotherSite
	 * @return
	 */
	public double distanceToSite(Site anotherSite) {
		double latDistance = Math.toRadians(latitude - anotherSite.getLatitude());
		double lonDistance = Math.toRadians(longitude - anotherSite.getLongitude());
		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
				+ Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(anotherSite.getLatitude()))
						* Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c / HELI_SPEED;

		return distance;
	}

	public static double distance(double lat1, double lat2, double lon1, double lon2, double el1, double el2) {

		// Radius of the earth

		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);
		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c * 1000; // convert to meters

		double height = el1 - el2;

		distance = Math.pow(distance, 2) + Math.pow(height, 2);

		return Math.sqrt(distance);
	}

	@Override
	public int hashCode() {
		int result = id;
		result += 37 * result + siteName.hashCode();
		result += 37 * result + (isEndangered ? 1 : 0);
		result += 37 * result + latitude;
		result += 37 * result + longitude;
		result += 37 * result + country.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		Site another = null;
		if (obj instanceof Site) {
			another = (Site) obj;
		} else
			return false;
		if (another.id != id)
			return false;
		if (!another.getSiteName().equals(siteName))
			return false;
		if (another.latitude != latitude)
			return false;
		if (another.longitude != longitude)
			return false;
		if (!another.getCountry().equals(country))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Site: " + id + " name: " + siteName ;
	}
}
