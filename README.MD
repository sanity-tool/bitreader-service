## Run it:
`./mvnw spring-boot:run` or `docker run sanitytool/bitreader-service`
## Use it:
curl --header "Content-Type:application/octet-stream" --trace-ascii debugdump.txt --data-binary @target/debug/parser/memory.cpp/result.bc http://localhost:8080/parse
