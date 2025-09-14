# 🚀 R for Maven

[![Maven Central](https://img.shields.io/maven-central/v/com.enosistudio/r-for-maven.svg)](https://central.sonatype.com/artifact/com.enosistudio/r-for-maven)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-11%2B-brightgreen.svg)](https://openjdk.java.net/)

> **Android R.java style resource constants for Java Maven projects!**

Generate compile-time constants for all your resource files automatically, just like Android's `R.java` class. No more hardcoded strings, no more typos in resource paths!

## ✨ Features

- 🎯 **Zero Configuration** - Works out of the box
- 📁 **Auto-Discovery** - Scans `src/main/resources/` automatically  
- 🏗️ **Build Integration** - Generates during Maven compilation
- 🎨 **Clean Names** - Converts filenames to valid Java identifiers
- ⚡ **Fast Generation** - Lightweight and efficient
- 🔧 **IDE Friendly** - Generated sources are properly recognized

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
                <keepInProjectFiles>true</keepInProjectFiles> <!-- Optional: keep generated files in project -->
                <resourcesDir>${project.basedir}/src/main/resources</resourcesDir> <!-- Optional: specify resources directory -->
                <outputSrcDirectory>${project.basedir}/src/main/java</outputSrcDirectory> <!-- Optional: specify output source directory -->
                <outputTargetDirectory>${project.build.directory}/generated-sources</outputTargetDirectory> <!-- Optional: specify output target directory -->
                <packageName>com.enosistudio.generated</packageName> <!-- Optional: specify package name -->
            </configuration>
            <executions>
                <execution>
                    <!-- Can help intellij to not bug when creating the source folder, when using <keepInProjectFiles>false</keepInProjectFiles> -->
                    <phase>process-sources</phase>
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
InputStream logo = getClass().getResourceAsStream("/images/logo.png");

// Typos cause runtime errors 💥
InputStream oops = getClass().getResourceAsStream("/iamges/logo.png"); // Whoops!
```

### After (✅ Type-safe)
```java
import com.enosistudio.generated.R;

// Generated constants - no typos possible!
InputStream logo = R.LOGO_PNG.openStream();

// Compile-time safety 🛡️
InputStream safe = R.LOOG_PNG.openStream(); // Won't compile!
```

## 📂 Generated Code

**Resources:**
```
src/main/resources/
├── logo.png
├── config.json
```

**Generated R.java:**
```java
package com.enosistudio.generated;

public enum R {
    LOGO_PNG("logo.png"),
    CONFIG_JSON("config.json");

    // ...
}
```

## 🔧 Requirements

- ☕ Java 11+
- 🔨 Maven 3.6+

---

⭐ **Give a star!**
