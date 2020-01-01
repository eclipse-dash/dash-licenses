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
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.function.Consumer;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ISettings;

public class ClearlyDefinedSupport {

	private ISettings settings;
	
	public ClearlyDefinedSupport(ISettings settings) {
		this.settings = settings;
	}
	
	/**
	 * 
	 * <pre>
	 * {
	 * 	"maven/mavencentral/io.netty/netty-transport/4.1.42":{ ... },
	 *  "maven/mavencentral/io.netty/netty-resolver/4.1.42":{ ... }
	 * }
	 * </pre>
	 * 
	 * @param ids
	 * @param consumer
	 */
	public void matchAgainstClearlyDefined(Collection<IContentId> ids, Consumer<IContentData> consumer) {
		if (ids.size() == 0) return;
		
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
        	JsonArrayBuilder builder = Json.createBuilderFactory(null).createArrayBuilder();
        	ids.stream().forEach(id -> builder.add(id.toString()));
        	String json = builder.build().toString();
        	
            HttpPost post = new HttpPost(settings.getClearlyDefinedDefinitionsUrl());
            post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        		            
            CloseableHttpResponse response = httpclient.execute(post);
            if (response.getStatusLine().getStatusCode() == 200) {
            	InputStream content = response.getEntity().getContent();
            	JsonReader reader = Json.createReader(new InputStreamReader(content, "UTF-8"));
            	JsonObject read = (JsonObject)reader.read();
            	
            	read.forEach((key,each) -> 
            		consumer.accept(new ClearlyDefinedContentData(key, each.asJsonObject())));
				
            	content.close();
            }
            response.close();
        } catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            try {
				httpclient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
}
