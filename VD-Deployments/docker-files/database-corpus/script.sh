#!/bin/bash

mysql -u root -p'$MYSQL_ROOT_PASSWORD' -se 'use $MYSQL_DATABASE;' 2>/dev/null

if [ $? == 0 ] ; 
then
  echo "Database Present - So skipping DB creation"
else
  echo "DB is not present - getting created"
  mysql -u root -p'$MYSQL_ROOT_PASSWORD' --database=$MYSQL_DATABASE < /opt/VD-DDLScripts.sql 
fi
