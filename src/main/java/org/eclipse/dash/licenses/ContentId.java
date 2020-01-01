package org.eclipse.dash.licenses;

public class ContentId implements IContentId {

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
	}
	
	public static ContentId getContentId(String string) {
		String[] parts = string.split("\\/");
		if (parts.length != 5) throw new IllegalArgumentException("Ids must contain five parts");
		String type = parts[0];
		String source = parts[1];
		String namespace = parts[2];
		String name = parts[3];
		String version = parts[4];
		return new ContentId(type, source, namespace, name, version);
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

	@Override
	public String getNamespace() {
		return namespace;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getSource() {
		return source;
	}

	@Override
	public boolean isValid() {
		return true;
	}
}
