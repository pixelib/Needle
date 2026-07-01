<p align="center">
  <img src="https://raw.githubusercontent.com/pixelib/Needle/main/.github/assets/logo.svg" alt="Needle" width="160">
</p>

<h1 align="center">Needle</h1>

<p align="center">
  <a href="https://central.sonatype.com/artifact/dev.pixelib.needle/needle">
    <img src="https://img.shields.io/maven-central/v/dev.pixelib.needle/needle?style=flat-square&color=blue&label=Maven%20Central" alt="Maven Central">
  </a>
  <a href="https://github.com/pixelib/Needle/actions/workflows/ci.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/pixelib/Needle/ci.yml?style=flat-square&label=CI" alt="CI">
  </a>
  <a href="https://raw.githubusercontent.com/pixelib/Needle/main/.github/badges/jacoco.svg">
    <img src="https://raw.githubusercontent.com/pixelib/Needle/main/.github/badges/jacoco.svg" alt="Coverage">
  </a>
  <img src="https://img.shields.io/badge/Java-21-blue?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21">
  <img src="https://img.shields.io/github/license/pixelib/Needle?style=flat-square&color=blue" alt="License">
</p>

<p align="center">
  <b>Ultra-lightweight dependency injection for Java.</b><br>
  Minimal overhead, annotation-driven, zero config.
</p>

---

## Features

- **Lightweight** – tiny footprint, fast startup, no XML config
- **Annotation-driven** – `@Component`, `@Wired`, `@Named`, `@PreDestroy`
- **Automatic resolution** – scans classpath, resolves graphs, handles cycles
- **Constructor & field injection** – `@Wired` on fields or constructor params
- **Method components** – `@Component` on factory methods for programmatic wiring
- **Shutdown hooks** – automatic `@PreDestroy` cleanup via runtime hook

## Getting Started

### Install

Add dependency (Maven Central):

```xml
<dependency>
    <groupId>dev.pixelib.needle</groupId>
    <artifactId>needle</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Quick start

```java
public class App {
    static void main(String[] args) {
        Needle needle = Needle.init(App.class);
    }
}
```

---

<details>
<summary><b>More examples</b></summary>

#### Component classes

```java
@Component
public class ExampleService {
    @Wired
    private Database db;
}
```

#### Named components

```java
@Component
@Named("main")
public class MainDatabase implements Database { }

@Component
@Named("backup")
public class BackupDatabase implements Database { }
```

#### Factory / method components

```java
@Component
public class AppConfig {
    @Component
    Database createDatabase() {
        return new Database("jdbc:...");
    }
}
```

#### Constructor injection

```java
@Component
public class OrderService {
    private final PaymentGateway gateway;

    public OrderService(PaymentGateway gateway) {
        this.gateway = gateway;
    }
}
```

</details>

---

## Requirements

- Java 21+

## Build

```bash
mvn clean test
```

JaCoCo coverage report generated at `target/site/jacoco/index.html`.

## Contributing

PRs welcome. Keep it lightweight.

## License

GNU General Public License v3.0 — see [LICENSE](LICENSE).
