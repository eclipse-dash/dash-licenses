/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Instances of {@link Batchifier} group the input that they're provided with
 * into batches and hand them off to a consumer.
 *
 * @param <T> the type of object that is batched
 */
public class Batchifier<T> {
	private Consumer<Collection<T>> consumer = batch -> {};
	private int batchSize = 1000;

	public Batchifier() {
	}

	/**
	 * Set the maxium batch size. Batches will be no larger (but may be smaller)
	 * than this number.
	 *
	 * @param batchSize the maximum size of batches sent to the consumer.
	 * @return the receiver
	 */
	public Batchifier<T> setBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	/**
	 * Set the consumer that will received the batched data.
	 *
	 * @param consumer
	 * @return the receiver
	 */
	public Batchifier<T> setConsumer(Consumer<Collection<T>> consumer) {
		this.consumer = consumer;
		return this;
	}

	/**
	 * Actually do the batching operation. Pull data from the provided input and
	 * group it into batches. When the batch is the right size, hand it over to the
	 * consumer.
	 *
	 * Yes, "batchify" isn't really a word. And, yes, we really could just use the
	 * word "batch" here, but since a batch can be either noun or a verb, we decided
	 * to be a bit playful with the name to make it more obvious that this method
	 * does something. Yes, all method names are verbs... this way just feels more
	 * understandable.
	 *
	 * @param input The data to process.
	 * @return the receiver
	 */
	public Batchifier<T> batchify(Iterator<T> input) {
		while (input.hasNext()) {
			List<T> batch = new ArrayList<>();
			while (input.hasNext()) {
				T id = input.next();
				batch.add(id);
				if (batch.size() >= batchSize)
					break;
			}
			consumer.accept(batch);
		}
		return this;
	}
}
