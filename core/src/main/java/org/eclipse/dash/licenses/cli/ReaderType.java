/*************************************************************************
 * Copyright (c) 2025 Red Hat Inc. and others
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.cli;

import java.util.Locale;

/**
 * Enum for distinguishing the supported reader types.
 */
public enum ReaderType {
	PNPM, NPM, YARN, FLAT;

	/**
	 * Converts an arbitrary string to the matching ReaderType. For instance:
	 * "pnpm", "PnPm", or "PNPM" -> PNPM.
	 *
	 * @param value The string to be converted
	 * @return The corresponding enum value
	 */
	public static ReaderType fromString(String value) {
		return ReaderType.valueOf(value.trim().toUpperCase(Locale.ROOT));
	}
}