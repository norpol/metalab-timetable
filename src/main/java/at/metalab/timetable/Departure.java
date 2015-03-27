package at.metalab.timetable;

public class Departure {

	private String linieId;

	private String linie;

	private String direction;

	private String towards;

	private String countdown;

	public String getLinieId() {
		return linieId;
	}

	public void setLinieId(String linieId) {
		this.linieId = linieId;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public void setTowards(String towards) {
		this.towards = towards;
	}

	public String getTowards() {
		return towards;
	}

	public String getCountdown() {
		return countdown;
	}

	public void setCountdown(String countdown) {
		this.countdown = countdown;
	}

	public void setLinie(String linie) {
		this.linie = linie;
	}

	public String getLinie() {
		return linie;
	}

}
