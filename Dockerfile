FROM openjdk:11-jdk AS builder

RUN apt-get update && apt-get -y install gcc g++ make ccache swig libncurses5-dev
RUN git clone -b saving-debug-80 --depth 1 --progress --verbose https://github.com/okutane/llvm.git
WORKDIR llvm
RUN ./build.sh

COPY pom.xml /tmp/
COPY src /tmp/src/
COPY generate.sh /tmp/
COPY mvnw /tmp/
COPY .mvn /tmp/.mvn
WORKDIR /tmp/
RUN ./mvnw package -DskipTests

FROM openjdk:11-jdk
VOLUME /tmp
COPY --from=builder /tmp/target/bitreader-service-0.0.1-SNAPSHOT.jar app.jar
COPY --from=builder /tmp/target/native/shared/libirreader.so libirreader.so
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-Djava.security.egd=file:/dev/./urandom", "-Djava.library.path=/", "-jar", "/app.jar"]
