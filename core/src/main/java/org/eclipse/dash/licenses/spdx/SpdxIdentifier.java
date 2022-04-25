/*************************************************************************
 * Copyright (c) 2020, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.spdx;

import java.util.Collection;

public class SpdxIdentifier extends SpdxExpression {

	private String code;

	private SpdxIdentifier(String code) {
		this.code = code;
	}

	public static SpdxExpression code(String code) {
		if ("NONE".equalsIgnoreCase(code))
			return SpdxNone.INSTANCE;
		return new SpdxIdentifier(code);
	}

	@Override
	public String toString() {
		return code;
	}

	@Override
	public boolean matchesApproved(Collection<String> approved) {
		return approved.stream().filter(value -> value.compareToIgnoreCase(code) == 0).findAny().isPresent();
	}

	@Override
	public boolean isIdentifier() {
		return true;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof SpdxIdentifier) {
			var identifier = (SpdxIdentifier) object;
			return this.code.equalsIgnoreCase(identifier.code);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.code.hashCode();
	}
}
