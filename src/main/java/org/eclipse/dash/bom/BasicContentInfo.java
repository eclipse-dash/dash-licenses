package org.eclipse.dash.bom;

public class BasicContentInfo implements ContentInfo {

	private License license;
	private ContentId id;
	private String state;
	private int score;

	public BasicContentInfo(ContentId id, License license, String state, int score) {
		this.id = id;
		this.license = license;
		this.state = state;
		this.score = score;
	}

	public License getLicense() {
		return license;
	}

	public ContentId getId() {
		return id;
	}

	public String getState() {
		return state;
	}

	@Override
	public int getScore() {
		return score;
	}

}
