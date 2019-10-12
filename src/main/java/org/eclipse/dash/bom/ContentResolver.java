package org.eclipse.dash.bom;

public interface ContentResolver {

	/**
	 * Find information regarding the content with the given ID.
	 * 
	 * This method always returns a value.
	 * 
	 * @param id
	 * @return an instance of {@link Results} or {@link Results#EmptyResult}.
	 */
	Results resolve(ContentId id);

}
