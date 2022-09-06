#!/bin/sh
#
curl -s -X GET http://localhost:11112/getBugReport?format=json --output ./output/bugReport.json