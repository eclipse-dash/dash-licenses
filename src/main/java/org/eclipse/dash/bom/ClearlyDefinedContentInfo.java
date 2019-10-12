package org.eclipse.dash.bom;

import net.minidev.json.JSONObject;

public class ClearlyDefinedContentInfo implements ContentInfo {

	private JSONObject data;

	public ClearlyDefinedContentInfo(JSONObject data) {
		this.data = data;
	}

	@Override
	public License getLicense() {
		return new License(((JSONObject)data.get("licensed")).getAsString("declared"));
	}
	
	//@Override
	public Number getEffectiveScore() {
		return ((JSONObject)data.get("scores")).getAsNumber("effective");
	}

	@Override
	public ContentId getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getState() {
		// TODO Auto-generated method stub
		return null;
	}


}
