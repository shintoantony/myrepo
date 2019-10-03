FROM inmbzp7148.in.dst.ibm.com:5000/ubuntu:16.04
MAINTAINER VirtualDispatcherAdmin

USER root

RUN add-apt-repository ppa:webupd8team/java
RUN apt-get update && echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true |  /usr/bin/debconf-set-selections && apt-get install -y oracle-java8-installer

# set java path
ENV JAVA_HOME="/usr/lib/jvm/java-8-oracle"

RUN apt-get -y update && apt-get -y upgrade 
RUN mkdir /usr/local/tomcat
RUN wget http://www-us.apache.org/dist/tomcat/tomcat-9/v9.0.13/bin/apache-tomcat-9.0.13.tar.gz -O /tmp/tomcat.tar.gz
RUN cd /tmp && tar xvfz tomcat.tar.gz
RUN cp -Rv /tmp/apache-tomcat-9.0.13/* /usr/local/tomcat/
RUN /usr/local/tomcat/bin/catalina.sh start

RUN mkdir /tmp/virtual-dispatcher
RUN mkdir /tmp/virtual-dispatcher/data
RUN mkdir /tmp/virtual-dispatcher/logs
RUN touch /tmp/virtual-dispatcher/logs/vd-training-logs.logs

ADD TrainingService.jar /home
RUN chmod 755 /home/TrainingService.jar
ADD start.sh /home
RUN chmod 755 /home/start.sh
ADD merge.sh /home
RUN chmod 755 /home/merge.sh


############NewRelic Agent Steps#######################

COPY newrelic /home/newrelic

EXPOSE 8080 2025 60001 21 22
CMD ["sh","/home/start.sh"]

