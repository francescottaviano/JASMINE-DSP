FROM openjdk:8-jdk-alpine

VOLUME /tmp

ADD kafkastreams.jar app.jar

EXPOSE 1099

ENTRYPOINT ["java","-jar","/app.jar"]
