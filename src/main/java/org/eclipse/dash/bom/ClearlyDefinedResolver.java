package org.eclipse.dash.bom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class ClearlyDefinedResolver implements ContentResolver {

	@Override
	public Results resolve(ContentId id) {
		String url = "https://api.clearlydefined.io/definitions/" + id;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet get = new HttpGet(url);
            CloseableHttpResponse response = httpclient.execute(get);
            if (response.getStatusLine().getStatusCode() == 200) {
            	InputStream content = response.getEntity().getContent();
				JSONObject parse = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(content);
            	content.close();
            }
            response.close();
        } catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
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

}
