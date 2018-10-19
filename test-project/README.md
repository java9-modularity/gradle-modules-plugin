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

It will use a current plugin version from Gradle maven repository to compile the test project with
modules and run the unit tests.

Internal test project
===

This mode is enabled in `ModulePluginSmokeTest` by passing an extra parameter (`-PINTERNAL_TEST_IN_PROGRESS`) that disables
dependency resolution of a plugin jar. The test makes the plugin under development available
to the test project by sharing a classpath (using Gradle TestKit).

__CAUTION:__ If the parameter is used outside of test, it will break the build because the plugin jar won't be resolved.