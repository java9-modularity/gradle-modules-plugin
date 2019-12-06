Improve .classpath file created by Gradle's eclipse plugin

## Rational for this contribution

Currently Gradle doesn't support modular Java (which is the main reason
for "gradle-modules-plugin"). Thus it is no wonder that the .classpath
file created by Gradle's eclipse plugin requires (currently manual)
changes in modular Java projects.

The problems discovered so far are outlined in (rather simple) projects
in new sub-folders test-project* (see below for more information).

The intention of this contribution is to improve the .classpath file
created by Gradle's eclipse plugin such that no manual corrections are
necessary.

For background information and starting point, see
* https://github.com/java9-modularity/gradle-modules-plugin/issues/33
* https://docs.gradle.org/current/userguide/eclipse_plugin.html

This contribution was tested in the following environments:
Gradle:
* 5.3.1: Gradle version used by "gradle-modules-plugin" version 1.6.0
* 5.6.4: Latest 5.x version of Gradle
* 6.0.1: As of 2019-11-28 current Gradle release

Eclipse: version 2019-09 (as of 2019-09-28 current Eclipse release)

Operating systems:
* Windows 10 (widely used)
* ubuntu 16.04 LTS (because I have access to it and whant to check the
  UN*X world as well)

## Backword compatibility

If this contribution is incorprated in a new version of
"gradle-modules-plugin", then that new version is fully backward
compatible with version 1.6.0, because the code proposed by this
contribution is disabled by an extension (default value false).
Only if a build-srcipt enables the new code via setting the appropriate
extension then .classpath files are improved by code from this
contribution.
How a build-srcipt might enable is shown e.g. in the build-script of
project test-project531-litu.

## Content

Two new classes and correpsonding test-classes. That is the essential
part of the contribution.

Furthermore compared to "gradle-modules-plugin" version 1.6.0 this
project containsseveral new sub-folders with various (rather)
small projects. Each of those additional sub-folders is explained
hereafter briefly with reason and purpose. These new sub-folders show
and highlight problems with the current behaviour of Gradle's eclipe
plugin. None of these sub-folders are necessary to be published together
with an updated version of "gradle-modules-plugin" in case this
contribution is accepted.

## General remarks

All projects in the additional sub-folders where created by the
"gradle init" task where the same version of gradle is used as
later on in the project.

The name of the sub-folders follow the following principle:
  prefix = test-project
  xyz    = indicating the gradle version which is used in that subfolder,
           e.g: 5.3.1 => gradle version 5.3.1, 601 => gradle version 6.0.1
  -li    = hyphen followed by "li" indicating that these sub-folders
           contain gradle projects providing a library
  ?      = single character stating the (main) purpose of the gradle project
           b => a library consuming other libraries of type t or m
           m => a library used in the main source-set of another library
           t => a library used in the test source-set of another library
  ?      = another single character indicating whether a project is
           modular or not and which test engines are used by that
           project in the test source-set. Thus modular as well as
           non-modular is covered as well as all combinations for using
           test-engines JUnit 4, JUnit 5 and TestNG
           - a = 01 => non-modular          JUnit 5
           - b = 02 => non-modular
           - ...
           - t = 20 =>   modular                     TestNG
           - u = 21 =>   modular            JUnit 5
           - v = 22 =>   modular   JUnit 4
           - w = 23 =>   modular            JUnit 5, TestNG
           - x = 24 =>   modular   JUnit 4,          TestNG
           - y = 25 =>   modular   JUnit 4, JUnit 5
           - z = 26 =>   modular   JUnit 4, JUnit 5, TestNG

## Lessons learned

a. As shown in project "test-project531-litu" Gradle's eclipse plugin in
   Gradle version 5.3.1 doesn't create appropriate Eclipse files for
   modular projects.
   This problem is NOT fixed by "gradle-modules-plugin" in version 1.6.0.
   This problem is fixed in the contribution on branch afi33.
b. As shown in projects e.g. "test-project564-litu" and
   "test-project601-litu" Gradle's eclipse plugin in Gradle 5.6.4 and
   6.0.1 is able to create appropriate Eclipse files for "simple" projects.
c. As shown in project "test-project601-libu" Gradle's eclipse plugin in
   the current Gradle version is not able to create appropriate Eclipse
   files for modular projects if external dependencies exist.
   This problem is NOT fixed by "gradle-modules-plugin" in version 1.6.0.
   This problem is fixed in the contribution on branch afi33.

## Description of additional projects

test-project531-litu
  Properties
  - Gradle version 5.3.1
  - modular, provides library consumed by other projects in test source-set
  - project uses JUnit 5 test engine only

########################################################################

test-project564-litu
  Properties
  - Gradle version 5.6.4
  - modular, provides library consumed by other projects in test source-set
  - project uses JUnit 5 test engine only

########################################################################

test-project601-libu
  Properties
  - Gradle version 6.0.1
  - modular project consumes libaries in main source-set
  - project uses JUnit 5 test engine only

########################################################################

test-project601-lima
  Properties
  - Gradle version 6.0.1
  - non-modular, provides library consumed by other projects their main
    source-set
  - project uses JUnit 5 test engine only

########################################################################

test-project601-limu
  Properties
  - Gradle version 6.0.1
  - modular, provides library consumed by other projects in main source-set
  - project uses JUnit 5 test engine only

########################################################################

test-project601-litu
  Properties
  - Gradle version 6.0.1
  - modular, provides library consumed by other projects in test source-set
  - project uses JUnit 5 test engine only