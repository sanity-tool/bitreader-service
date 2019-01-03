FROM sanitytool/bitreader-service

ENTRYPOINT []
CMD java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -Djava.security.egd=file:/dev/./urandom -Djava.library.path=/ -jar /app.jar --server.port=$PORT
