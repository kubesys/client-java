/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package com.github.kubesys;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * @author wuheng09@gmail.com
 *
 */
public class ProjectTitileTest extends AbstractKubernetesClientTest {

	
	public static void main(String[] args) throws Exception {
		
		KubernetesClient client = createClient2(null);
		
		JsonNode navi = client.getResource("Frontend", "default", "routes-admin").get("spec").get("routes");
		
		for (int i = 0; i < navi.size(); i++) {
			JsonNode menu = navi.get(i).get("children");
			for (int j = 0; j < navi.size(); j++) {
				try {
					JsonNode list = menu.get(j).get("children");
					for (int k = 0; k < list.size(); k++) {
						System.out.println(list.get(k).get("name").asText());
					}
				} catch (Exception ex) {
					//
				}
			}
		}
	}


}
