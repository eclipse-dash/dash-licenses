package org.eclipse.dash.bom;

public class ContentId {

	private String type;
	private String source;
	private String namespace;
	private String name;
	private String version;

	public ContentId(String type, String source, String namespace, String name, String version) {
		this.type = type;
		this.source = source;
		this.namespace = namespace;
		this.name = name;
		this.version = version;
		// TODO Auto-generated constructor stub
	}
	
	public ContentId(String string) {
		String[] parts = string.split("\\/");
		if (parts.length != 5) throw new IllegalArgumentException("Ids must contain five parts");
		this.type = parts[0];
		this.source = parts[1];
		this.namespace = parts[2];
		this.name = parts[3];
		this.version = parts[4];
	}

	@Override
	public String toString() {
		return type + "/" + source + "/" + namespace + "/" + name + "/" + version;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ContentId) {
			return toString().equals(obj.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
