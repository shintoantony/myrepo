FROM inmbzp5170.in.dst.ibm.com:5000/ubuntu-java8:16.04
MAINTAINER Chayan13<chdebnat@in.ibm.com>
EXPOSE 8080
COPY spring-petclinic-1.5.0.jar /home/javauser/
USER javauser
CMD ["java", "-jar", "/home/javauser/spring-petclinic-1.5.0.jar"]
