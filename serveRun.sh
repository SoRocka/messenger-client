#!/bin/bash

# Очистка и сборка проекта
echo "Starting Maven clean and install..."
mvn clean install

if [ $? -eq 0 ]; then
    echo "Maven build successful, starting Spring Boot..."
    mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx1024m"
else
    echo "Maven build failed. Exiting..."
    exit 1
fi
