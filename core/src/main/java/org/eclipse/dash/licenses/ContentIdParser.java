/*************************************************************************
 * Copyright (c) 2019 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses;

public interface ContentIdParser {

	/**
	 * Implementors of this method answer either an instance of {@link IContentId}
	 * that represents the input, or <code>null</code> if the input cannot be
	 * parsed.
	 * 
	 * @param input a content identifier
	 * @return an instance of {@link IContentId} or <code>null</code>
	 */
	IContentId parseId(String input);

}
