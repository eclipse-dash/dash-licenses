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

public abstract class SpdxExpression {

	public abstract boolean matchesApproved(Collection<String> approved);

	public boolean isBinary() {
		return false;
	}

	public boolean isValid() {
		return true;
	}

	public boolean isIdentifier() {
		return false;
	}

	public SpdxExpression and(SpdxExpression expression) {
		if (this.equals(expression))
			return this;
		return SpdxBinaryOperation.create(SpdxBinaryOperation.AND, this, expression.asGroup());
	}

	/**
	 * Express the receiver as a group. We do this when joining two expressions to
	 * retain the meaning and integrity of the individual statements. The default
	 * case is that the receiver is a group. Subclasses that require actual grouping
	 * override.
	 * 
	 * @return
	 */
	protected SpdxExpression asGroup() {
		return this;
	}

	public String toPrecedenceString() {
		return toString();
	}
}
