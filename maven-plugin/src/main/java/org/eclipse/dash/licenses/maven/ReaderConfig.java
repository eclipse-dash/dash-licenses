/*************************************************************************
 * Copyright (c) 2025 Red Hat Inc. and others
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.maven;

import java.util.Collections;
import java.util.List;

import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.dash.licenses.cli.ReaderType;

/**
 * Represents the configuration for a single reader, which reader type (e.g.
 * pnpm, npm, yarn, flat) and the file patterns to scan (list of glob patterns)
 */
public class ReaderConfig {

	/**
	 * The reader type string as entered in the pom.xml..
	 */
	@Parameter(required = true)
	private String type;

	/**
	 * A list of file/glob patterns, e.g. "frontend/ or pnpm-lock.yaml".
	 **/
	@Parameter
	private List<String> files;

	/**
	 * Convert the raw type string to our enum.
	 * 
	 * @return The ReaderType enum value.
	 */
	public ReaderType getReaderType() {
		return ReaderType.fromString(type);
	}

	/**
	 * Returns the file patterns or an empty list if none configured.
	 * 
	 * @return A non-null list of file patterns.
	 */
	public List<String> getFiles() {
		return files != null ? files : Collections.emptyList();
	}
}