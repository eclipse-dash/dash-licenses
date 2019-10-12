package org.eclipse.dash.bom;

public class LicenseFinder {

	private ContentRegistry registry;
	private ContentResolver resolver;

	public LicenseFinder(ContentRegistry registry, ContentResolver resolver) {
		this.registry = registry;
		this.resolver = resolver;		
	}
	
	public ContentInfo findLicenseInformation(ContentId id) {
		ContentInfo info = registry.find(id);
		if (info != null) return info;
		
		Results results = resolver.resolve(id);
		for(ContentInfo each : results.getAll()) {
			registry.cache(each);
		}
		
		return results.bestMatch();
	}

}
