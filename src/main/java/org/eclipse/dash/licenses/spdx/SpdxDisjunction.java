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

public class SpdxDisjunction extends SpdxExpression {

	private SpdxExpression operand1;
	private SpdxExpression operand2;

	public SpdxDisjunction(SpdxExpression operand1, SpdxExpression operand2) {
		this.operand1 = operand1;
		this.operand2 = operand2;
	}

	@Override
	public String toString() {
		return "(" + operand1.toString() + " OR " + operand2.toString() + ")";
	}

	@Override
	public boolean matchesApproved(Collection<String> approved) {
		return operand1.matchesApproved(approved) || operand2.matchesApproved(approved);
	}
}
