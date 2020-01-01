package org.eclipse.dash.licenses;

import javax.json.JsonObject;

import org.eclipse.dash.licenses.LicenseSupport.Status;

public class FoundationData implements IContentData {

	/**
	 * This field holds data that presents a single unit of content answered by a
	 * query to the Eclipse Foundation. The value is in JSON format as in this
	 * example:
	 * 
	 * <pre>
	 * {
	 *       "authority": "CQ7766", 
	 *       "confidence": "100", 
	 *       "definitionUrl": null, 
	 *       "id": "maven/mavencentral/com.google.guava/guava/15.0", 
	 *       "license": "Apache-2.0", 
	 *       "sourceUrl": null, 
	 *       "status": "approved"
	 *   }
	 * </pre>
	 */
	private JsonObject data;

	public FoundationData(JsonObject data) {
		this.data = data;
	}

	@Override
	public IContentId getId() {
		return ContentId.getContentId(data.getString("id"));
	}

	@Override
	public String getLicense() {
		return data.getString("license");
	}

	@Override
	public int getScore() {
		return data.getInt("confidence");
	}
	
	@Override
	public Status getStatus() {
		if ("approved".equals(data.getString("status"))) 
			return LicenseSupport.Status.Approved;
		return LicenseSupport.Status.Restricted;
	}

	@Override
	public String getAuthority() {
		return data.getString("authority");
	}
}
