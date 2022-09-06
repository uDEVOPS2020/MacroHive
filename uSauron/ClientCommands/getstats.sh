#!/bin/sh
#
curl -s -X GET http://localhost:11112/getStats?format=json --output ./output/stats.json