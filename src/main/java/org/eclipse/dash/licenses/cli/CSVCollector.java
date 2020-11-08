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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Optional;

import org.eclipse.dash.licenses.LicenseData;
import org.eclipse.dash.licenses.LicenseSupport.Status;

/**
 * Instances of CSVCollector output all results to an {@link OutputStream} in
 * CVS format. The current implementation dumps output as it encounters results;
 * no attempts are made to synchronize
 */
public class CSVCollector implements IResultsCollector {
	private PrintWriter output;

	public CSVCollector(OutputStream output) {
		this.output = new PrintWriter(output);
	}

	@Override
	public void accept(LicenseData data) {
		// FIXME Use a proper CSV framework
		// TODO Make column selection and order configurable
		output.println(
				String.format("%s, %s, %s, %s", data.getId(), Optional.ofNullable(data.getLicense()).orElse("unknown"),
						data.getStatus() == Status.Approved ? "approved" : "restricted",
						Optional.ofNullable(data.getAuthority()).orElse("none")));
	}

	@Override
	public void close() {
		output.flush();
	}
}
