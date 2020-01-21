/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License 2.0 which accompanies this 
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.clearlydefined;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Consumer;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.JsonUtils;

public class ClearlyDefinedSupport {

	private ISettings settings;

	public ClearlyDefinedSupport(ISettings settings) {
		this.settings = settings;
	}

	/**
	 * The ClearlyDefined API expects a flat array of ids in JSON format in the
	 * payload of the POST request.
	 * 
	 * <pre>
	 * {
	 *	"maven/mavencentral/io.netty/netty-transport/4.1.42",
	 *	"maven/mavencentral/io.netty/netty-resolver/4.1.42",
	 * 	...
	 * }
	 * </pre>
	 * 
	 * And answers an associative array in JSON format similar to the following.
	 * 
	 * <pre>
	 * {
	 *	"maven/mavencentral/io.netty/netty-transport/4.1.42":{ ... },
	 *	"maven/mavencentral/io.netty/netty-resolver/4.1.42":{ ... },
	 *	...
	 * }
	 * </pre>
	 * 
	 * See the {@link ClearlyDefinedSupport} type for an example of the value.
	 * 
	 * Note that the ClearlyDefined API will always return a value for every id that
	 * is provided. In cases where the id is not in the ClearlyDefined database, the
	 * score is reported as 0.
	 * 
	 * @param ids      ids to search in five-part ClearlyDefined format.
	 * @param consumer the closure to execute with a instance of
	 *                 {@link ClearlyDefinedContentData} for each value included in
	 *                 the result.
	 */
	public void matchAgainstClearlyDefined(Collection<IContentId> ids, Consumer<IContentData> consumer) {
		if (ids.size() == 0)
			return;

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpPost post = new HttpPost(settings.getClearlyDefinedDefinitionsUrl());
			post.setEntity(new StringEntity(JsonUtils.toJson(ids), ContentType.APPLICATION_JSON));

			CloseableHttpResponse response = httpclient.execute(post);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				InputStream content = response.getEntity().getContent();

				JsonUtils.readJson(content).forEach((key, each) -> {
					ClearlyDefinedContentData data = new ClearlyDefinedContentData(key, each.asJsonObject());
					if (data.getScore() > settings.getConfidenceThreshold()) {
						consumer.accept(data);
					}
				});

				content.close();
			} else {
				// TODO Deal with anything other than a 200 response code
			}
			response.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
		}
	}
}
