/*************************************************************************
 * Copyright (c) 2020,2022 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.spdx;

import java.util.Collection;

public class SpdxNone extends SpdxExpression {

	public static SpdxNone INSTANCE = new SpdxNone();

	private SpdxNone() {
	}

	@Override
	public boolean matchesApproved(Collection<String> approved) {
		return false;
	}

	@Override
	public SpdxExpression and(SpdxExpression expression) {
		return expression;
	}

	@Override
	public String toString() {
		return "None";
	}

	@Override
	protected SpdxExpression asGroup() {
		return this;
	}
}
