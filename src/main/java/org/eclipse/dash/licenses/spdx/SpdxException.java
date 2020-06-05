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

public class SpdxException extends SpdxExpression {

	private SpdxExpression exception;
	private SpdxExpression identifier;

	public SpdxException(SpdxExpression exception, SpdxExpression identifier) {
		this.exception = exception;
		this.identifier = identifier;
	}

	@Override
	public String toString() {
		return identifier.toString() + " WITH " + exception.toString();
	}

	@Override
	public boolean matchesApproved(Collection<String> singleton) {
		// TODO Implement this.
		return false;
	}
}
