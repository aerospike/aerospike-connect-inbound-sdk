# Aerospike Connect Inbound SDK

SDK for building custom transforms or plugins for 
[Aerospike inbound connectors](https://www.aerospike.com/docs/connect/index.html).

## Usage

Add a dependency to com.aerospike:aerospike-connect-inbound-sdk.

### Maven

```xml
<dependency>
    <groupId>com.aerospike</groupId>
    <artifactId>aerospike-connect-inbound-sdk</artifactId>
    <version>0.9.0-SNAPSHOT</version>
</dependency>
```

See this example [pom.xml](examples/kafka-maven/pom.xml).

### Gradle

#### Kotlin DSL

```kotlin
dependencies {
    api("com.aerospike:aerospike-connect-inbound-sdk:0.9.0-SNAPSHOT")
}
```
See this example [build.gradle.kts](examples/kafka-gradle/build.gradle.kts).

#### Groovy

```groovy
dependencies {
    api "com.aerospike:aerospike-connect-inbound-sdk:0.9.0-SNAPSHOT"
}
```