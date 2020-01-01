package org.eclipse.dash.licenses;

import java.util.Optional;

public interface ContentIdParser {

	public Optional<IContentId> parseId(String input);

}
