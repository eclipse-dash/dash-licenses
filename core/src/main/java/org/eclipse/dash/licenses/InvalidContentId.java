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

public class InvalidContentId implements IContentId {

	private String value;

	public InvalidContentId(String value) {
		this.value = value;
	}

	@Override
	public String toClearlyDefined() {
		return null;
	}

	@Override
	public String generateDownloadUrl() {
		return null;
	}

	@Override
	public String toString() {
		return "Invalid: " + value;
	}

	@Override
	public String getNamespace() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getVersion() {
		return null;
	}

	@Override
	public String getType() {
		return null;
	}

	@Override
	public String getSource() {
		return null;
	}

	@Override
	public boolean isValid() {
		return false;
	}
}
