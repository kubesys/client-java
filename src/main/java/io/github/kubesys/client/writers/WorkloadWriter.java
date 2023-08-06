/**
 * Copyright (2023, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.writers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author wuheng@iscas.ac.cn
 * @since  2023/07/26
 * @version 1.0.2
 *
 */
public abstract class WorkloadWriter extends KindWriter {

	static final String TEMPLATE = "apiVersion: \"#APIVERSION#\"\r\n"
			+ "kind: \"#KIND#\"\r\n"
			+ "metadata: \r\n"
			+ "  name: \"#NAME#\"\r\n"
			+ "  namespace: \"#NAMESPACE#\"\r\n"
			+ "  labels: \r\n"
			+ "    name: \"#NAME#\"\r\n"
			+ "spec: \r\n"
			+ "  replicas: #NUMBER# \r\n"
			+ "  selector: \r\n"
			+ "    matchLabels:\r\n"
			+ "      name: \"#NAME#\" \r\n"
			+ "  template: \r\n"
			+ "    metadata: \r\n"
			+ "      labels: \r\n"
			+ "        name: \"#NAME#\"";

	public WorkloadWriter(String[] kvs) throws Exception {
		super(kvs);
		if (this.json.get("spec").get("replicas").asInt() == 0) {
			((ObjectNode) json.get("spec")).remove("replicas");
		}
	}
	
	static final String MATSER = "nodeSelector:\r\n"
			+ "  node-role.kubernetes.io/control-plane: \"\"\r\n"
			+ "tolerations:\r\n"
			+ "  - key: node-role.kubernetes.io/control-plane\r\n"
			+ "    effect: NoSchedule";
	
	public WorkloadWriter withMasterEnbale() throws Exception {
	    ObjectNode template = getObjectValue(getObjectValue("spec"), "template");
	    template.set("spec", toObjectNode(MATSER));
		return this;
	}
	
	public WorkloadWriter withContainer(Container container) throws Exception {
	    ArrayNode containers = getArrayValue(getObjectValue(
	    		getObjectValue(getObjectValue("spec"), "template"), "spec"), "containers");
	    ObjectNode c = toObjectNode(container);
	    containers.add(c);
		return this;
	}
	
	static final String VOLUME = "name: \"#NAME#\"\r\n"
			+ "persistentVolumeClaim:\r\n"
			+ "  claimName: #PVC#";
	
	public WorkloadWriter withPVCVolume(String name, String pvc) throws Exception {
	    ArrayNode volumes = getArrayValue(getObjectValue(
	    		getObjectValue(getObjectValue("spec"), "template"), "spec"), "volumes");
	    ObjectNode v = toObjectNode(VOLUME, new String[] {"#NAME#", name, "#PVC#", pvc});
	    volumes.add(v);
		return this;
	}
	
	static final String HOST = "name: #NAME#\r\n"
			+ "hostPath:\r\n"
			+ "  path: #PATH#";
	
	public WorkloadWriter withHostVolume(String name, String path) throws Exception {
	    ArrayNode volumes = getArrayValue(getObjectValue(
	    		getObjectValue(getObjectValue("spec"), "template"), "spec"), "volumes");
	    ObjectNode v = toObjectNode(HOST, new String[] {"#NAME#", name, "#PATH#", path});
	    volumes.add(v);
		return this;
	}
	
	static final String CONFIGMAP = "name: #NAME#\r\n"
			+ "configMap:\r\n"
			+ "  name: #CONFIGMAP_NAME#\r\n"
			+ "  items:\r\n"
			+ "  - key: #CONFIGMAP_KEY#\r\n"
			+ "    path: #PATH#";
	
	public WorkloadWriter withConfigMapVolume(String name, String cm, String cmKey, String path) throws Exception {
	    ArrayNode volumes = getArrayValue(getObjectValue(
	    		getObjectValue(getObjectValue("spec"), "template"), "spec"), "volumes");
	    ObjectNode v = toObjectNode(CONFIGMAP, new String[] {"#NAME#", name, "#CONFIGMAP_NAME#", cm, "#CONFIGMAP_KEY#", cmKey, "#PATH#", path});
	    volumes.add(v);
		return this;
	}
	
	@Override
	public String getTemplate() {
		return TEMPLATE;
	}
	
	
	public static class Container {
		
		String name;
		
		String image;
		
		String imagePullPolicy = "IfNotPresent";
		
		Env[] env;
		
		String[] args;
		
		Port[] ports;
		
		VolumeMount[] volumeMounts;

		public Container(String name, String image, String[] args, Env[] env, Port[] ports, VolumeMount[] volumeMounts) {
			super();
			this.name = name;
			this.image = image;
			this.args = args;
			this.env = env;
			this.ports = ports;
			this.volumeMounts = volumeMounts;
		}
		
		public Container(String name, String image, Env[] env, Port[] ports, VolumeMount[] volumeMounts) {
			super();
			this.name = name;
			this.image = image;
			this.env = env;
			this.ports = ports;
			this.volumeMounts = volumeMounts;
		}

		public Env[] getEnv() {
			return env;
		}

		public void setEnv(Env[] env) {
			this.env = env;
		}

		public Port[] getPorts() {
			return ports;
		}

		public void setPorts(Port[] ports) {
			this.ports = ports;
		}

		public VolumeMount[] getVolumeMounts() {
			return volumeMounts;
		}

		public void setVolumeMounts(VolumeMount[] volumeMounts) {
			this.volumeMounts = volumeMounts;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getImage() {
			return image;
		}

		public void setImage(String image) {
			this.image = image;
		}

		public String getImagePullPolicy() {
			return imagePullPolicy;
		}

		public void setImagePullPolicy(String imagePullPolicy) {
			this.imagePullPolicy = imagePullPolicy;
		}

		public String[] getArgs() {
			return args;
		}

		public void setArgs(String[] args) {
			this.args = args;
		}
		
	}
	
	public static class Env {
		
		String name;
		
		ValueFrom valueFrom;
		
		
		public Env(String name, ValueFrom valueFrom) {
			super();
			this.name = name;
			this.valueFrom = valueFrom;
		}


		public String getName() {
			return name;
		}


		public void setName(String name) {
			this.name = name;
		}

		public ValueFrom getValueFrom() {
			return valueFrom;
		}

		public void setValueFrom(ValueFrom valueFrom) {
			this.valueFrom = valueFrom;
		}

		public static class ValueFrom {
			
			SecretKeyRef secretKeyRef;
			
			public ValueFrom(SecretKeyRef secretKeyRef) {
				super();
				this.secretKeyRef = secretKeyRef;
			}

			public SecretKeyRef getSecretKeyRef() {
				return secretKeyRef;
			}

			public void setSecretKeyRef(SecretKeyRef secretKeyRef) {
				this.secretKeyRef = secretKeyRef;
			}

			public static class SecretKeyRef {
				String name;
				
				String key;

				public SecretKeyRef(String name, String key) {
					super();
					this.name = name;
					this.key = key;
				}
				

				public String getName() {
					return name;
				}

				public void setName(String name) {
					this.name = name;
				}

				public String getKey() {
					return key;
				}

				public void setKey(String key) {
					this.key = key;
				}

			}
		}
	}
	
	public static class Port {
		
		int containerPort;

		public Port(int containerPort) {
			super();
			this.containerPort = containerPort;
		}

		public int getContainerPort() {
			return containerPort;
		}

		public void setContainerPort(int containerPort) {
			this.containerPort = containerPort;
		}
		
	}
	
	public static class VolumeMount {
		String name;
		
		String mountPath;

		public VolumeMount(String name, String mountPath) {
			super();
			this.name = name;
			this.mountPath = mountPath;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getMountPath() {
			return mountPath;
		}

		public void setMountPath(String mountPath) {
			this.mountPath = mountPath;
		}
		
	}
}
