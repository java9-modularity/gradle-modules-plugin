Compared to "gradle-modules-plugin" version 1.6.0 this folder contains several new sub-folders
with various (rather) small projects. Each of those additional sub-folders is explained hereafter
briefly with reason and purpose.

All combinations of the following environments are tested:
Gradle: versions
  - 5.3.1: Gradle version used by "gradle-modules-plugin" version 1.6.0
  - 5.6.4: Latest 5.x version of Gradle
  - 6.0.1: As of 2019-11-28 current Gradle release

Eclipse: version 2019-09 (as aof 2019-09-28 current Eclipse release)

OS:
  - Windows 7 (widley used)
  - Windows 10 (also widely used)
  - ubuntu 16.04 LTS (because I have access to it and whant to check the UN*X world as well)


General remarks:
The name of the sub-folders follow the following principle:
  prefix = test-project
  xyz    = indicating the gradle version which is used in that subfolder,
           e.g: 5.3.1 => gradle version 5.3.1, 601 => gradle version 6.0.1
  -li    = hyphe followed by "li" indicating that these sub-folders contain gradle projects
           providing a library
  ?      = single character stating the (main) purpose of the gradle project
           b => a library consuming other libraries of type t or m
           m => a library consumed / used in the main source-set of another library
           t => a library consumed / used in the test source-set of another library
  ?      = another single character indicating whether a project is modular or not and which
           test engines are used by that project in the test source-set. Thus modular as well as
           non-modular is covered as well as all combinations for using test-engines JUnit 4,
           JUnit 5 and TestNG
           - a = 01 => non-modular                   TestNG
           - b = 02 => non-modular          JUnit 5
           - ...
           - t = 20 =>   modular                     TestNG
           - u = 21 =>   modular            JUnit 5
           - v = 22 =>   modular   JUnit 4
           - w = 23 =>   modular            JUnit 5, TestNG
           - x = 24 =>   modular   JUnit 4,          TestNG
           - y = 25 =>   modular   JUnit 4, JUnit 5
           - z = 26 =>   modular   JUnit 4, JUnit 5, TestNG

All projects where created by the "gradle init" task where the same version of gradle is used
as later on in the project.

###################################################################################################

Lessons learned:
a. As shown in project "test-project531-litu" Gradle's eclipse plugin in Gradle version 5.3.1
   doesn't create appropriate Eclipse files for modular projects.
   This problem is NOT fixed by "gradle-modules-plugin" in version 1.6.0.
   This problem is fixed in the contribution on branch afi33.
b. As shown in projects e.g. "test-project564-litu" and "test-project601-litu" Gradle's eclipse
   plugin in Gradle versions 5.6.4 and 6.0.1 is able to create appropriate Eclipse files for
   "simple" projects.
c. As shown in project "test-project601-libu" Gradle's eclipse plugin in the current Gradle version
   is not able to create appropriate Eclipse files for modular projects.
   This problem is NOT fixed by "gradle-modules-plugin" in version 1.6.0.
   This problem is fixed in the contribution on branch afi33.

###################################################################################################

test-project531-litu
  Properties
  - Gradle version 5.3.1
  - project provides a library intentded to be consumed by other projects in their test source-set
  - project uses JUnit 5 test engine only

###################################################################################################

test-project564-litu
  Properties
  - Gradle version 5.6.4
  - project provides a library intentded to be consumed by other projects in their test source-set
  - project uses JUnit 5 test engine only

###################################################################################################

test-project601-libu
  Properties
  - Gradle version 6.0.1
  - consumes a modular libary in main source-set (here from test-project601-limu)
  - project uses JUnit 5 test engine only

###################################################################################################

test-project601-litu
  Properties
  - Gradle version 6.0.1
  - project provides a library intentded to be consumed by other projects in their test source-set
  - project uses JUnit 5 test engine only

###################################################################################################

