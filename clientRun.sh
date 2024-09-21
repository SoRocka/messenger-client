#!/bin/bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.LoginClientWindow" -Dexec.args="-Xmx1024m"
