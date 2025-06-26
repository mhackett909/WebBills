FROM amazoncorretto@sha256:b901e71d1e74cc8c2fe65d320b2293a4a14afe8c5ce
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
