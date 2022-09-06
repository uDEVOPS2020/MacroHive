#!/bin/sh
#
curl -X POST -H "Accept: application/text" -T ./initFiles/config.txt http://localhost:11111/configuration?filename=config.txt
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_ss.txt http://localhost:11111/jsonfilestxt?filename=JSONFiles.txt
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_ss/1.json http://localhost:11111/specification?filename=1.json
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_ss/2.json http://localhost:11111/specification?filename=2.json
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_ss/3.json http://localhost:11111/specification?filename=3.json
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_ss/4.json http://localhost:11111/specification?filename=4.json
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_ss/5.json http://localhost:11111/specification?filename=5.json