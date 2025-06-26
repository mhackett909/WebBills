FROM amazoncorretto:17.0.15@sha256:b901e71d1e74cc8c2fe65d320b2293a4a14afe8c5cec2a0611fc3d3304c47dcceac
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
