/*************************************************************************
 * Copyright (c) 2020 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.spdx;

import java.util.Collection;

public class SpdxPlus extends SpdxExpression {
	private SpdxIdentifier identifier;

	private SpdxPlus(SpdxIdentifier identifier) {
		this.identifier = identifier;
	}

	public static SpdxExpression create(SpdxIdentifier identifier) {
		return new SpdxPlus(identifier);
	}

	@Override
	public boolean matchesApproved(Collection<String> approved) {
		// TODO Implement this
		// We need some means of identifying one license as being a later version of
		// another.
		return identifier.matchesApproved(approved);
	}

	@Override
	public String toString() {
		return identifier.toString() + "+";
	}
}
