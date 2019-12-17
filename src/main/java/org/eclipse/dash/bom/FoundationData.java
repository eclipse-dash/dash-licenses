package org.eclipse.dash.bom;

import javax.json.JsonValue;

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
	private JsonValue data;

	public FoundationData(JsonValue data) {
		this.data = data;
	}

	@Override
	public IContentId getId() {
		return new ContentId(data.asJsonObject().getString("id"));
	}

	@Override
	public String getLicense() {
		return data.asJsonObject().getString("license");
	}

	@Override
	public int getScore() {
		return data.asJsonObject().getInt("confidence");
	}
}
