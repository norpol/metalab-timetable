package at.metalab.timetable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class WienerLinien {

	private final static Logger LOG = Logger.getLogger(WienerLinien.class
			.getName());

	private String domain;

	private String key;

	public WienerLinien(String key) {
		this.domain = "https://www.wienerlinien.at/ogd_realtime";
		this.key = key;
	}

	public List<Departure> queryMonitor(QueryMonitor abfahrten)
			throws IOException {
		StringBuilder s = new StringBuilder();
		for (String rbl : abfahrten.getRbls()) {
			if (s.length() != 0) {
				s.append("&");
			}

			s.append("rbl=");
			s.append(rbl);
		}

		HttpURLConnection urlConnection = (HttpURLConnection) createURLConnection(
				"monitor", s.toString());

		urlConnection.addRequestProperty("Accept", "application/json");
		urlConnection.addRequestProperty("Content-Type", "application/json");

		urlConnection.connect();

		ObjectMapper om = new ObjectMapper();
		JsonNode root = om.readTree(urlConnection.getInputStream());

		JsonNode monitors = root.path("data").path("monitors"); // array
		List<Departure> departures = new LinkedList<>();
		for (int i = 0; i < monitors.size(); i++) {
			departures.addAll(processMonitor(monitors.get(i)));
		}

		om.enable(SerializationFeature.INDENT_OUTPUT);

		LOG.info(om.writeValueAsString(root));

		return departures;
	}

	public List<Departure> selectNext(List<Departure> departures, String linie,
			String direction, int count) {
		List<Departure> selected = new LinkedList<>();

		for (Departure departure : departures) {
			if (departure.getLinie().equals(linie)
					&& departure.getDirection().equals(direction)) {
				selected.add(departure);
			}

			if (selected.size() >= count) {
				break;
			}
		}

		return selected;
	}

	private List<Departure> processMonitor(JsonNode monitorNode) {
		JsonNode linesNode = monitorNode.path("lines");

		List<Departure> departures = new LinkedList<>();
		for (int i = 0; i < linesNode.size(); i++) {
			departures.addAll(processLine(linesNode.get(i)));
		}

		return departures;
	}

	private List<Departure> processLine(JsonNode lineNode) {
		String name = lineNode.path("name").asText();
		String towards = lineNode.path("towards").asText();

		JsonNode departuresNode = lineNode.path("departures");

		List<Departure> departures = new LinkedList<>();
		departures.addAll(processDepartures(departuresNode.path("departure")));

		for (Departure departure : departures) {
			if (departure.getLinieId() == null
					|| departure.getLinieId().isEmpty()) {
				departure.setLinieId(lineNode.path("lineId").asText());
			}
			if (departure.getTowards() == null
					|| departure.getTowards().isEmpty()) {
				departure.setTowards(towards);
			}
			if (departure.getDirection() == null
					|| departure.getDirection().isEmpty()) {
				departure.setDirection(lineNode.path("direction").asText());
			}
			departure.setLinie(name);
		}

		return departures;
	}

	private List<Departure> processDepartures(JsonNode departureNode) {
		List<Departure> departures = new LinkedList<>();
		for (int i = 0; i < departureNode.size(); i++) {
			departures.add(processDeparture(departureNode.get(i)));
		}

		return departures;
	}

	private Departure processDeparture(JsonNode departureNode) {
		JsonNode departureTime = departureNode.path("departureTime");

		Departure departure = new Departure();
		departure.setCountdown(departureTime.path("countdown").asText());

		if (departureNode.path("vehicle") != null) {
			if (departureNode.path("direction") != null) {
				departure.setDirection(departureNode.path("vehicle")
						.path("direction").asText());
			}

			if (departureNode.path("towards") != null) {
				departure.setTowards(departureNode.path("vehicle")
						.path("towards").asText());
			}
		}

		return departure;
	}

	private URLConnection createURLConnection(String path, String params)
			throws MalformedURLException, IOException {
		String sender = String.format("sender=%s", key);

		URLConnection urlConnection = new URL(String.format("%s/%s?%s&%s",
				domain, path, sender, params)).openConnection();

		LOG.fine("retrieving "
				+ urlConnection.getURL().toString()
						.replace(sender, "sender=its_a_secret_to_everyone"));

		return urlConnection;
	}

}
