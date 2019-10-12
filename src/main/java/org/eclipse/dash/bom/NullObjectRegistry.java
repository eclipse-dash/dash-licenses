package org.eclipse.dash.bom;

public class NullObjectRegistry implements ContentRegistry {

	@Override
	public ContentInfo find(ContentId id) {
		return null; // TODO EmptyResult.getInstance() ?
	}

	@Override
	public void cache(ContentInfo each) {
		// TODO Auto-generated method stub
		
	}

}
