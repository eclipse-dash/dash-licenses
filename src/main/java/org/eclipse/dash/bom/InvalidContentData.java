package org.eclipse.dash.bom;

public class InvalidContentData implements IContentData {

	private IContentId id;

	public InvalidContentData(IContentId id) {
		this.id = id;
	}

	@Override
	public IContentId getId() {
		return id;
	}

	@Override
	public String getLicense() {
		return "";
	}

	@Override
	public int getScore() {
		return 0;
	}

	@Override
	public String getAuthority() {
		return null;
	}

}
