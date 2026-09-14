/*************************************************************************
 * Copyright (c) 2026 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.dash.licenses.http.HttpClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HttpClientServiceTests {

	@ParameterizedTest
	@ValueSource(ints = { 502, 503, 504, 524 })
	void testTransientGatewayErrorsAreRetriable(int statusCode) {
		assertTrue(HttpClientService.isTransientGatewayError(statusCode));
	}

	@ParameterizedTest
	@ValueSource(ints = { 200, 400, 401, 403, 404, 429, 500, 501 })
	void testOtherStatusesAreNotRetriable(int statusCode) {
		assertFalse(HttpClientService.isTransientGatewayError(statusCode));
	}

	@Test
	void testSuccessIsNotRetriable() {
		assertFalse(HttpClientService.isTransientGatewayError(200));
	}
}
