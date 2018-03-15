[![Build Status](https://travis-ci.org/sanity-tool/bitreader-service.svg?branch=master)](https://travis-ci.org/sanity-tool/bitreader-service) [![codecov](https://codecov.io/gh/sanity-tool/bitreader-service/branch/master/graph/badge.svg)](https://codecov.io/gh/sanity-tool/bitreader-service) [![Code quality](https://sonarcloud.io/api/project_badges/measure?project=ru.urururu%3Abitreader-service&metric=alert_status)](https://sonarcloud.io/dashboard?id=ru.urururu%3Abitreader-service)

## Run it:
`./mvnw spring-boot:run` or `docker run -d -p 8080:8080 sanitytool/bitreader-service`
## Use it:
`curl --header "Content-Type:application/octet-stream" --trace-ascii debugdump.txt --data-binary @target/debug/parser/memory.cpp/result.bc http://localhost:8080/parse`
## Documentation:
If you've made it running you can access real api docs at: `http://localhost:8080/v2/api-docs`
