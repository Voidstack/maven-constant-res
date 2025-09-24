# 🚀 R for Maven

[![Maven Central](https://img.shields.io/maven-central/v/com.enosistudio/r-for-maven.svg)](https://central.sonatype.com/artifact/com.enosistudio/r-for-maven)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-11%2B-brightgreen.svg)](https://openjdk.java.net/)

> **Type-safe hierarchical resource access for Java Maven projects - inspired by Android's R.java!**

Generate a type-safe, hierarchical R.java class that mirrors your resource directory structure. Access files and folders with intuitive syntax like `R.config.database.readContent()` while enjoying full IDE autocompletion and compile-time validation.

## ✨ Features

- 📁 **Hierarchical Structure** - Mirrors your resources directory tree
- 🏗️ **Build Integration** - Generates during Maven compilation  
- 🔤 **Smart Naming** - Converts file/folder names to camelCase Java identifiers
- 📖 **Rich File API** - Built-in methods for reading, streaming, and path manipulation
- 📂 **Folder Methods** - Access folder metadata with `myFolder._self` `.getName()`, `getPath()`
- ⚡ **Fast Generation** - Lightweight and efficient

## 📦 Installation

Add the plugin to your `pom.xml`:

```xml
<dependencies>
    <!-- Required: the R class use various dependancy such as org.jetbrains.annotations -->
    <dependency>
        <groupId>com.enosistudio</groupId>
        <artifactId>r-for-maven</artifactId>
        <version>1.0.2</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>com.enosistudio</groupId>
            <artifactId>r-for-maven</artifactId>
            <version>1.0.2</version>
            <configuration>
                <keepInProjectFiles>true</keepInProjectFiles> <!-- Optional: keep generated files in project -->
                <resourcesDir>${project.basedir}/src/main/resources</resourcesDir> <!-- Optional: specify resources directory -->
                <outputSrcDirectory>${project.basedir}/src/main/java</outputSrcDirectory> <!-- Optional: specify output source directory -->
                <outputTargetDirectory>${project.build.directory}/generated-sources</outputTargetDirectory> <!-- Optional: specify output target directory -->
                <packageName>com.enosistudio.generated</packageName> <!-- Optional: specify package name -->
            </configuration>
            <executions>
                <execution>
                    <phase>generate-sources</phase> <!-- Optional: Can help intellij to not bug when creating the source folder, when using <keepInProjectFiles>false</keepInProjectFiles> -->
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
String folderName = R.config._self.getName();     // "config"
String folderPath = R.config._self.getPath();     // "config"

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

⭐ **Star this repo if it helps!**
