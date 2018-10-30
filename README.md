[![Build Status](https://travis-ci.com/java9-modularity/gradle-modules-plugin.svg?branch=master)](https://travis-ci.com/java9-modularity/gradle-modules-plugin)

Introduction
===

This Gradle plugin helps working with the Java Platform Module System.
The plugin is published in the [Gradle plugin repository](https://plugins.gradle.org/plugin/org.javamodularity.moduleplugin). 
It makes building, testing and running modules seamless from the Gradle perspective.
It sets up compiler and jvm settings with flags such as `--module-path`, so that you can build, test and run JPMS modules without manually setting up your build files.

The plugin is designed to work in repositories that contain multiple modules.
The plugin currently supports:

* Compiling modules
* Testing module code with whitebox tests (traditional unit tests)
* Testing modules blackbox (testing module boundaries and services)
* Running/packaging modular applications using the application plugin

The plugin supports the following test engines:

* JUnit 5
* JUnit 4
* TestNG

An example application using this plugin is available [here](https://github.com/java9-modularity/gradle-modules-plugin-example). 

Setup
===

For this guide we assume the following directory structure:

```
.
├── build.gradle
├── gradle
├── greeter.api
├── greeter.provider
├── greeter.provider.test
├── greeter.runner
└── settings.gradle
```

* greeter.api: Exports an interface
* greeter.provider: Provides a service implementation for the interface provided by `greeter.api`
* greeter.provider.test: Blackbox module test for `greeter.provider`
* greeter.runner: Main class that uses the `Greeter` service, that can be started/packaged with the `application plugin`

The main build file should look as follows:

```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.org.javamodularity:moduleplugin:1.1.0"
  }
}

subprojects {
    apply plugin: 'java'
    apply plugin: "org.javamodularity.moduleplugin"

    version "1.0-SNAPSHOT"

    sourceCompatibility = 11
    targetCompatibility = 11

    repositories {
        mavenCentral()
    }

    test {
        useJUnitPlatform()

        testLogging {
            events 'PASSED', 'FAILED', 'SKIPPED'
        }
    }

    dependencies {
        testImplementation 'org.junit.jupiter:junit-jupiter-api:5.3.1'
        testImplementation 'org.junit.jupiter:junit-jupiter-params:5.3.1'
        testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.3.1'
    }
}
```

The most important line in this build file is: `apply plugin: "org.javamodularity.moduleplugin"` which enables the module plugin for sub projects.
Because this is an external plugin, we need to tell Gradle where to find it, which is done in the `buildscript` section.
The subprojects typically don't need extra configuration in their build files related to modules.

To build the project simply run `./gradlew build` like you're used to.

Creating a module
===

The only thing that makes a module a module is the existence of `module-info.java`.
In a `module-info.java` you'll need to define the module name, and possibly declare your exports, dependencies on other modules, and service uses/provides.
A simple example is the following. 
The module name is `greeter.api` and the module exports a package `examples.greeter.api`.

```java
module greeter.api {
    exports examples.greeter.api;
}
```

For Gradle, just make sure the plugin is applied. 
This will make sure the correct compiler flags are used such as `--module-path` instead of `-cp`.

Module dependencies
===

When a module depends on another module, the dependency needs to be declared in two different places.
First it needs to be declared in the `dependencies` section of the gradle build file.

```groovy
dependencies {
    implementation project(':greeter.api') //Example of dependency on another module in the project
    implementation "com.fasterxml.jackson.core:jackson-databind:2.9.5" //Example of an external dependency
}
```

Next, it needs to be defined in `module-info.java`.

```java
module greeter.provider {
    requires greeter.api; //This is another module provided by our project
    requires java.net.http; //This is a module provided by the JDK
    requires com.fasterxml.jackson.databind; //This is an external module
}
```

Note that the coordinates for the Gradle dependency are not necessarily the same as the module name!

Why do we need to define dependencies in two places!?
---
We need the Gradle definition so that during build time, Gradle knows how to locate the modules.
The plugin puts these modules on the `--module-path`.
Next, when Gradle invokes the Java compiler, the compiler is set up with the correct `--module-path` so that the compiler has access to them.
When using the module system the compiler checks dependencies and encapsulation based on the `requires`, `exports` and `opens` keywords in `module-info-java`.
These are related, but clearly two different steps.

Whitebox testing
===

Whitebox testing is your traditional unit test, where an implementation class is tested in isolation.

Typically we would have a structure as follows:

```
.
├── build.gradle
└── src
    ├── main
    │   ├── java
    │   │   ├── examples
    │   │   │   └── greeter
    │   │   │       └── Friendly.java
    │   │   └── module-info.java
    │   └── resources
    └── test
        ├── java
        │   └── examples
        │       └── greeter
        │           └── FriendlyTest.java
        └── resources

```

This poses a challenge for the module system, because the whole point of encapsulation is to hide implementation classes!
A class that is not exported can't be accessed from outside the module.
In the example above we have another problem, the main and test code uses the same package structure (which is very common).
The module system does not allow split packages however.

We have two different options to work around this:

* Run whitebox tests on the classpath (ignore the fact that we're in the module world)
* *Patch* the module so that it contains the code from both the main and test sources.

Either option is fine.
By default, the plugin will automatically setup the compiler and test runtime to run on the module path, and patch the module to avoid split packages.

How does it work?
---- 

Essentially, the plugin enables the following compiler flags:

* `--module-path` containing all dependencies
* `--patch-module` to merge the test classes into the modules
* `--add-modules` to add the test runtime (JUnit 5, JUnit 4 and TestNG are supported)
* `--add-reads` for the test runtime. This way we don't have to `require` the test engine in our module.
* `--add-opens` so that the test engine can access the tests without having to export/open them in `--module-info.java`.

The plugin also integrates additional compiler flags specified in a `module-info.test` file.
For example, if your tests need to access types from a module shipping with the JDK (here: `java.scripting`).
Note that each non-comment line represents a single argument that is passed to the compiler as an option.

```text
// Make module visible.
--add-modules
  java.scripting

// Same "requires java.scripting" in a regular module descriptor.
--add-reads
  greeter.provider=java.scripting
```

See `src/test/java/module-info.test` and `src/test/java/greeter/ScriptingTest.java` in `test-project/greeter.provider` for details.

Fall-back to classpath mode
----

If for whatever reason this is unwanted or introduces problems, you can enable classpath mode, which essentially turns of the plugin while running tests.

```groovy
test {
    moduleOptions {
        runOnClasspath = true
    }
}
``` 

Blackbox testing
===

It can be very useful to test modules as a blackbox.
Are packages exported correctly, and are services provided correctly? 
This allows you to test your module as if you were a user of the module.
To do this, we create a separate module that contains the test.
This module `requires` and/or `uses` the module under test, and tests it's externally visible behaviour.
In the following example we test a module `greeter.provider`, which provides a service implementation of type `Greeter`.
The `Greeter` type is provided by yet another module `greeter.api`.

The test module would typically be named something similar to the the module it's testing, e.g. `greeter.provider.test`.
In `src/main/java` it has some code that looks like code that you would normally write to use the module that's being tested.
For example, we do a service lookup.

```java
package tests;

import examples.greeter.api.Greeter;

import java.util.ServiceLoader;

public class GreeterLocator {
    public Greeter findGreeter() {
        return ServiceLoader.load(Greeter.class).findFirst().orElseThrow(() -> new RuntimeException("No Greeter found"));
    }
}
```

In `src/test/java` we have our actual tests.

```java
package tests;

import examples.greeter.api.Greeter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class GreeterTest {
    @Test
    void testLocate() {
        Greeter greeter = new GreeterLocator().findGreeter();
        assertFalse(greeter.hello().isBlank());
    }
}
```

Because we clearly want to run this code as a module, we also need to have a `module-info.java`.

```java
import examples.greeter.api.Greeter;

module greeter.provider.test {
    requires greeter.api;

    uses Greeter;
}
```

As we've discussed before, we also need to configure the `--module-path` so that the compiler knows about the `greeter.api` module, and the JVM also starts with the `greeter.provider` module available.
In the `build.gradle` we should add dependencies to do this.

```gradle
dependencies {
    implementation project(':greeter.api')
    runtimeOnly project(':greeter.provider')
}

```  

Using the Application plugin
===
Typically you use the `application` plugin in Gradle to run the application from Gradle and, more importantly, package it in a distributable zip/tar.
To work with modules correctly JVM needs to be configured with the correct arguments such as `--module-path` to use the module path instead of the classpath.
The plugin takes care of all that automatically.
The only change compared to "normal" use of the application plugin is the format of the `mainClass`.
When starting a main class from a module, the module name needs to be provided.
To make this easier, the plugin injects a variable `$moduleName` in the build script.

```gradle
apply plugin: 'application'
mainClassName = "$moduleName/examples.Runner"
```

As usual, you can still set extra JVM arguments using the `run` configuration.

```gradle
run {
    jvmArgs = [
            "-XX:+PrintGCDetails"
    ]

    applicationDefaultJvmArgs = [
            "-XX:+PrintGCDetails"
    ]
}
```

Limitations
===

Please file issues if you run into any problems or have additional requirements!

Requirements
===

This plugin requires JDK 11 to be used when running Gradle.

Contributing
===

Please tell us if you're using the plugin on [@javamodularity](https://twitter.com/javamodularity)!
We would also like to hear about any issues or limitations you run into.
Please file issues in the Github project.
Bonus points for providing a test case that illustrates the issue.

Contributions are very much welcome. 
Please open a Pull Request with your changes.
Make sure to rebase before creating the PR so that the PR only contains your changes, this makes the review process much easier.
Again, bonus points for providing tests for your changes.

Release notes
===

1.1.0
---

