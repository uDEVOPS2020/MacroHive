#!/bin/sh
#
#
curl -s -X GET http://localhost:11111/file?fileID=log --output ./output/log.txt
curl -s -X GET http://localhost:11111/file?fileID=failed --output ./output/failedTests.txt
curl -s -X GET http://localhost:11111/file?fileID=success --output ./output/successTests.txt
curl -s -X GET http://localhost:11111/file?fileID=stats --output ./output/stats.txt
curl -s -X GET http://localhost:11111/file?fileID=features --output ./output/testFeatures.csv
curl -s -X GET http://localhost:11111/file?fileID=testsuite --output ./output/testsuite.txt
curl -s -X GET http://localhost:11111/file?fileID=reliabilities --output ./output/reliabilities.csv