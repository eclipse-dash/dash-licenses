package org.eclipse.dash.bom;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class LicenseSupport {
	
	private Map<String, String> approvedLicenses;
	
	public enum Status {Approved, Restricted};
	
	private LicenseSupport(Map<String, String> approvedLicenses) {
		this.approvedLicenses = approvedLicenses;
	}
	
	public static LicenseSupport getLicenseSupport(ISettings settings) {
		Map<String, String> approvedLicenses = getApprovedLicenses(settings);
		return new LicenseSupport(approvedLicenses);
	}
	
	public static LicenseSupport getLicenseSupport(Reader reader) {
		Map<String, String> approvedLicenses = getApprovedLicenses(reader);
		return new LicenseSupport(approvedLicenses);
	}
	
	private static Map<String, String> getApprovedLicenses(ISettings settings) {
		
		String url = settings.getApprovedLicensesUrl();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet get = new HttpGet(url);
        		            
            CloseableHttpResponse response = httpclient.execute(get);
            if (response.getStatusLine().getStatusCode() == 200) {
            	InputStream content = null;
            	try {
					content = response.getEntity().getContent();
            		InputStreamReader contentReader = new InputStreamReader(content, "UTF-8");
            		return getApprovedLicenses(contentReader);
            	} finally {
            		content.close();
            	}
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
		return null;        
	}

	public static Map<String, String> getApprovedLicenses(Reader contentReader) {
		JsonReader reader = Json.createReader(contentReader);
		JsonObject read = (JsonObject)reader.read();
		
		Map<String, String> licenses = new HashMap<>();
		read.getJsonObject("approved")
			.forEach((key,name) -> licenses.put(key.toUpperCase(),name.toString()));
		
		// Augment the official list with licenses that are acceptable, but
		// not explicitly included in our approved list.
		licenses.put("EPL-1.0", "Eclipse Public License, v1.0");
		licenses.put("EPL-2.0", "Eclipse Public License, v2.0");
		licenses.put("WTFPL", "WTFPL");
		licenses.put("CC-BY-3.0", "CC-BY-3.0");
		licenses.put("CC-BY-4.0", "CC-BY-4.0");
		licenses.put("UNLICENSE", "Unlicense");
		licenses.put("ARTISTIC-2.0", "Artistic-2.0");
		return licenses;
	}

	public Status getStatus(String expression) {
		// TODO We possibly need more sophisticated expression parsing.
		
		// This is a quick and dirty "for now" solution. I'm reasoning that
		// an AND condition is weird and needs attention. Over time, we'll
		// probably get a better sense of what more we can automate. 
		String spdx = expression.toUpperCase();
		if (spdx.contains("AND")) return Status.Restricted;
		for (String id : spdx.split("/\\s+OR\\s+/i")) {
			if (approvedLicenses.containsKey(id)) return Status.Approved;
		}
		return Status.Restricted;
	}
}
