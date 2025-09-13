# 🚀 Maven Constant Resources Plugin

[![Maven Central](https://img.shields.io/maven-central/v/com.enosi/maven-constant-res.svg)](https://central.sonatype.com/artifact/com.enosi/maven-constant-res)
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
            <groupId>com.enosi</groupId>
            <artifactId>maven-constant-res</artifactId>
            <version>1.0.0</version>
            <executions>
                <execution>
                    <goals>
                        <goal>generate-r</goal>
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
InputStream config = getClass().getResourceAsStream("/config.json"); 
InputStream template = getClass().getResourceAsStream("/templates/email.html");

// Typos cause runtime errors 💥
InputStream oops = getClass().getResourceAsStream("/iamges/logo.png"); // Whoops!
```

### After (✅ Type-safe)
```java
import com.enosi.generated.R;

// Generated constants - no typos possible!
InputStream logo = getClass().getResourceAsStream("/" + R.logo_png);
InputStream config = getClass().getResourceAsStream("/" + R.config_json);
InputStream template = getClass().getResourceAsStream("/" + R.email_html);

// Compile-time safety 🛡️
InputStream safe = getClass().getResourceAsStream("/" + R.non_existent); // Won't compile!
```

## 📂 Generated Code

**Resources:**
```
src/main/resources/
├── logo.png
├── config.json
└── style.css
```

**Generated R.java:**
```java
package com.enosi.generated;

public class R {
    public static final String logo_png = "logo.png";
    public static final String config_json = "config.json";
    public static final String style_css = "style.css";
}
```

## 🔧 Requirements

- ☕ Java 11+
- 🔨 Maven 3.6+

---

⭐ **Give a star if this helped you!**
