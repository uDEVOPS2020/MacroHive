#!/bin/sh
#
curl -X POST -H "Accept: application/text" -T ./initFiles/config.txt http://localhost:11111/configuration?filename=config.txt
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_ftgo.txt http://localhost:11111/jsonfilestxt?filename=JSONFiles.txt
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_ftgo/swagger_8081.json http://localhost:11111/specification?filename=swagger_8081.json
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_ftgo/swagger_8082.json http://localhost:11111/specification?filename=swagger_8082.json
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_ftgo/swagger_8083.json http://localhost:11111/specification?filename=swagger_8083.json
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_ftgo/swagger_8084.json http://localhost:11111/specification?filename=swagger_8084.json
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_ftgo/swagger_8085.json http://localhost:11111/specification?filename=swagger_8085.json
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_ftgo/swagger_8086.json http://localhost:11111/specification?filename=swagger_8086.json
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_ftgo/swagger_8089.json http://localhost:11111/specification?filename=swagger_8089.json
curl -X GET http://localhost:11111/execute?execOption=all
curl -X GET http://localhost:11111/clean