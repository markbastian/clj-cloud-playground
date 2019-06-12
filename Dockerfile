FROM openjdk:8-alpine

COPY target/clj-cloud-playground-0.1.0-SNAPSHOT-standalone.jar /bin/app.jar

EXPOSE 3000
EXPOSE 3001

CMD ["java", "-jar", "/bin/app.jar"]
