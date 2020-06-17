# Kubernetes-client

We expect to provide a Java client:
- **Flexibility**. It can support all Kubernetes-based systems with minimized extra development, such as [Openshift](https://www.redhat.com/en/technologies/cloud-computing/openshift), [istio](https://istio.io/), etc.
- **Usability**. Developers just need to learn to write json/yaml(kubernetes native style) from [Kubernetes documentation](https://kubernetes.io/docs/home/).
- **Integration**. It can work with the other Kubernetes clients, such as [fabric8](https://github.com/fabric8io/kubernetes-client), [official](https://github.com/kubernetes-client/java/).

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
  <groupId>io.github.kubesys</groupId>
  <artifactId>kubernetes-client</artifactId>
  <version>0.5</version>
  <systemPath>${basedir}/libs/kubernetes-client-0.5.jar</systemPath>  
</dependency>
```

Not that you can get kubernetes-client-0.5.jar from this [web](https://github.com/kubesys/kubernetes-client/releases/download/v0.5/kubernetes-client-0.5.jar)

## Usage

- [Usage](#usage)
    - [Creating a client](#creating-a-client)
    - [Simple example](#simple-example)
    - [Work with other SDKs](#work-with-other-sdks)


### Creating a client

The easiest way to create a client is:

```java
KubernetesClient client = new KubernetesClient(url, eyJhbGciOiJSUzI1NiIsImtpZCI6IjZMbjZOUGxaZHZBamRfY2tPSUlCOGhoRXBwcWpvQjlFQ1RPU3NzZzhmeXcifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudC10b2tlbi00N2Y2ZCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJrdWJlcm5ldGVzLWNsaWVudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6ImYzZjRkYjRlLTUzNDYtNDc0NS1iOWM1LTdhMTJmMzk5MDI5YyIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTprdWJlcm5ldGVzLWNsaWVudCJ9.Kt31obAmLePHJWO1Y_krp-h3KRDQFd34bunG_5u-mrDk5YP7EBJ87HbNSzNpZJe-_wZQDE_ZNMprpabfz19K3D5VrZjuq1g1pwcYTpxyaN_QjzVRBx7B2lPJmKNXeA-godT8yfbQDMtiMw9uyksLg8qDMUHP5VI-CH2KSTkRgqbaU5OoAkwy2niR3S9atsVcaPCzp1ab36XLvTLckgGSTJt5uHnFfGSmWS4Ako8aM5HVVox6Hz55OgiyRUbc7c-ED39itQHDkUOgKNUXkX9saW38l5Xn9OG_MWkpyJD7GQxbQJf2I36tgM0io1c08IGTFRLcSDB_YflDeyFqJT5aDA);
client.watchResources(AutoDiscoverCustomizedResourcesWacther.TARGET_KIND, 
								AutoDiscoverCustomizedResourcesWacther.TARGET_NAMESPACE, 
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

- Prototype
  - 0.1: support create, update, get, delete and list operations using the HTTP protocol.
  - 0.2: support update status, watch operations using the HTTP protocol.
  - 0.3: support automatically detect customized Kubernetes resources during runtime.
  - 0.4: support HTTPs protocol.
  - 0.5: improve logger
  - 0.6: release to Maven
  
- Develop

- Production

## Others

- mvn -Dgpg.passphrase='pwd' clean deploy
