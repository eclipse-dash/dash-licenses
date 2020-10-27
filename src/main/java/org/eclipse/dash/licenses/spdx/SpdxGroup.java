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

public class SpdxGroup extends SpdxExpression {

	private SpdxExpression expression;

	public SpdxGroup(SpdxExpression expression) {
		this.expression = expression;
	}

	@Override
	public String toString() {
		// Note that we don't print any parentheses, because they don't really add any
		// information after parsing.
		return expression.toString();
	}

	@Override
	public boolean matchesApproved(Collection<String> approved) {
		return expression.matchesApproved(approved);
	}
}
