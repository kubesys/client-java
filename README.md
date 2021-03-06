# Kubernetes-client

We expect to provide a Java client:
- **Flexibility**. It can support all Kubernetes-based systems with minimized extra development, such as [Openshift](https://www.redhat.com/en/technologies/cloud-computing/openshift), [istio](https://istio.io/), etc.
- **Usability**. Developers just need to learn to write json/yaml(kubernetes native style) from [Kubernetes documentation](https://kubernetes.io/docs/home/).
- **Integration**. It can work with the other Kubernetes clients, such as [fabric8](https://github.com/fabric8io/kubernetes-client), [official](https://github.com/kubernetes-client/java/).

This project is based on [httpclient](https://github.com/apache/httpcomponents-client) and [jackson](https://github.com/FasterXML/jackson-databind).

## Comparison

|                           | [official](https://github.com/kubernetes-client/java/) | [fabric8](https://github.com/fabric8io/kubernetes-client) | [this project](https://github.com/kubesys/kubernetes-client)  | 
|---------------------------|------------------|------------------|-------------------|
|        Compatibility                      |  provide different SDK version | provide different SDK version |  one version for all |
|  Support customized Kubernetes resources  |  a lot of development          | a lot of development          |  zero-deployment     |
|    Works with the other SDKs              |  complex                       | complex                       |  simple              |     

 
## Installation

To install the Java client library to your local Maven repository, simply execute:

```shell
git clone --recursive https://github.com/kubesys/kubernetes-client
cd java
mvn install
```

### Maven users

Add this dependency to your project's POM:

```xml
<dependency>
  <groupId>com.github.kubesys</groupId>
  <artifactId>kubernetes-client</artifactId>
  <version>4.2.0</version> 
</dependency>

<repositories>
   <repository>
       <id>pdos-repos</id>
       <name>PDOS Releases</name>
       <url>http://39.106.40.190:31021/repository/maven-public/</url>
    </repository>
</repositories>
```

## Usage

- [Usage](#usage)
    - [Creating a client](#creating-a-client)
    - [Simple example](#simple-example)
    - [Get all kinds](#get-all-kinds)
    - [Work with other SDKs](#work-with-other-sdks)


### Creating a client


The easiest way to create a client is:

```java
KubernetesClient client = new KubernetesClient(url, token);
client.watchResources(kind, namespace, 
					new AutoDiscoverCustomizedResourcesWacther(client));
```

Here, the token can be created and get by following commands:

1. create token

```yaml
kubectl create -f https://raw.githubusercontent.com/kubesys/kubernetes-client/master/account.yaml
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
System.out.println(client.getLatestApiVersion("Pod"));
System.out.println(client.getPlural("Pod"));
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

## Roadmap

- 1.3.0:  using Apache httpComponents
- 1.4.0:  support keep-alive connections
- 1.6.0:  support Thread daemon
- 1.7.0:  support getMetadatas for Kubernetes 
- 1.8.0:  fix watch timeout bug
- 1.9.0:  default timeout is 10 years
- 2.0.0   production-ready
- 2.1.0   support getMeta for all kinds
- 2.2.0:  support RBAC
- 2.3.0:  support hasResource
- 2.5.0   update getMeta
- 2.7.0   support get kinds
- 4.0.0:  support multiple group
