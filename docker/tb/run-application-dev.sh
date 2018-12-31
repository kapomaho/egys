#!/bin/bash
#
# Copyright © 2016-2018 The Thingsboard Authors
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


if [ "$DATABASE_TYPE" == "cassandra" ]; then
    until nmap $CASSANDRA_HOST -p $CASSANDRA_PORT | grep "$CASSANDRA_PORT/tcp open\|filtered"
    do
      echo "Wait for cassandra db to start..."
      sleep 10
    done
fi

if [ "$DATABASE_TYPE" == "sql" ]; then
    if [ "$SPRING_DRIVER_CLASS_NAME" == "org.postgresql.Driver" ]; then
        until nmap $POSTGRES_HOST -p $POSTGRES_PORT | grep "$POSTGRES_PORT/tcp open"
        do
          echo "Waiting for postgres db to start..."
          sleep 10
        done
    fi
fi

echo "Starting 'Thingsboard'..."

if [ "$DEBUG_MODE" == "false" ]; then
    echo "Rebuilding fat jar with version: $VERSION"
    cd /app
    mvn clean install -DskipTests -Dlicense.skip=true
    echo "Running fat jar..."
    java -agentlib:jdwp=transport=dt_socket,server=y,address=4000,suspend=n -jar ./application/target/thingsboard-2.1.3-boot.jar
elif [ "$DEBUG_MODE" == "true" ]; then
    cd /app
    echo "Running fat jar..."
    java -agentlib:jdwp=transport=dt_socket,server=y,address=4000,suspend=n -jar ./application/target/thingsboard-2.1.3-boot.jar&
    cd /app/ui
    npm start
fi
