/**
 * Copyright (2022, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.install.stack;


import io.github.kubesys.client.writers.ConfigMapWriter;
import io.github.kubesys.client.writers.DeploymentWriter;
import io.github.kubesys.client.writers.PVCWriter;
import io.github.kubesys.client.writers.PVWriter;
import io.github.kubesys.client.writers.ServiceWriter;
import io.github.kubesys.client.writers.WorkloadWriter.Container;
import io.github.kubesys.client.writers.WorkloadWriter.Port;
import io.github.kubesys.client.writers.WorkloadWriter.VolumeMount;

/**
 * @author wuheng@iscas.ac.cn
 * @since  2023/08/02
 * @version 1.0.3
 *
 * get real Url from <code>KubernetesRuleBase</code>
 * 
 */
public class KubeMonitorTest {
	
	static final String NAME = "kube-monitor";
	
	static final String CONFIG_YML = "prometheus.yml";
	
	static final String YML_PATH = "configs/prometheus.yml";
	
	static final String PROM = "prometheus";
	
	static final String PROM_IMAGE = "prom/prometheus:v2.46.0";
	
	static final String VOLUME_CONFIG = "config";
	
	public static void main(String[] args) throws Exception {
		
		ConfigMapWriter cm = new ConfigMapWriter(NAME, StackCommon.NAMESPACE);
		cm.withYamlData(CONFIG_YML, StackCommon.read(YML_PATH)).stream(System.out);
		
		PVWriter pv = new PVWriter(NAME);
		pv.withCapacity("20").withPath(StackCommon.PATH + PROM).withPVC(NAME, StackCommon.NAMESPACE).stream(System.out);
		PVCWriter pvc = new PVCWriter(NAME, StackCommon.NAMESPACE);
		pvc.withCapacity("20").stream(System.out);
		
		DeploymentWriter deploy = new DeploymentWriter(NAME, StackCommon.NAMESPACE);
		
		deploy.withMasterEnbale()
				.withContainer(new Container(PROM, PROM_IMAGE, 
								new String[] {
										"--config.file=/etc/prometheus/prometheus.yml",
										"--storage.tsdb.path=/prometheus",
								},
								null, 
								new Port[] {
										new Port(9090)
								}, 
								new VolumeMount[] {
										new VolumeMount(VOLUME_CONFIG, "/etc/prometheus/"),
										new VolumeMount(StackCommon.VOLUME_DATA, "/prometheus")
								}))
				.withConfigMapVolume(VOLUME_CONFIG, NAME, CONFIG_YML, CONFIG_YML)
				.withPVCVolume(StackCommon.VOLUME_DATA, NAME)
		.stream(System.out);
		
		
		ServiceWriter service = new ServiceWriter(NAME, StackCommon.NAMESPACE);
		service.withType("NodePort").withSelector(NAME)
				.withPort(9090, 30302, "monitoring")
				.stream(System.out);
	}
}
