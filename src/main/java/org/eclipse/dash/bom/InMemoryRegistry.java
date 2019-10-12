package org.eclipse.dash.bom;

import java.util.HashMap;
import java.util.Map;

public class InMemoryRegistry implements ContentRegistry {
	Map<ContentId, ContentInfo> registry = new HashMap<>();
	
	@Override
	public ContentInfo find(ContentId id) {
		return registry.get(id);
	}

	@Override
	public void cache(ContentInfo each) {
		registry.put(each.getId(), each);
	}

}
