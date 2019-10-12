package org.eclipse.dash.bom;

public interface ContentRegistry {

	ContentInfo find(ContentId id);

	void cache(ContentInfo info);

}
