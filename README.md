Voici le README mis à jour pour refléter votre nouvelle implémentation hiérarchique :

```markdown
# 🚀 R for Maven

[![Maven Central](https://img.shields.io/maven-central/v/com.enosistudio/r-for-maven.svg)](https://central.sonatype.com/artifact/com.enosistudio/r-for-maven)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-11%2B-brightgreen.svg)](https://openjdk.java.net/)

> **Hierarchical resource constants for Java Maven projects - Android R.java style for the JVM!**

Generate a type-safe, hierarchical R.java class that mirrors your resource directory structure. Access files and folders with intuitive syntax like `R.config.database.readContent()` while enjoying full IDE autocompletion and compile-time validation.

## ✨ Features

- 🎯 **Zero Configuration** - Works out of the box
- 📁 **Hierarchical Structure** - Mirrors your `src/main/resources/` directory tree
- 🏗️ **Build Integration** - Generates during Maven compilation  
- 🔤 **Smart Naming** - Converts file/folder names to camelCase Java identifiers
- 📖 **Rich File API** - Built-in methods for reading, streaming, and path manipulation
- 📂 **Folder Methods** - Access folder metadata with `getName()`, `getPath()`
- ⚡ **Fast Generation** - Lightweight and efficient
- 🔧 **IDE Friendly** - Perfect autocompletion and navigation

## 📦 Installation

Add the plugin to your `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.enosistudio</groupId>
            <artifactId>r-for-maven</artifactId>
            <version>1.0.0</version>
            <configuration>
                <keepInProjectFiles>true</keepInProjectFiles>
                <packageName>com.enosistudio.generated</packageName>
            </configuration>
            <executions>
                <execution>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## 🏃‍♂️ Usage

### Before (❌ Error-prone)
```java
// Hardcoded strings everywhere!
InputStream config = getClass().getResourceAsStream("/config/database.properties");
InputStream logo = getClass().getResourceAsStream("/images/icons/logo.png");

// Typos cause runtime errors 💥
String content = Files.readString(Paths.get("config/databse.properties")); // Whoops!
```

### After (✅ Type-safe & Intuitive)
```java
import com.enosistudio.generated.R;

// Hierarchical access with autocompletion!
String content = R.config.databaseProperties.readContent();
InputStream logo = R.images.icons.logoPng.openStream();
URL resource = R.templates.emailHtml.getURL();

// Folder information
String folderName = R.config.getName();     // "config"
String folderPath = R.config.getPath();     // "config"

// Compile-time safety 🛡️
R.config.databseProperties.readContent(); // Won't compile - no typos possible!
```

## 📂 Generated Structure

**Your Resources:**
```
src/main/resources/
├── config/
│   ├── database.properties
│   └── app-settings.yml
├── templates/
│   ├── email.html
│   └── reports/
│       └── invoice.pdf
└── logo.png
```

**Generated R.java:**
```java
package com.enosistudio.generated;

public final class R {
    public static final RFile logoPng = new RFile("logo.png");
    
    public static final class config extends RFolder {
        public static final RFolder _self = new config();
        private config() { super("config", "config"); }
        
        public static final RFile databaseProperties = new RFile("config/database.properties");
        public static final RFile appSettingsYml = new RFile("config/app-settings.yml");
    }
    
    public static final class templates extends RFolder {
        public static final RFolder _self = new templates();
        private templates() { super("templates", "templates"); }
        
        public static final RFile emailHtml = new RFile("templates/email.html");
        
        public static final class reports extends RFolder {
            public static final RFolder _self = new reports();
            private reports() { super("reports", "templates/reports"); }
            
            public static final RFile invoicePdf = new RFile("templates/reports/invoice.pdf");
        }
    }
    
    // Built-in utility classes for files and folders
    public static class RFolder { /* folder methods */ }
    public static final class RFile { /* rich file API */ }
}
```

## 🔧 File Methods

Each `RFile` provides rich functionality:

```java
RFile file = R.config.databaseProperties;

// Content reading
String content = file.readContent();           // Read as UTF-8 string
InputStream stream = file.openStream();         // Get InputStream
URL url = file.getURL();                        // Get URL

// Path operations  
String fileName = file.getFileName();           // "database.properties"
String extension = file.getExtension();         // "properties" 
String path = file.getPath();                   // "config/database.properties"
Path javaPath = file.toPath();                  // Java NIO Path
File javaFile = file.toFile(baseDir);           // Java File

// Utilities
boolean exists = file.exists();                 // Check existence
String resourcePath = file.toResourcePath();    // "/config/database.properties"
```

## 📁 Folder Methods

Each folder class extends `RFolder`:

```java
// Access folder information
String name = R.config.getName();               // "config"
String path = R.config.getPath();               // "config" 
String fullPath = R.templates.reports.getPath(); // "templates/reports"
```

## ⚙️ Configuration

| Parameter | Default | Description |
|-----------|---------|-------------|
| `keepInProjectFiles` | `true` | Keep generated files in `src/main/java` |
| `resourcesDir` | `src/main/resources` | Resources directory to scan |
| `packageName` | `com.enosistudio.generated` | Package for generated R.java |
| `outputSrcDirectory` | `src/main/java` | Output when `keepInProjectFiles=true` |
| `outputTargetDirectory` | `target/generated-sources` | Output when `keepInProjectFiles=false` |

## 🔧 Requirements

- ☕ Java 11+
- 🔨 Maven 3.6+

---

⭐ **Star this repo if it helps your project!**
```