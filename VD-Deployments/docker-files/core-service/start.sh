#!/bin/bash

cd /vdbkp/ 
DIR_LOGS="/vdbkp"

if [ "$(ls -A $DIR_LOGS)" ]; then
rm -rf /tmp/virtual-dispatcher/logs/
ln -s $DIR_LOGS/logs /tmp/virtual-dispatcher/
else
mv /tmp/virtual-dispatcher/logs/ $DIR_LOGS/
ln -s $DIR_LOGS/logs /tmp/virtual-dispatcher/
fi

echo 'Virtual Dispatcher Core Micro Service Started'


nohup java -javaagent:/home/newrelic/newrelic.jar -jar /home/CoreService.jar >> /tmp/virtual-dispatcher/logs/vd-core-logs.logs 2>&1 &  cron && tail -f /tmp/virtual-dispatcher/logs/cron.log

