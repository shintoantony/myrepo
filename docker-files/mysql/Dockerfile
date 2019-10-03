FROM mysql
MAINTAINER Shinto

ENV MYSQL_DATABASE sample
ENV MYSQL_ROOT_PASSWORD admin

COPY /scripts.sql /docker-entrypoint-initdb.d/


EXPOSE 3306

