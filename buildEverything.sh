#!/bin/sh

# The purpose of this shell-srcipt is to build all the projects in this folder.
#
# In particular the following actions are performed:
# a. gradlew build pushToMavenLocal
# b. gradlew cleanEclipse eclipse

# Note: Multiline comments could start with : ' and end with # '

#: '
# build   gradle-modules-plugin ...................................................................
echo "build gradle-modules-plugin" && \
./gradlew build publishToMavenLocal && \
echo "-------------------------------------------------------" && \
echo "create Eclipse files" && \
./gradlew cleanEclipse eclipse

returnValue=$?
if [ $returnValue -ne 0 ]; then
  echo "Return code was not zero but ${returnValue}."
  exit -1
fi # ______________________________________________________________________________________________
# '

#: '
# build   531-litu ................................................................................
echo "-------------------------------------------------------" && \
echo "build 531-litu" && \
cd test-project531-litu && \
./gradlew build publishToMavenLocal && \
echo "-------------------------------------------------------" && \
echo "create Eclipse files" && \
./gradlew cleanEclipse eclipse

returnValue=$?
if [ $returnValue -ne 0 ]; then
  echo "Return code was not zero but ${returnValue}."
  exit -1
else
  cd ..
fi # ______________________________________________________________________________________________
# '

#: '
# build   564-litu ................................................................................
echo "-------------------------------------------------------" && \
echo "build 564-litu" && \
cd test-project564-litu && \
./gradlew build publishToMavenLocal && \
echo "-------------------------------------------------------" && \
echo "create Eclipse files" && \
./gradlew cleanEclipse eclipse

returnValue=$?
if [ $returnValue -ne 0 ]; then
  echo "Return code was not zero but ${returnValue}."
  exit -1
else
  cd ..
fi # ______________________________________________________________________________________________
# '

#: '
# build   601-lima ................................................................................
echo "-------------------------------------------------------" && \
echo "build 601-lima" && \
cd test-project601-lima && \
./gradlew build publishToMavenLocal && \
echo "-------------------------------------------------------" && \
echo "create Eclipse files" && \
./gradlew cleanEclipse eclipse

returnValue=$?
if [ $returnValue -ne 0 ]; then
  echo "Return code was not zero but ${returnValue}."
  exit -1
else
  cd ..
fi # ______________________________________________________________________________________________
# '

#: '
# build   601-limu ................................................................................
echo "-------------------------------------------------------" && \
echo "build 601-limu" && \
cd test-project601-limu && \
./gradlew build publishToMavenLocal && \
echo "-------------------------------------------------------" && \
echo "create Eclipse files" && \
./gradlew cleanEclipse eclipse

returnValue=$?
if [ $returnValue -ne 0 ]; then
  echo "Return code was not zero but ${returnValue}."
  exit -1
else
  cd ..
fi # ______________________________________________________________________________________________
# '

#: '
# build   601-litu ................................................................................
echo "-------------------------------------------------------" && \
echo "build 601-litu" && \
cd test-project601-litu && \
./gradlew build publishToMavenLocal && \
echo "-------------------------------------------------------" && \
echo "create Eclipse files" && \
./gradlew cleanEclipse eclipse

returnValue=$?
if [ $returnValue -ne 0 ]; then
  echo "Return code was not zero but ${returnValue}."
  exit -1
else
  cd ..
fi # ______________________________________________________________________________________________
# '

#: '
# build   601-libu ................................................................................
echo "-------------------------------------------------------" && \
echo "build 601-libu" && \
cd test-project601-libu && \
./gradlew build publishToMavenLocal && \
echo "-------------------------------------------------------" && \
echo "create Eclipse files" && \
./gradlew cleanEclipse eclipse

returnValue=$?
if [ $returnValue -ne 0 ]; then
  echo "Return code was not zero but ${returnValue}."
  exit -1
else
  cd ..
fi # ______________________________________________________________________________________________
# '
