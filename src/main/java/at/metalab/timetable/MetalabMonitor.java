package at.metalab.timetable;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

public class MetalabMonitor {

	private static Logger LOG = Logger
			.getLogger(MetalabMonitor.class.getName());

	private WienerLinien wienerLinien;
	private final QueryMonitor metalabQuery = new QueryMonitor("4205", "4210",
			"252", "269");

	private volatile Monitor state = new Monitor();

	public MetalabMonitor() {
		try {
			String senderId = IOUtils.toString(Thread.currentThread()
					.getContextClassLoader()
					.getResourceAsStream("senderId.txt")).replaceAll("\n", "");

			wienerLinien = new WienerLinien(senderId);
		} catch (IOException ioException) {
			LOG.severe("could not loader wiener linien key");
			throw new RuntimeException(ioException); // bail out for now
		} catch (NullPointerException nullPointerException) {
			LOG.severe("could not loader wiener linien key");
			throw nullPointerException; // bail out for now
		}

		new Thread() {

			public synchronized void start() {
				setName("MetalabMonitorUpdater");
				setDaemon(true);
				super.start();
			};

			public void run() {
				try {
					do {
						final long ts = System.currentTimeMillis();

						Monitor newState = new Monitor(state);

						try {
							List<Departure> currentDepartures = wienerLinien
									.queryMonitor(metalabQuery);

							List<Departure> selectedDepartures = new LinkedList<>();
							int maxCountDepartures = 2;

							selectedDepartures.addAll(wienerLinien.selectNext(
									currentDepartures, "2", "H",
									maxCountDepartures));
							selectedDepartures.addAll(wienerLinien.selectNext(
									currentDepartures, "2", "R",
									maxCountDepartures));

							selectedDepartures.addAll(wienerLinien.selectNext(
									currentDepartures, "U2", "H",
									maxCountDepartures));
							selectedDepartures.addAll(wienerLinien.selectNext(
									currentDepartures, "U2", "R",
									maxCountDepartures));

							newState.setLastUpdateServer(ts);
							newState.setDepartures(selectedDepartures);
						} catch (IOException ioException) {
							LOG.severe("could not update departures: "
									+ ioException.getMessage());
							// bad luck
						} finally {
							newState.setLastUpdate(ts);
							newState.buildJson();

							state = newState;

							MonitorSocket.setMonitor(newState);
						}

						Thread.sleep(1000 * 60); // wait 1 minute between
													// updates
					} while (true);
				} catch (InterruptedException interruptedException) {

				}
			};
		}.start();
	}

	public Monitor getMonitor() {
		return state;
	}
}
