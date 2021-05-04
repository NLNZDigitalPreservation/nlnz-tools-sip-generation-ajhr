<h1 align="center">nlnz-tools-sip-generation-ajhr</h1>

## Introduction
The tool is to format the AJHR contents and transfer to the contents accepted by Rosetta:
1. Walk through the original folder, and copy the related contents to the root location which are used to deposit to Rosetta.
2. The mets.xml is generated for each SIP.

## Installation
### JDK Installation
Java11 is recommended, please use: java -version to make sure JDK is installed and to check the version of JDK. 

### AJHR SIP Generation Tool
- Build from source code:
Users could build the execution java application from source code:
 ./gradlew clean build -x test

- Download the built application: 
Users could get a built jar application from: Y:\ndha\pre-deposit_prod\frank\software\ajhr-XXXXX.jar.


## Usage
java -Xms1G -Xmx4G -jar build/libs/ajhr-1.0.0-RELEASE.jar --srcDir="Y:/ndha/pre-deposit_prod/frank/AJHR_ORIGINAL" --destDir="C:/Users/leefr/workspace/tmp/AJHR_TEST" --forceReplace=true --maxThreads=5

- srcDir: The srcDir is the original folder. For the test, the examples can be used: Y:/ndha/pre-deposit_prod/frank/AJHR_ORIGINAL
- destDir: The destDir is the root location which are used to deposit to Rosetta.  For the test, it can be a temp directory.
- forceReplace: If it’s true, all the files in the destDir will be deleted before processing SIP generation.  If it’s false, the existing SIP will be kept and the related SIP process will be skiped.
- maxThreads: The concurrent threads used to process the SIP generation.