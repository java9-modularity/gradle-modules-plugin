Introduction
===

This test project can be used as a standalone test project to verify the published plugin.
It is also used as an internal test project for testing unpublished plugin changes.

Standalone test product
===
To run this product as a standalone test product use this command (launched from `test-project` directory):
```
../gradlew clean build
```

It will use the most recent plugin version from Gradle maven repository to compile the test project with
modules and run the unit tests.

Testing locally published plugin
===

You can publish the plugin locally by running this command from the root directory:

`./gradlew publishToMavenLocal`

You can test the locally published plugin by running the following command from `test-project` directory.

`../gradlew -c local_maven_settings.gradle clean build` 

It will use the locally published version of the plugin to compile the test project with 
modules and run the unit tests.


Internal test project
===

This mode is enabled in `ModulePluginSmokeTest` by passing an extra parameter (`-c smoke_test_settings.gradle`).
`smoke_test_settings.gradle` script configures plugin management for `build.gradle` so that the plugin cannot be resolved from
a Gradle plugin repository. Instead, it relies on the smoke test to make the plugin under development available
to the test project by sharing a classpath (using Gradle TestKit).

__CAUTION:__ This approach won't work outside of the smoke test, it will break the build because the plugin jar won't be resolved.
