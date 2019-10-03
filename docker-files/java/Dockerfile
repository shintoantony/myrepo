FROM docker202020/java-base:v1
MAINTAINER shinto

COPY base.jar /home
RUN chmod 755 /home/base.jar

CMD ["java","-jar","/home/base.jar"]
