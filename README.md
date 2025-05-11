<img src="https://github.com/user-attachments/assets/f7c677ea-2207-41ae-bab3-a19b375c6d0f" alt="logo">

# Needle  ![Test Coverage](https://img.shields.io/badge/Test%20Coverage-prima-pink?&style=plastic) ![Hyves](https://img.shields.io/badge/Hyves-follow-green?labelColor=GREEN&style=social)

Needle is an ultra-lightweight dependency injection framework for Java, similar to Spring. It simplifies the management of dependencies in your application, making your code more modular, testable, and maintainable.

## ✨ Features

- **Lightweight**: Minimal overhead, designed to be fast and efficient.
- **Annotation-based**: Use simple annotations to define dependencies.
- **Automatic Dependency Resolution**: Automatically resolves and injects dependencies.
- **Integration**: Easily integrates with other frameworks and libraries.

## 🚀 Usage

### 📘 Main class Example

```java
public class Main {
    public static void main(String[] args) {
        Needle init = Needle.init(Main.class);
    }
}
```

### 📙 Class components Example

```java
@Component
public class ExampleService {
    
    private String name;
}
```
### 📗 Method components Example

```java
@Component
public class SampleConfig {

    @Component
    SampleComponent create() {
        return new SampleComponent();
    }
}

```

### 📕 Defining Dependencies

```java

@Getter
public class B {
    @Wired
    private C depC;
}
```

```java

@Getter
public class SampleService {
    
    private final SampleComponent sampleComponent;
    
    public SampleService(SampleComponent sampleComponent) {
        this.sampleComponent = sampleComponent;
    }
}
```

## 📦 Installation

Add the following dependency to your Maven project:

```xml
<dependency>
    <groupId>dev.pixelib</groupId>
    <artifactId>needle</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
```
