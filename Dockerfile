FROM openjdk:9-jdk
VOLUME /tmp
ARG JAR_FILE
ARG SHARED
ADD ${JAR_FILE} app.jar
ADD ${SHARED}/${DLL_NAME} ${DLL_NAME}
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Djava.library.path=/","-jar","/app.jar"]