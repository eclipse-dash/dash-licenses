/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import org.eclipse.dash.licenses.util.Batchifier;
import org.junit.jupiter.api.Test;

class BatchifierTests {

	@Test
	void testMultipleBatches() {
		final Set<Collection<Integer>> batches = new HashSet<>();
		new Batchifier<Integer>().setBatchSize(5).setConsumer(batch -> {
			assertEquals(5, batch.size());
			batches.add(batch);
		}).batchify(IntStream.range(0, 20).iterator());
		assertEquals(4, batches.size());
	}

	@Test
	void testSmallerThanBatchSize() {
		final Set<Collection<Integer>> batches = new HashSet<>();
		new Batchifier<Integer>().setBatchSize(100).setConsumer(batch -> {
			assertEquals(20, batch.size());
			batches.add(batch);
		}).batchify(IntStream.range(0, 20).iterator());
		assertEquals(1, batches.size());
	}

}
