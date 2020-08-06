FROM openjdk:13-alpine

COPY *.jar /app.jar

CMD ["java", "-Dconsole.encoding=UTF-8", "-Dfile.encoding=UTF-8", "-Djenkins", "-Droot_log_level=INFO", "-jar", "/app.jar"]