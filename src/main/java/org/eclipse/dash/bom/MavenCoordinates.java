package org.eclipse.dash.bom;

public class MavenCoordinates extends Coordinates {

	private String groupid;
	private String artifactid;
	private String version;

	public MavenCoordinates(String groupid, String artifactid, String version) {
		this.groupid = groupid;
		this.artifactid = artifactid;
		this.version = version;
	}

	@Override
	String getType() {
		return "maven";
	}

	@Override
	String getSource() {
		return "mavencentral";
	}

	@Override
	String getNamespace() {
		return groupid;
	}

	@Override
	String getName() {
		return artifactid;
	}

	@Override
	String getVersion() {
		return version;
	}

}
