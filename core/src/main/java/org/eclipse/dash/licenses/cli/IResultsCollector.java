/*************************************************************************
 * Copyright (c) 2020, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.cli;

import org.eclipse.dash.licenses.LicenseData;

public interface IResultsCollector {

	void accept(LicenseData data);

	default void close() {
	}

	/**
	 * Answers the status of the collector. In practical terms, this is the value
	 * that is returned to the shell following execution of the programme. A zero
	 * return value indicates that the script ran as expected; a non-zero value
	 * indicates abnormal results. By convention, we return the number of
	 * problematic items we encounter.
	 * 
	 * @return
	 */
	default int getStatus() {
		return 0;
	}
}
