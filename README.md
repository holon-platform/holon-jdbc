# Holon platform JDBC module

> Latest release: [6.0.0](#obtain-the-artifacts)

This is the __JDBC__ module of the [Holon Platform](https://holon-platform.com), which provides _Java DataBase Connectivity_ support, dealing with `javax.sql.DataSource` configuration and management in single or multiple persistence source enviroments.

The module main features are:

* A `DataSourceBuilder` API to create and configure `javax.sql.DataSource` instances using a configuration property source and supporting the most popular and best performing _pooling_ DataSource implementations ([HikariCP](https://github.com/brettwooldridge/HikariCP), [Apache DBCP2](https://commons.apache.org/proper/commons-dbcp/) and  [Tomcat JDBC Connection Pool](https://tomcat.apache.org/tomcat-9.0-doc/jdbc-pool.html)).
* A basic _multi-tenant_ DataSource implementation using the platform foundation `TenantResolver` interface.
* __Spring__ integration for `javax.sql.DataSource` beans configuration and initialization (with Spring's transaction management support) using the `@EnableDataSource` configuration annotation.
* __Spring Boot__ integration for single or multiple `javax.sql.DataSource` beans auto-configuration using `application.properties`/`application.yaml` configuration properties.

See the module [documentation](https://docs.holon-platform.com/current/reference/holon-jdbc.html) for details.

Just like any other platform module, this artifact is part of the [Holon Platform](https://holon-platform.com) ecosystem, but can be also used as a _stand-alone_ library.

See [Getting started](#getting-started) and the [platform documentation](https://docs.holon-platform.com/current/reference) for further details.

## At-a-glance overview

_JDBC DataSource configuration:_
```java
DataSourceConfigProperties config = DataSourceConfigProperties.builder()
	.withPropertySource("datasource.properties").build();

DataSource dataSource = DataSourceBuilder.create().build(config);
```

_JDBC DataSource builder:_
```java
DataSource dataSource = DataSourceBuilder.builder() 
	.type(DataSourceType.HIKARICP)
	.url("jdbc:h2:mem:testdb")
	.username("sa") 
	.minPoolSize(5)
	.withInitScriptResource("init.sql")
	.build();
```

_Spring Boot multiple DataSource auto-configuration:_
```yaml
holon: 
  datasource:
    one:
      url: "jdbc:h2:mem:testdb1"
      username: "sa"
    two:
      url: "jdbc:h2:mem:testdb2"
      username: "sa"
```

See the [module documentation](https://docs.holon-platform.com/current/reference/holon-jdbc.html) for the user guide and a full set of examples.

## Code structure

See [Holon Platform code structure and conventions](https://github.com/holon-platform/platform/blob/master/CODING.md) to learn about the _"real Java API"_ philosophy with which the project codebase is developed and organized.

## Getting started

### System requirements

The Holon Platform is built using __Java 21__, so you need a JRE/JDK version 21 or above to use the platform artifacts.

### Releases

See [releases](https://github.com/holon-platform/holon-jdbc/releases) for the available releases. Each release tag provides a link to the closed issues.

### Obtain the artifacts

The [Holon Platform](https://holon-platform.com) is open source and licensed under the [Apache 2.0 license](LICENSE.md). All the artifacts (including binaries, sources and javadocs) are available from the [Maven Central](https://mvnrepository.com/repos/central) repository.

The Maven __group id__ for this module is `com.holon-platform.jdbc` and a _BOM (Bill of Materials)_ is provided to obtain the module artifacts:

_Maven BOM:_
```xml
<dependencyManagement>
    <dependency>
        <groupId>com.holon-platform.jdbc</groupId>
        <artifactId>holon-jdbc-bom</artifactId>
        <version>6.0.0</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
</dependencyManagement>
```

See the [Artifacts list](#artifacts-list) for a list of the available artifacts of this module.

### Using the Platform BOM

The [Holon Platform](https://holon-platform.com) provides an overall Maven _BOM (Bill of Materials)_ to easily obtain all the available platform artifacts:

_Platform Maven BOM:_
```xml
<dependencyManagement>
    <dependency>
        <groupId>com.holon-platform</groupId>
        <artifactId>bom</artifactId>
        <version>${platform-version}</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
</dependencyManagement>
```

See the [Artifacts list](#artifacts-list) for a list of the available artifacts of this module.

### Build from sources

You can build the sources using Maven (version 3.3.x or above is recommended) like this: 

`mvn clean install`

## Getting help

* Check the [platform documentation](https://docs.holon-platform.com/current/reference) or the specific [module documentation](https://docs.holon-platform.com/current/reference/holon-jdbc.html).

* Ask a question on [Stack Overflow](http://stackoverflow.com). We monitor the [`holon-platform`](http://stackoverflow.com/tags/holon-platform) tag.

* Report an [issue](https://github.com/holon-platform/holon-jdbc/issues).

* A [commercial support](https://holon-platform.com/services) is available too.

## Examples

See the [Holon Platform examples](https://github.com/holon-platform/holon-examples) repository for a set of example projects.

## Contribute

See [Contributing to the Holon Platform](https://github.com/holon-platform/platform/blob/master/CONTRIBUTING.md).

[![Gitter chat](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/holon-platform/contribute?utm_source=share-link&utm_medium=link&utm_campaign=share-link) 
Join the __contribute__ Gitter room for any question and to contact us.

## License

All the [Holon Platform](https://holon-platform.com) modules are _Open Source_ software released under the [Apache 2.0 license](LICENSE).

## Artifacts list

Maven _group id_: `com.holon-platform.jdbc`

Artifact id | Description
----------- | -----------
`holon-jdbc` | Core artifact, providing `DataSourceBuilder` API and _multi-tenancy_ support
`holon-jdbc-spring` | __Spring__ integration using the `@EnableDataSource` annotation
`holon-jdbc-spring-boot` | __Spring Boot__ integration for `DataSource` auto-configuration
`holon-starter-jdbc` | __Spring Boot__ _starter_ for `DataSource` auto-configuration
`holon-starter-jdbc-hikaricp` | __Spring Boot__ _starter_ for `DataSource` auto-configuration using the [HikariCP](https://github.com/brettwooldridge/HikariCP) _pooling_ DataSource implementation
`holon-jdbc-bom` | Bill Of Materials
`documentation-jdbc` | Documentation
