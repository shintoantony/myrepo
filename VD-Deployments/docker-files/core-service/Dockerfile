FROM inmbzp7148.in.dst.ibm.com:5000/ubuntu:16.04
MAINTAINER VirtualDispatcherAdmin

USER root
RUN add-apt-repository ppa:webupd8team/java
RUN apt-get update && echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true |  /usr/bin/debconf-set-selections && apt-get install -y oracle-java8-installer

# set java path
ENV JAVA_HOME="/usr/lib/jvm/java-8-oracle"

RUN 	apt-get -y update && apt-get -y upgrade \
	&& apt-get install cron && apt-get -y install wget \
	&& mkdir /usr/local/tomcat \
	&& wget http://www-us.apache.org/dist/tomcat/tomcat-9/v9.0.12/bin/apache-tomcat-9.0.12.tar.gz -O /tmp/tomcat.tar.gz \
	&& cd /tmp && tar xvfz tomcat.tar.gz \
	&& cp -Rv /tmp/apache-tomcat-9.0.12/* /usr/local/tomcat/
RUN /usr/local/tomcat/bin/catalina.sh start

ADD crontab /etc/cron.d/vdcoreservice-cron
RUN chmod 0644 /etc/cron.d/vdcoreservice-cron
RUN mkdir /tmp/virtual-dispatcher
RUN mkdir /tmp/virtual-dispatcher/logs
RUN touch /tmp/virtual-dispatcher/logs/vd-core-logs.logs
RUN touch /tmp/virtual-dispatcher/logs/cron.log

ADD CoreService.jar /home
ADD start.sh /home
RUN chmod 755 /home/start.sh

RUN chmod 755 /home/CoreService.jar

############NewRelic Agent Steps#######################

COPY newrelic /home/newrelic

EXPOSE 8080 2026 443 60001 60000
CMD ["sh","/home/start.sh"]

