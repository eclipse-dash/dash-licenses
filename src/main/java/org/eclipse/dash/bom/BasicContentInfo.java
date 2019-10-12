package org.eclipse.dash.bom;

public class BasicContentInfo implements ContentInfo {

	private License license;
	private ContentId id;
	private String state;

	public BasicContentInfo(ContentId id, License license, String state) {
		this.id = id;
		this.license = license;
		this.state = state;
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

}
