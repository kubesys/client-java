# Kubernetes-client-java

We expect to provide a Java client (Please using JDK 8):

- **Flexibility**. It can support all Kubernetes-based systems with minimized extra development, such as [Openshift](https://www.redhat.com/en/technologies/cloud-computing/openshift), [istio](https://istio.io/), etc.
- **Usability**. Developers just need to learn to write json/yaml(kubernetes native style) from [Kubernetes documentation](https://kubernetes.io/docs/home/).
- **Integration**. It can work with the other Kubernetes clients, such as [fabric8](https://github.com/fabric8io/kubernetes-client), [official](https://github.com/kubernetes-client/java/).

This project is based on the following softwares.

|               NAME            |   Website                       |      LICENSE              | 
|-------------------------------|---------------------------------|---------------------------|
|     Apache HttpComponent      |  https://github.com/apache/httpcomponents-client |  Apache License 2.0 |
|     Apache Commons-codec      |  https://github.com/apache/commons-codec         |  Apache License 2.0 |
|     FasterXML Jackson         |  https://github.com/FasterXML/jackson-databind   |  Apache License 2.0 |
|     Snakeyaml                 |  https://github.com/asomov/snakeyaml             |  Apache License 2.0 |
 
## Comparison

|                           | [official](https://github.com/kubernetes-client/java/) | [fabric8](https://github.com/fabric8io/kubernetes-client) | [this project](https://github.com/kubesys/kubernetes-client)  | 
|---------------------------|------------------|------------------|-------------------|
|        Compatibility                      |  provide different SDK versions | provide different SDK versions  |  one version for all |
|  Support customized Kubernetes resources  |  a lot of development           | a lot of development            |  zero-deployment     |
|    Works with the other SDKs              |  /                              | /                               |  simple              |     

## Architecture

![avatar](/docs/arch.png)
 
## Installation

To install the Java client library to your local Maven repository, simply execute:

```shell
git clone --recursive https://github.com/kubesys/kubernetes-client-java
cd java
mvn install
```

### Maven users

Add this dependency to your project's POM:

```xml
<dependency>
  <groupId>io.github.kubesys</groupId>
  <artifactId>kubernetes-client</artifactId>
  <version>2.0.3</version> 
</dependency>

<repositories>
   <repository>
       <id>pdos-repos</id>
       <name>PDOS Releases</name>
       <url>http://39.100.71.73:31021/repository/maven-public/</url>
    </repository>
</repositories>
```

## Quick start

- [Creating a client](#creating-a-client)
- [Get all kinds](#get-all-kinds)
- [Simple examples](#simple-examples)
- [watch resourcs](#watch-resources)
- [Work with other SDKs](#work-with-other-sdks)
    


### Creating a client


The easiest way to create a client is:

```java
String url = "https://IP:6443/";
String token = "xxx";
KubernetesClient client = new KubernetesClient(url, token);
```

you can create and get a token by the following commands:

1. create token

```yaml
kubectl create -f https://raw.githubusercontent.com/kubesys/kubernetes-client-java/master/account.yaml
```
2. get token

```kubectl
kubectl -n kube-system describe secret $(kubectl -n kube-system get secret | grep kubernetes-client | awk '{print $1}') | grep "token:" | awk -F":" '{print$2}' | sed 's/ //g'

```
### get-all-kinds

1. get kind

Here, the 'kind' means the [Kubernetes kind](https://kubernetes.io/docs/concepts/overview/working-with-objects/kubernetes-objects/)

```java
System.out.println(client.getKinds().toPrettyString());
```
for example, the output is 
```json
[
	"DaemonSet",
	"Node",
	"Pod",
	"ClusterRole",
	"StorageClass",
	"PriorityClass",
	"ReplicationController",
	"PersistentVolume",
	"ReplicaSet",
	"Job"
]
```

2. get fullkind

Here, fullkind = [apiversion](https://kubernetes.io/docs/concepts/overview/working-with-objects/kubernetes-objects/) + "." + [kind]((https://kubernetes.io/docs/concepts/overview/working-with-objects/kubernetes-objects/))

```java
System.out.println(client.getFullKinds().toPrettyString());
```

for example, the output is

```json
[
	"apps.DaemonSet",
	"Node",
	"Pod",
	"rbac.authorization.k8s.io.ClusterRole",
	"storage.k8s.io.StorageClass",
	"scheduling.k8s.io.PriorityClass",
	"apps.ReplicaSet",
	"batch.Job"
]
```
### simple-examples

Assume you have a json:

```json
{
  "apiVersion": "v1",
  "kind": "Pod",
  "metadata": {
    "name": "busybox",
    "namespace": "default",
    "labels": {
      "test": "test"
    }
  }
}
```

List resources:

```java
client.listResources("Pod")       // kind or fullKind
```

Create a resource:

```java
client.createResource(new ObjectMapper().readTree(json));
```

Get a resource:

```java
client.getResource("Pod", "default", "busybox");       // kind or fullKind
```

Delete a resource::

```java
client.deleteResource("Pod", "default", "busybox")     // kind or fullKind
```


Close client

```java
client.close()
```

### watch-resources

```java
KubernetesWatcher watcher = new KubernetesWatcher(client) {
			
  public void doModified(JsonNode node) {
    System.out.println(node);
  }
  
  public void doDeleted(JsonNode node) {
    System.out.println(node);
  }
  
  public void doAdded(JsonNode node) {
    System.out.println(node);
  }
  
  public void doClose() {
    System.out.println("close");
  }
};

client.watchResources("Pod", KubernetesConstants.VALUE_ALL_NAMESPACES, watcher);
```

### work-with-other-sdks

Unlike [fabric8](https://github.com/fabric8io/kubernetes-client), you need to implement 'create, update, delete, list, get, watch' operators for customized Kubernetes resources. 

This SDK peovides a unified API using JSON, and can automatically support customized Kubernetes resources.

In addition, if you want to get object (not JSON), you just need to write a JavaBean or reused a JavaBean from an existing SDKs. 

Let take fabric8 for example.


```java
JsonNode json = client.getResource("Pod", "default", "busybox");
io.fabric8.kubernetes.api.model.Pod pod = new ObjectMapper().readValue(json.toString(), io.fabric8.kubernetes.api.model.Pod.class);
```


## Roadmap

- 2.0.x: product ready
  - 2.0.1: use new package name: io.github.kubesys.kubeclient
  - 2.0.2: remove duplicate codes
  - 2.0.3: support watch many resources
  - 2.0.4: support default value when Kubernetes has many groups for a kind

  
[Sonatype](https://mp.weixin.qq.com/s?__biz=Mzg2MDYzODI5Nw==&mid=2247493958&idx=1&sn=d7e47334823f58db7ce012783045f382&source=41#wechat_redirect)
