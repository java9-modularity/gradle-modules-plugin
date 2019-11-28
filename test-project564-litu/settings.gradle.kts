/* see ../ReadMe.txt for content and purpose of this project. */
rootProject.name = "foo.litu"

pluginManagement {
  plugins {
    id("org.javamodularity.moduleplugin") version "1.6.1-SNAPSHOT"
  } // end plugins
  
  repositories {
    mavenLocal()
    gradlePluginPortal()
  }
}