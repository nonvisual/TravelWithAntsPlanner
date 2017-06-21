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

public final class Site {
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

	public enum Category {
		Cultural, Natural, Mixed
	}

	private Site() {
		//default constructor for hibernate
	}
	public Site(String siteName, boolean isEndangered, double latitude, double longitude, Category category,
			String country) {
		super();
		this.siteName = siteName;
		this.isEndangered = isEndangered;
		this.latitude = latitude;
		this.longitude = longitude;
		this.category = category;
		this.country = country;
	}

	public int getId() {
		return id;
	}

	private void setId(int id) {
		this.id = id;
	}

	public String getSiteName() {
		return siteName;
	}

	private void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public boolean isEndangered() {
		return isEndangered;
	}

	private void setEndangered(boolean isEndangered) {
		this.isEndangered = isEndangered;
	}

	public double getLatitude() {
		return latitude;
	}

	private void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	private void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public Category getCategory() {
		return category;
	}

	private void setCategory(Category category) {
		this.category = category;
	}

	public String getCountry() {
		return country;
	}

	private void setCountry(String country) {
		this.country = country;
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
		return "Site: " + id + " name: " + siteName;
	}
}
