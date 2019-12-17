package org.eclipse.dash.bom;

import java.util.Optional;

public interface ContentIdParser {

	public Optional<IContentId> parseId(String input);

}
