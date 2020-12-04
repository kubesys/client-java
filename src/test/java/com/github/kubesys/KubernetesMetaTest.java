/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * @author wuheng09@gmail.com
 *
 */
public class KubernetesMetaTest extends AbstractKubernetesClientTest {

	
	
	public static void main(String[] args) throws Exception {
		KubernetesClient client = createClient2();

		JsonNode json = client.getAnalyzer().getMeta();
		for (Iterator<JsonNode> iter =json.iterator();iter.hasNext(); ) {
			System.out.println(iter.next().get("kind"));
		}
		
	}

}
