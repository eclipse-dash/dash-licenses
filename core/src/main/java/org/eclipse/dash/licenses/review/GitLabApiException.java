/*************************************************************************
 * Copyright (c) 2020,2024 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.review;

public class GitLabApiException extends Exception {

	private static final long serialVersionUID = 1L;
	private int httpStatus;

	public GitLabApiException(String message) {
		this(message, null);
	}

	public GitLabApiException(String message, int httpStatus) {
		this(message, null, httpStatus);
	}

	public GitLabApiException(String message, Exception cause) {
		this(message, cause, -1);
	}

	public GitLabApiException(String message, Exception cause, int httpStatus) {
		super(message, cause);
		this.httpStatus = httpStatus;
	}

	public int getHttpStatus() {
		return httpStatus;
	}

}
