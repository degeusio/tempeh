package org.tempeh;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Util {
	public static String getQualifiedName(String namespace, String name){
		return namespace + "-" + name;
	}
	
	public HttpURLConnection fetchUrl(String urlStr) throws MalformedURLException{
		URL url = new URL(urlStr);

		long timeout_ms = 100;
		int numTries = 0;
		while(true) {
			int code = -1;
			try{
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setConnectTimeout(60000); //1 minute
				connection.setAllowUserInteraction(false);
				connection.setReadTimeout(60000);  //1 minute
				connection.connect();
				code = connection.getResponseCode();
				if(code == HttpURLConnection.HTTP_OK)
					return connection;
			}
			catch(IOException e){
				
			}

			if(numTries < 3)
				numTries++;
			else{
				return null;
			}
			
			try {
				Thread.sleep(timeout_ms);
			}
			catch (Exception e1) {}
			timeout_ms *= 2;
			
		}
		
	}
}
