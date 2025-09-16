/*************************************************************************
 * Copyright (c) 2025 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Arrays;

import org.eclipse.dash.licenses.util.ProximityComparator;
import org.junit.jupiter.api.Test;

class ProximityComparatorTests {

	@Test
	void test1() {
		var stuff = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		Arrays.sort(stuff, new ProximityComparator(5));
		assertArrayEquals(new Integer[] {5, 4, 6, 3, 7, 2, 8, 1, 9, 10}, stuff);
	}

	@Test
	void test2() {
		var stuff = new Integer[]{5, 4, 6, 3, 7, 2, 8, 1, 9, 10};
		Arrays.sort(stuff, new ProximityComparator(5));
		assertArrayEquals(new Integer[] {5, 4, 6, 3, 7, 2, 8, 1, 9, 10}, stuff);
	}

	@Test
	void test3() {
		var stuff = new Integer[]{5, 4, 6, 3, 7, 2, 8, 1, 9};
		Arrays.sort(stuff, new ProximityComparator(5));
		assertArrayEquals(new Integer[] {5, 4, 6, 3, 7, 2, 8, 1, 9}, stuff);
	}

	@Test
	void test4() {
		var stuff = new Integer[]{5, 4, 5, 3, 5, 2, 8, 1, 9};
		Arrays.sort(stuff, new ProximityComparator(5));
		assertArrayEquals(new Integer[] {5, 5, 5, 4, 3, 2, 8, 1, 9}, stuff);
	}
}
