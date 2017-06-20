package de.in.tum.dss.preparation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import de.in.tum.dss.model.Site;

/**
 * Class for html file with map and route visualization (using Google Maps)
 * @author nonvi
 *
 */
public class GMapsHTML {
	private static final String TEMPLATE = "./src/main/resources/template.html";

	/**
	 * Generates html file for Google Maps at current path
	 * @param path where the file should be created
	 * @param destinations list of sites to visit
	 * @throws IOException
	 */
	public void generateHTML(String path, List<Site> destinations) throws IOException {
		InputStream in = getClass().getResourceAsStream("/template.html");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder builder = new StringBuilder();

		String sCurrentLine;
		while ((sCurrentLine = reader.readLine()) != null) {
			builder.append(sCurrentLine+"\n");
		}
		String contents = builder.toString();

		// generate javascript array
		builder = new StringBuilder();
		for (int i = 0; i < destinations.size(); i++) {
			Site site = destinations.get(i);
			builder.append("{ lat: " + site.getLatitude() + ", lng: " + site.getLongitude() + ", name: \""
					+ site.getSiteName() + "\" }");
			if (i != destinations.size() - 1) {
				builder.append(",");
			}
		}
		contents = contents.replace("var flightPlanCoordinates = [];",
				"var flightPlanCoordinates = [" + builder.toString() + "];");
		try (PrintWriter out = new PrintWriter(path)) {
			out.println(contents);
		}

	}
}
