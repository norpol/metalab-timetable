package at.metalab.timetable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Monitor {
	private List<Departure> departures = new LinkedList<>();

	private Long lastUpdateServer = null;

	private long lastUpdate = 0;

	private String json;

	public Monitor() {
	}

	public Monitor(Monitor old) {
		this.departures.addAll(old.getDepartures());
		this.lastUpdate = old.lastUpdate;
		this.lastUpdateServer = old.lastUpdateServer;
	}

	public Monitor(List<Departure> departures, long lastUpdate,
			Long lastUpdateServer) {
		this.departures.addAll(departures);
		this.lastUpdate = lastUpdate;
		this.lastUpdateServer = lastUpdateServer;
	}

	public boolean isStale() {
		return lastUpdateServer == null
				|| lastUpdateServer.longValue() != lastUpdate;
	}

	public void setDepartures(List<Departure> departures) {
		this.departures = departures;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public void setLastUpdateServer(long lastUpdateServer) {
		this.lastUpdateServer = lastUpdateServer;
	}

	public List<Departure> getDepartures() {
		return departures;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public Long getLastUpdateServer() {
		return lastUpdateServer;
	}

	public void buildJson() {
		ObjectMapper om = new ObjectMapper();
		om.enable(SerializationFeature.INDENT_OUTPUT);

		Monitor monitor = this;

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("departures", monitor.getDepartures());
		data.put("tsLastUpdate", monitor.getLastUpdate());
		data.put("tsLastUpdateServer", monitor.getLastUpdateServer());
		data.put("stale", monitor.isStale());

		try {
			setJson(om.writeValueAsString(data));
		} catch (JsonProcessingException jsonProcessingException) {
			setJson("{ }");
		}
	}

	public String getJson() {
		return json;
	}

	private void setJson(String json) {
		this.json = json;
	}
}
