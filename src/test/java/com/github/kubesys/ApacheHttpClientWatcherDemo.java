/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * @author wuheng09@gmail.com
 *
 */
public class ApacheHttpClientWatcherDemo {

	
	public static void main(String[] args) throws Exception {

		String url = "";
	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    HttpGet get = new HttpGet(url);
	    HttpResponse response = httpClient.execute(get);
	    BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	    String line = null;
	    while ((line = br.readLine()) != null) {
	    	System.out.println(line);
	    }
	}
}
