/**
 * Copyright (2022, ) Institute of Software, Chinese Academy of Sciences
 */
package io.github.kubesys.client.install.stack;


import io.github.kubesys.client.writers.DaemonSetWriter;
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
public class KubeExporterTest {
	
	static final String NAME = "kube-exporter";
	
	static final String EXPORTER = "node-exporter";
	
	static final String EXPORTER_IMAGE = "prom/node-exporter:v1.6.1";
	
	static final String VOLUME_PROCEFS = "procfs";
	
	static final String VOLUME_SYSFS  = "sysfs";
	
	public static void main(String[] args) throws Exception {
		
		
		DaemonSetWriter ds = new DaemonSetWriter(NAME, StackCommon.NAMESPACE);
		
		ds.withMasterEnbale()
				.withContainer(new Container(EXPORTER, EXPORTER_IMAGE, 
						new String[] {
								"--path.procfs=/host/proc",
								"--path.sysfs=/host/sys",
								"--collector.filesystem.ignored-mount-points=^/(sys|proc|dev|host|etc)($$|/)"
						},
						null, 
						new Port[] {
								new Port(9100)
						}, 
						new VolumeMount[] {
								new VolumeMount(VOLUME_PROCEFS, "/host/proc"),
								new VolumeMount(VOLUME_SYSFS, "/host/sys")
						}))
				.withHostVolume(VOLUME_PROCEFS, "/proc")
				.withHostVolume(VOLUME_SYSFS, "/sys")
		.stream(System.out);
		
		ServiceWriter service = new ServiceWriter(NAME, StackCommon.NAMESPACE);
		service.withType("NodePort").withSelector(NAME)
				.withPort(9100, 30303, "node-exporter")
		.stream(System.out);
	}
}
