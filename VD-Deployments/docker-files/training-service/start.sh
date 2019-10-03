#!/bin/bash

cd /vdbkp/
mkdir logs
mkdir data

DIR_LOGS="/vdbkp/logs"
DIR_DATA="/vdbkp/"

if [ "$(ls -A $DIR_LOGS)" ]; then
rm -rf /tmp/virtual-dispatcher/logs/vd-training-logs.logs
ln -s $DIR_LOGS/vd-training-logs.logs /tmp/virtual-dispatcher/logs/
else
mv /tmp/virtual-dispatcher/logs/vd-training-logs.logs $DIR_LOGS/
ln -s $DIR_LOGS/vd-training-logs.logs /tmp/virtual-dispatcher/logs/
fi

if [ "$(ls -A $DIR_DATA)" ]; then
rm -rf /tmp/virtual-dispatcher/data/
ln -s $DIR_DATA/data /tmp/virtual-dispatcher/
else
mv /tmp/virtual-dispatcher/data/ $DIR_DATA/
ln -s $DIR_DATA/data /tmp/virtual-dispatcher/
fi

echo 'Virtual Dispatcher Training Micro Service Started'
nohup java -javaagent:/home/newrelic/newrelic.jar -jar /home/TrainingService.jar >> /tmp/virtual-dispatcher/logs/vd-training-logs.logs 2>&1
#print 'Virtual Dispatcher Training Micro Service Started'
