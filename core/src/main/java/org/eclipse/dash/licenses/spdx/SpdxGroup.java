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
import java.util.function.Function;

public class SpdxGroup extends SpdxExpression {

	SpdxExpression expression;

	public SpdxGroup(SpdxExpression expression) {
		this.expression = expression;
	}

	@Override
	public String toString() {
		return "(" + expression.toString() + ")";
	}

	@Override
	public String toAnnotatedString(Function<String, String> annotator) {
		return "(" + expression.toAnnotatedString(annotator) + ")";
	}

	@Override
	public SpdxExpression collapse() {
		var collapsed = expression.collapse();
		if (collapsed.isBinary())
			return new SpdxGroup(collapsed);
		return collapsed;
	}

	@Override
	public boolean matchesApproved(Collection<String> approved) {
		return expression.matchesApproved(approved);
	}

	@Override
	public boolean contains(SpdxExpression value) {
		return expression.contains(value);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof SpdxGroup) {
			var group = (SpdxGroup) object;
			return this.expression.equals(group.expression);
		}
		return false;
	}

	@Override
	boolean equalsBinaryOperation(SpdxBinaryOperation spdxBinaryOperation) {
		return expression.equalsBinaryOperation(spdxBinaryOperation);
	}

	@Override
	public int hashCode() {
		return this.expression.hashCode();
	}
}
