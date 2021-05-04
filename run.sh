#!/bin/sh

export minMemory="1024m"
export maxMemory="4096m"
export srcDir="/media/sf_frank/AJHR_ORIGINAL"
export destDir="/media/sf_frank/AJHR_TEST"
export forceReplace=true
export maxThreads=1

java -Xms${minMemory} -Xmx${maxMemory} \
     -jar build/libs/ajhr-1.0.0-RELEASE.jar \
     --srcDir=${srcDir} \
     --destDir=${destDir} \
     --forceReplace=${forceReplace} \
     --maxThreads=${maxThreads}
