#!/bin/sh

export minMemory="1024m"
export maxMemory="4096m"
export srcDir="C:/Users/leefr/workspace/tmp/AJHR_ORIGINAL"
export destDir="C:/Users/leefr/workspace/tmp/AJHR_TEST"
export forceReplace=true
export maxThreads=5

java -Xms${minMemory} -Xmx${maxMemory} \
     -jar build/libs/ajhr-0.0.1-SNAPSHOT.jar \
     --srcDir=${srcDir} \
     --destDir=${destDir} \
     --forceReplace=${forceReplace} \
     --maxThreads=${maxThreads}
