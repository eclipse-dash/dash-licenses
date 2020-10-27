/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses;

public class InvalidContentData implements IContentData {

	private IContentId id;

	public InvalidContentData(IContentId id) {
		this.id = id;
	}

	@Override
	public IContentId getId() {
		return id;
	}

	@Override
	public String getLicense() {
		return null;
	}

	@Override
	public int getScore() {
		return 0;
	}

	@Override
	public String getAuthority() {
		return null;
	}

	@Override
	public String getUrl() {
		if (!getId().isValid())
			return null;
		return "https://clearlydefined.io/definitions/" + getId();
	}

}