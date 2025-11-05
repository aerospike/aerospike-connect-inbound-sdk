# Aerospike Connect Inbound SDK

SDK for building message transformer or plugins for
[Aerospike inbound connectors](https://www.aerospike.com/docs/connect/index.html).

## Java version compatibility

| SDK Version | Minimum Java Version |
|-------------|----------------------|
| <= 1.3.2    | 8                    |
| 1.4.0+      | 11                   |

## Usage

Add a dependency to com.aerospike:aerospike-connect-inbound-sdk.

### Maven

```xml

<dependency>
    <groupId>com.aerospike</groupId>
    <artifactId>aerospike-connect-inbound-sdk</artifactId>
    <version>1.3.2</version>
</dependency>
```

See this example [pom.xml](examples/kafka/pom.xml).

### Gradle

#### Kotlin DSL

```kotlin
dependencies {
    api("com.aerospike:aerospike-connect-inbound-sdk:1.3.2")
}
```

#### Groovy

```groovy
dependencies {
    api "com.aerospike:aerospike-connect-inbound-sdk:1.3.2"
}
```
