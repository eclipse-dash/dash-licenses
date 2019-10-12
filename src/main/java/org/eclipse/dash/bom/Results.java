package org.eclipse.dash.bom;

import java.util.Collections;
import java.util.List;

public interface Results {

	public static Results EmptyResult = new Results() {

		@Override
		public List<ContentInfo> getAll() {
			return Collections.emptyList();
		}

		@Override
		public ContentInfo bestMatch() {
			return null;
		}
		
	};

	List<ContentInfo> getAll();

	ContentInfo bestMatch();

}
