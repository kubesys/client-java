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
|        Compatibility                      |  provide different SDK version | provide different SDK version |  one version for all |
|  Support customized Kubernetes resources  |  a lot of development          | a lot of development          |  zero-deployment     |
|    Works with the other SDKs              |  complex                       | complex                       |  simple              |     

## Architecture

![avatar](/docs/arch.png)
 
## Installation

To install the Java client library to your local Maven repository, simply execute:

```shell
git clone --recursive https://github.com/kubesys/client-java
cd java
mvn install
```

### Maven users

Add this dependency to your project's POM:

```xml
<dependency>
  <groupId>io.github.kubesys</groupId>
  <artifactId>kubernetes-client</artifactId>
  <version>2.0.2</version> 
</dependency>

<repositories>
   <repository>
       <id>pdos-repos</id>
       <name>PDOS Releases</name>
       <url>http://39.100.71.73:31021/repository/maven-public/</url>
    </repository>
</repositories>
```

## Usage

- [Usage](#usage)
    - [中文文档](https://www.yuque.com/kubesys/kubernetes-client/overview)
    - [Creating a client](#creating-a-client)
    - [Simple example](#simple-example)
    - [Get all kinds](#get-all-kinds)
    - [Work with other SDKs](#work-with-other-sdks)
    


### Creating a client


The easiest way to create a client is:

```java
String url = "https://IP:6443/";
String token = "xxx";
KubernetesClient client = new KubernetesClient(url, token);
client.watchResources(kind, new AutoDiscoverCustomizedResourcesWacther(client));
```

Here, the token can be created and get by following commands:

1. create token

```yaml
kubectl create -f https://raw.githubusercontent.com/kubesys/kubernetes-client-java/master/account.yaml
```
2. get token

```kubectl
kubectl -n kube-system describe secret $(kubectl -n kube-system get secret | grep kubernetes-client | awk '{print $1}') | grep "token:" | awk -F":" '{print$2}' | sed 's/ //g'

```



### simple-example

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
client.listResources("Pod")
```

Create a resource:

```java
client.createResource(new ObjectMapper().readTree(json));
```

Get a resource:

```java
client.getResource("Pod", "default", "busybox");
```

Delete a resource::

```java
client.deleteResource("Pod", "default", "busybox")
```


Close client

```java
client.close()
```

### get-all-kinds

```java
System.out.println(client.getKinds());
```

### work-with-other-sdks

Unlike [fabric8](https://github.com/fabric8io/kubernetes-client), which need to learn fabric8 framework to support customized Kubernetes resources. Deveolpers use this SDK just need to focus on how to write JSON.

It means that our novel design can automatically detect customized Kubernetes resources during runtime.
In addition, if you want to use object (not JSON) to program, 
you need to write JavaBean or reused it from the other SDKs. 

Let take fabric8 for example.


```java
JsonNode json = client.getResource("Pod", "default", "busybox");
io.fabric8.kubernetes.api.model.Pod pod = new ObjectMapper().readValue(json.toString(), io.fabric8.kubernetes.api.model.Pod.class);
```

## docs

- [Sonatype](https://mp.weixin.qq.com/s?__biz=Mzg2MDYzODI5Nw==&mid=2247493958&idx=1&sn=d7e47334823f58db7ce012783045f382&source=41#wechat_redirect)

## Roadmap

- 2.0.x: product ready
  - 2.0.1: use new package name: io.github.kubesys.kubeclient
  - 2.0.2: 
