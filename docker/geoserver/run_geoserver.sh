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

#!/bin/bash

GEOSERVER_DATA_DIR=/opt/geoserver-2.11.2/data_dir
GEOSERVER_HOME=/opt/geoserver-2.11.2

PATH=/usr/sbin:/usr/bin:/sbin:/bin
DESC="GeoServer daemon"
NAME=geoserver
JAVA_HOME=/docker-java-home/jre
JAVA_OPTS="-Xms128m -Xmx4096m -server"

ARGS="$JAVA_OPTS -DGEOSERVER_DATA_DIR=$GEOSERVER_DATA_DIR -Djava.awt.headless=true"

echo "Init...."

while [ ! -f "$GEOSERVER_HOME/start.jar" ]
do
  echo "Waiting for geoserver volume to be ready..."
  sleep 2
done

echo "Starting...."

cd $GEOSERVER_HOME
java -jar start.jar $ARGS

