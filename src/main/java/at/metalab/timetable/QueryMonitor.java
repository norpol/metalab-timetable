package at.metalab.timetable;

import java.util.HashSet;
import java.util.Set;

public class QueryMonitor {
	private Set<String> rbls = new HashSet<>();

	public QueryMonitor() {
	}
	
	public QueryMonitor(String ... rbls) {
		for(String rbl: rbls) {
			addRbl(rbl);
		}
	}
	
	public void addRbl(String rbl) {
		rbls.add(rbl);
	}
	
	public Set<String> getRbls() {
		return rbls;
	}
}
