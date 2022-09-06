#!/bin/sh
#
curl -X POST -H "Accept: application/text" -T ./initFiles/config.txt http://localhost:11111/configuration?filename=config.txt
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_case_studies.txt http://localhost:11111/jsonfilestxt?filename=JSONFiles.txt
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_case_studies/slimV3.json http://localhost:11111/specification?filename=slim.json
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_case_studies/airline.json http://localhost:11111/specification?filename=airline.json
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_case_studies/streaming.json http://localhost:11111/specification?filename=streaming.json
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_case_studies/petclinic.json http://localhost:11111/specification?filename=petclinic.json
curl -X POST -H "Accept: application/text" -T ./initFiles/JSONFiles_case_studies/safrs.json http://localhost:11111/specification?filename=safrs.json