package org.eclipse.dash.bom;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

@Singleton
public class LicenseChecker {
	private Settings settings;
	
	@Inject
	Set<ContentIdParser> contentIdParsers;
		
	public LicenseChecker(Settings settings) {
		this.settings = settings;
	}
	
	public void getLicenseData(List<IContentId> values, Consumer<IContentData> consumer) {
		Iterator<IContentId> dependencies = values.iterator();
		while (dependencies.hasNext()) {
			List<IContentId> batch = new ArrayList<>();
			while (dependencies.hasNext()) {
				IContentId id = dependencies.next();
				if (id.isValid()) {
					batch.add(id);
					if (batch.size() > settings.getBatchSize()) break;					
				} else {
					consumer.accept(new InvalidContentData(id));
				}
			}
			getContentData(batch, consumer);
		}
	}
	
	public void getContentData(List<IContentId> ids, Consumer<IContentData> consumer) {
		Set<IContentId> unresolved = new HashSet<>();
		unresolved.addAll(ids);
		
		matchAgainstFoundationData(ids, data -> {
			unresolved.remove(data.getId());
			consumer.accept(data);
		});
		
		matchAgainstClearlyDefined(unresolved, data -> {
			unresolved.remove(data.getId());
			consumer.accept(data);
		});
		
		unresolved.forEach(id -> new InvalidContentData(id));
	}
	
	private void matchAgainstFoundationData(Collection<IContentId> ids, Consumer<IContentData> consumer) {
		if (ids.size() == 0) return;
		
		String url = settings.getLicenseCheckUrl();
        
		CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
        	JsonArrayBuilder builder = Json.createBuilderFactory(null).createArrayBuilder();
        	ids.stream().forEach(id -> builder.add(id.toString()));
        	String json = builder.build().toString();
        	
            HttpPost post = new HttpPost(url);
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            parameters.add(new BasicNameValuePair("json", json));
            
            post.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
        		            
            CloseableHttpResponse response = httpclient.execute(post);
            if (response.getStatusLine().getStatusCode() == 200) {
            	InputStream content = response.getEntity().getContent();
            	JsonReader reader = Json.createReader(new InputStreamReader(content, "UTF-8"));
            	JsonObject read = (JsonObject)reader.read();
            	
            	JsonObject approved = read.getJsonObject("approved");
				if (approved != null)
					approved.forEach((key,each) -> consumer.accept(new FoundationData(each)));
				
				JsonObject restricted = read.getJsonObject("restricted");
				if (restricted != null)
					restricted.forEach((key,each) -> consumer.accept(new FoundationData(each)));
            	
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
	private void matchAgainstClearlyDefined(Collection<IContentId> ids, Consumer<IContentData> consumer) {
		if (ids.size() == 0) return;
		String url = settings.getClearlyDefinedDefinitionsUrl();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
        	JsonArrayBuilder builder = Json.createBuilderFactory(null).createArrayBuilder();
        	ids.stream().forEach(id -> builder.add(id.toString()));
        	String json = builder.build().toString();
        	
            HttpPost post = new HttpPost(url);
            post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        		            
            CloseableHttpResponse response = httpclient.execute(post);
            if (response.getStatusLine().getStatusCode() == 200) {
            	InputStream content = response.getEntity().getContent();
            	JsonReader reader = Json.createReader(new InputStreamReader(content, "UTF-8"));
            	JsonObject read = (JsonObject)reader.read();
            	
            	read.forEach((key,each) -> consumer.accept(new ClearlyDefinedContentData(key, each)));
				
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