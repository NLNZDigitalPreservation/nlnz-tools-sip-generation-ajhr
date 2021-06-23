#!/bin/sh

export minMemory="1024m"
export maxMemory="4096m"

java -Xms${minMemory} -Xmx${maxMemory} -jar build/libs/ajhr-1.1.0-RELEASE.jar --spring.config.location=file:conf/application.properties
