FROM openjdk
MAINTAINER Shinto

RUN yum -y update && yum -y upgrade \
	&& yum -y install wget \
	&& mkdir /usr/local/tomcat \
	&& wget http://www-us.apache.org/dist/tomcat/tomcat-9/v9.0.26/bin/apache-tomcat-9.0.26.tar.gz -O /tmp/tomcat.tar.gz \
	&& cd /tmp && tar xvfz tomcat.tar.gz \
	&& cp -Rv /tmp/apache-tomcat-9.0.26/* /usr/local/tomcat/
RUN /usr/local/tomcat/bin/catalina.sh start

EXPOSE 2026
