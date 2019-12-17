package org.eclipse.dash.bom;

public abstract class Coordinates {

	public IContentId getId() {
		return new ContentId(getType(), getSource(), getNamespace(), getName(), getVersion());
	}

	abstract String getType();
	abstract String getSource();
	abstract String getNamespace();
	abstract String getName();
	abstract String getVersion();

}
