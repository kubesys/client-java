/**
 * Copyright (2020, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.kubeclient.core;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author wuheng@iscas.ac.cn
 * @since  2.0.0
 *
 * get real Url from <code>KubernetesRuleBase</code>
 * 
 */
public class ConvertorTest {
	
	protected final static KubernetesRuleBase ruleBase  = new KubernetesRuleBase();
	
	protected final static KubernetesConvertor convertor = new KubernetesConvertor(ruleBase);
	
	protected final static ObjectNode podJson    = new ObjectMapper().createObjectNode();
	
	protected final static ObjectNode podMeta    = new ObjectMapper().createObjectNode();
	
	protected final static ObjectNode deployJson = new ObjectMapper().createObjectNode();
	
	protected final static ObjectNode deployMeta = new ObjectMapper().createObjectNode();

	protected final static ObjectNode nodeJson   = new ObjectMapper().createObjectNode();
	
	protected final static ObjectNode nodeMeta   = new ObjectMapper().createObjectNode();
	
	protected final static ObjectNode igrsJson   = new ObjectMapper().createObjectNode();
	
	protected final static ObjectNode igrsMeta   = new ObjectMapper().createObjectNode();
	
	static {
		ruleBase.fullKindToApiPrefixMapper.put("Pod",                            "https://39.100.71.73:6443/api/v1");
		ruleBase.fullKindToApiPrefixMapper.put("apps.Deployment",                "https://39.100.71.73:6443/apis/apps/v1");
		ruleBase.fullKindToApiPrefixMapper.put("Node",                           "https://39.100.71.73:6443/api/v1");
		ruleBase.fullKindToApiPrefixMapper.put("networking.k8s.io.IngressClass", "https://39.100.71.73:6443/apis/networking.k8s.io/v1");
		ruleBase.fullKindToNamespacedMapper.put("Pod",                            true);
		ruleBase.fullKindToNamespacedMapper.put("apps.Deployment",                true);
		ruleBase.fullKindToNamespacedMapper.put("Node",                           false);
		ruleBase.fullKindToNamespacedMapper.put("networking.k8s.io.IngressClass", false);
		ruleBase.fullKindToNameMapper.put("Pod",                                  "pods");
		ruleBase.fullKindToNameMapper.put("apps.Deployment",                      "deployments");
		ruleBase.fullKindToNameMapper.put("Node",                                 "nodes");
		ruleBase.fullKindToNameMapper.put("networking.k8s.io.IngressClass",       "ingressclasses");
		ruleBase.kindToFullKindMapper.put("Pod", Arrays.asList(new String[] {"Pod"}));
		ruleBase.kindToFullKindMapper.put("Deployment", Arrays.asList(new String[] {"apps.Deployment"}));
		ruleBase.kindToFullKindMapper.put("Node", Arrays.asList(new String[] {"Node"}));
		ruleBase.kindToFullKindMapper.put("IngressClass", Arrays.asList(new String[] {"networking.k8s.io.IngressClass"}));
		
		podJson.put("apiVersion", "v1");
		podJson.put("kind", "Pod");
		podMeta.put("name", "testPod");
		podMeta.put("namespace", "kube-system");
		podJson.set("metadata", podMeta);
		
		deployJson.put("apiVersion", "apps/v1");
		deployJson.put("kind", "Deployment");
		deployMeta.put("name", "testDeploy");
		deployMeta.put("namespace", "kube-system");
		deployJson.set("metadata", deployMeta);
		
		nodeJson.put("apiVersion", "v1");
		nodeJson.put("kind", "Node");
		nodeMeta.put("name", "testNode");
		nodeJson.set("metadata", nodeMeta);
		
		igrsJson.put("apiVersion", "networking.k8s.io/v1");
		igrsJson.put("kind", "IngressClass");
		igrsMeta.put("name", "testIngress");
		igrsJson.set("metadata", igrsMeta);
	}
	
	public static void main(String[] args) {
		System.out.println(convertor.createUrl(podJson));
	}
	
	@Test(expected = NullPointerException.class)
	public void testNullCreateURL() {
		convertor.createUrl(null);
	}
	
	@Test(expected = NullPointerException.class)
	public void testInvalidJsonCreateURL() {
		convertor.createUrl(new ObjectMapper().createObjectNode());
	}
	
	@Test(expected = NullPointerException.class)
	public void testNullBindingURL() {
		convertor.bindingUrl(null);
	}
	
	@Test(expected = NullPointerException.class)
	public void testInvalidJsonBindingURL() {
		convertor.bindingUrl(new ObjectMapper().createObjectNode());
	}
	
	@Test(expected = NullPointerException.class)
	public void testNullDeleteKindURL() {
		convertor.deleteUrl(null, "abc", "abc");
	}
	
	@Test(expected = NullPointerException.class)
	public void testNullDeleteNameURL1() {
		convertor.deleteUrl("abc", "abc", null);
	}
	
	@Test(expected = NullPointerException.class)
	public void testNullDeleteNameURL2() {
		convertor.deleteUrl("abc", "abc", "");
	}
	
	@Test(expected = NullPointerException.class)
	public void testNullListKindURL() {
		convertor.listUrl(null, "abc");
	}
	
	@Test
	public void testValidCreateURL() {
		assertEquals(convertor.createUrl(podJson), "https://39.100.71.73:6443/api/v1/namespaces/kube-system/pods");
		assertEquals(convertor.createUrl(deployJson), "https://39.100.71.73:6443/apis/apps/v1/namespaces/kube-system/deployments");
		assertEquals(convertor.createUrl(nodeJson), "https://39.100.71.73:6443/api/v1/nodes");
		assertEquals(convertor.createUrl(igrsJson), "https://39.100.71.73:6443/apis/networking.k8s.io/v1/ingressclasses");
	}
	
	@Test
	public void testValidDeleteURL() {
		assertEquals(convertor.deleteUrl("Pod", "kube-system", "testPod"), "https://39.100.71.73:6443/api/v1/namespaces/kube-system/pods/testPod");
		assertEquals(convertor.deleteUrl("apps.Deployment", "kube-system", "testDeploy"), "https://39.100.71.73:6443/apis/apps/v1/namespaces/kube-system/deployments/testDeploy");
		assertEquals(convertor.deleteUrl("Node", "", "testNode"), "https://39.100.71.73:6443/api/v1/nodes/testNode");
		assertEquals(convertor.deleteUrl("networking.k8s.io.IngressClass", "", "testIngress"), "https://39.100.71.73:6443/apis/networking.k8s.io/v1/ingressclasses/testIngress");
	}
	
	@Test
	public void testValidUpdateURL() {
		assertEquals(convertor.updateUrl("Pod", "kube-system", "testPod"), "https://39.100.71.73:6443/api/v1/namespaces/kube-system/pods/testPod");
		assertEquals(convertor.updateUrl("apps.Deployment", "kube-system", "testDeploy"), "https://39.100.71.73:6443/apis/apps/v1/namespaces/kube-system/deployments/testDeploy");
		assertEquals(convertor.updateUrl("Node", "", "testNode"), "https://39.100.71.73:6443/api/v1/nodes/testNode");
		assertEquals(convertor.updateUrl("networking.k8s.io.IngressClass", "", "testIngress"), "https://39.100.71.73:6443/apis/networking.k8s.io/v1/ingressclasses/testIngress");
	}
	
	@Test
	public void testValidGetURL() {
		assertEquals(convertor.getUrl("Pod", "kube-system", "testPod"), "https://39.100.71.73:6443/api/v1/namespaces/kube-system/pods/testPod");
		assertEquals(convertor.getUrl("apps.Deployment", "kube-system", "testDeploy"), "https://39.100.71.73:6443/apis/apps/v1/namespaces/kube-system/deployments/testDeploy");
		assertEquals(convertor.getUrl("Node", "", "testNode"), "https://39.100.71.73:6443/api/v1/nodes/testNode");
		assertEquals(convertor.getUrl("networking.k8s.io.IngressClass", "", "testIngress"), "https://39.100.71.73:6443/apis/networking.k8s.io/v1/ingressclasses/testIngress");
	}
	
	@Test
	public void testValidListURL() {
		assertEquals(convertor.listUrl("Pod", "kube-system"), "https://39.100.71.73:6443/api/v1/namespaces/kube-system/pods");
		assertEquals(convertor.listUrl("Pod", ""), "https://39.100.71.73:6443/api/v1/pods");
		assertEquals(convertor.listUrl("apps.Deployment", "kube-system"), "https://39.100.71.73:6443/apis/apps/v1/namespaces/kube-system/deployments");
		assertEquals(convertor.listUrl("apps.Deployment", ""), "https://39.100.71.73:6443/apis/apps/v1/deployments");
		assertEquals(convertor.listUrl("Node", ""), "https://39.100.71.73:6443/api/v1/nodes");
		assertEquals(convertor.listUrl("networking.k8s.io.IngressClass", ""), "https://39.100.71.73:6443/apis/networking.k8s.io/v1/ingressclasses");
	}
	
	@Test
	public void testValidUpdateStatusUrlURL() {
		assertEquals(convertor.updateStatusUrl("Pod", "kube-system", "testPod"), "https://39.100.71.73:6443/api/v1/namespaces/kube-system/pods/testPod/status");
		assertEquals(convertor.updateStatusUrl("apps.Deployment", "kube-system", "testDeploy"), "https://39.100.71.73:6443/apis/apps/v1/namespaces/kube-system/deployments/testDeploy/status");
		assertEquals(convertor.updateStatusUrl("Node", "", "testNode"), "https://39.100.71.73:6443/api/v1/nodes/testNode/status");
		assertEquals(convertor.updateStatusUrl("networking.k8s.io.IngressClass", "", "testIngress"), "https://39.100.71.73:6443/apis/networking.k8s.io/v1/ingressclasses/testIngress/status");
	}
	
	@Test
	public void testValidWatchOneUrlURL() {
		assertEquals(convertor.watchOneUrl("Pod", "kube-system", "testPod"), "https://39.100.71.73:6443/api/v1/watch/namespaces/kube-system/pods/testPod?watch=true&timeoutSeconds=315360000");
		assertEquals(convertor.watchOneUrl("apps.Deployment", "kube-system", "testDeploy"), "https://39.100.71.73:6443/apis/apps/v1/watch/namespaces/kube-system/deployments/testDeploy?watch=true&timeoutSeconds=315360000");
		assertEquals(convertor.watchOneUrl("Node", "", "testNode"), "https://39.100.71.73:6443/api/v1/watch/nodes/testNode?watch=true&timeoutSeconds=315360000");
		assertEquals(convertor.watchOneUrl("networking.k8s.io.IngressClass", "", "testIngress"), "https://39.100.71.73:6443/apis/networking.k8s.io/v1/watch/ingressclasses/testIngress?watch=true&timeoutSeconds=315360000");
	}
	
	@Test
	public void testValidWatchAllUrlURL() {
		assertEquals(convertor.watchAllUrl("Pod", "kube-system"), "https://39.100.71.73:6443/api/v1/watch/namespaces/kube-system/pods?watch=true&timeoutSeconds=315360000");
		assertEquals(convertor.watchAllUrl("Pod", ""), "https://39.100.71.73:6443/api/v1/watch/pods?watch=true&timeoutSeconds=315360000");
		assertEquals(convertor.watchAllUrl("apps.Deployment", "kube-system"), "https://39.100.71.73:6443/apis/apps/v1/watch/namespaces/kube-system/deployments?watch=true&timeoutSeconds=315360000");
		assertEquals(convertor.watchAllUrl("apps.Deployment", ""), "https://39.100.71.73:6443/apis/apps/v1/watch/deployments?watch=true&timeoutSeconds=315360000");
		assertEquals(convertor.watchAllUrl("Node", ""), "https://39.100.71.73:6443/api/v1/watch/nodes?watch=true&timeoutSeconds=315360000");
		assertEquals(convertor.watchAllUrl("networking.k8s.io.IngressClass", ""), "https://39.100.71.73:6443/apis/networking.k8s.io/v1/watch/ingressclasses?watch=true&timeoutSeconds=315360000");
	}
	
}
