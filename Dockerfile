FROM openjdk:11-jdk
VOLUME /tmp
ARG JAR_FILE
ARG SHARED
ADD ${JAR_FILE} app.jar
ADD ${SHARED}/${DLL_NAME} ${DLL_NAME}
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-Djava.security.egd=file:/dev/./urandom", "-Djava.library.path=/", "-jar", "/app.jar"]
