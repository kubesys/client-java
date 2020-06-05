# Kubernetes-client

We expect to provide a Java client:
- **Flexibility**. It can support all Kubernetes-based systems with minimized extra development, such as [Openshift](https://www.redhat.com/en/technologies/cloud-computing/openshift), [istio](https://istio.io/), etc.
- **Usability**. Developers just need to learn to write json/yaml(kubernetes native style) from [Kubernetes documentation](https://kubernetes.io/docs/home/).
- **Integration**. It can work with the other Kubernetes clients, such as [fabric8](https://github.com/fabric8io/kubernetes-client), [official](https://github.com/kubernetes-client/java/).

## Comparison

|                           | [official](https://github.com/kubernetes-client/java/) | [fabric8](https://github.com/fabric8io/kubernetes-client) | [this project]()  | 
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
  <groupId>io.github.kubesys</groupId>
  <artifactId>kubernetes-client</artifactId>
  <version>0.3</version>
  <systemPath>${basedir}/libs/kubernetes-client-0.2.jar</systemPath>  
</dependency>
```

Not that you can get kubernetes-client-0.3.jar from this [web](https://github.com/kubesys/kubernetes-client/releases/download/v0.3/kubernetes-client-0.3.jar)

## Usage

- [Usage](#usage)
    - [Creating a client](#creating-a-client)
    - [Simple example](#simple-example)
    - [Work with other SDKs](#work-with-other-sdks)


### Creating a client

The easiest way to create a client is:

```java
KubernetesClient client = new KubernetesClient(url);
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

- 0.1: support create, update, get, delete and list operations using the HTTP protocol.
- 0.2: support update status, watch operations using the HTTP protocol.
- 0.3: support automatically detect customized Kubernetes resources during runtime.
- 0.4: support HTTPs protocol.
