#!/bin/sh
#

curl -X POST -H "Accept: application/text" -T ./GND_T/gndt.json "http://localhost:11112/upload?option=groundtruth&filename=gndt"
curl -X POST -H "Accept: application/text" -T ./GND_T/1.json "http://localhost:11112/upload?option=groundtruth&filename=1"
curl -X POST -H "Accept: application/text" -T ./GND_T/2.json "http://localhost:11112/upload?option=groundtruth&filename=2"
curl -X POST -H "Accept: application/text" -T ./GND_T/3.json "http://localhost:11112/upload?option=groundtruth&filename=3"
curl -X POST -H "Accept: application/text" -T ./GND_T/4.json "http://localhost:11112/upload?option=groundtruth&filename=4"
curl -X POST -H "Accept: application/text" -T ./GND_T/5.json "http://localhost:11112/upload?option=groundtruth&filename=5"
curl -X POST -H "Accept: application/text" -T ./GND_T/6.json "http://localhost:11112/upload?option=groundtruth&filename=6"
curl -X POST -H "Accept: application/text" -T ./GND_T/7.json "http://localhost:11112/upload?option=groundtruth&filename=7"
curl -X POST -H "Accept: application/text" -T ./GND_T/8.json "http://localhost:11112/upload?option=groundtruth&filename=8"
curl -X POST -H "Accept: application/text" -T ./GND_T/9.json "http://localhost:11112/upload?option=groundtruth&filename=9"
curl -X POST -H "Accept: application/text" -T ./GND_T/10.json "http://localhost:11112/upload?option=groundtruth&filename=10"
curl -X POST -H "Accept: application/text" -T ./GND_T/11.json "http://localhost:11112/upload?option=groundtruth&filename=11"
curl -X POST -H "Accept: application/text" -T ./GND_T/12.json "http://localhost:11112/upload?option=groundtruth&filename=12"
curl -X POST -H "Accept: application/text" -T ./GND_T/13.json "http://localhost:11112/upload?option=groundtruth&filename=13"
curl -X POST -H "Accept: application/text" -T ./GND_T/14.json "http://localhost:11112/upload?option=groundtruth&filename=14"
curl -X POST -H "Accept: application/text" -T ./GND_T/15.json "http://localhost:11112/upload?option=groundtruth&filename=15"
curl -X POST -H "Accept: application/text" -T ./GND_T/16.json "http://localhost:11112/upload?option=groundtruth&filename=16"
curl -X POST -H "Accept: application/text" -T ./GND_T/17.json "http://localhost:11112/upload?option=groundtruth&filename=17"
curl -X POST -H "Accept: application/text" -T ./GND_T/18.json "http://localhost:11112/upload?option=groundtruth&filename=18"
curl -X POST -H "Accept: application/text" -T ./GND_T/19.json "http://localhost:11112/upload?option=groundtruth&filename=19"
curl -X POST -H "Accept: application/text" -T ./GND_T/20.json "http://localhost:11112/upload?option=groundtruth&filename=20"
curl -X POST -H "Accept: application/text" -T ./GND_T/21.json "http://localhost:11112/upload?option=groundtruth&filename=21"
curl -X POST -H "Accept: application/text" -T ./GND_T/22.json "http://localhost:11112/upload?option=groundtruth&filename=22"
curl -X POST -H "Accept: application/text" -T ./GND_T/23.json "http://localhost:11112/upload?option=groundtruth&filename=23"
curl -X POST -H "Accept: application/text" -T ./GND_T/24.json "http://localhost:11112/upload?option=groundtruth&filename=24"
curl -X POST -H "Accept: application/text" -T ./GND_T/25.json "http://localhost:11112/upload?option=groundtruth&filename=25"
curl -X POST -H "Accept: application/text" -T ./GND_T/26.json "http://localhost:11112/upload?option=groundtruth&filename=26"
curl -X POST -H "Accept: application/text" -T ./GND_T/27.json "http://localhost:11112/upload?option=groundtruth&filename=27"
curl -X POST -H "Accept: application/text" -T ./GND_T/28.json "http://localhost:11112/upload?option=groundtruth&filename=28"
curl -X POST -H "Accept: application/text" -T ./GND_T/29.json "http://localhost:11112/upload?option=groundtruth&filename=29"
curl -X POST -H "Accept: application/text" -T ./GND_T/30.json "http://localhost:11112/upload?option=groundtruth&filename=30"
curl -X POST -H "Accept: application/text" -T ./GND_T/31.json "http://localhost:11112/upload?option=groundtruth&filename=31"
curl -X POST -H "Accept: application/text" -T ./GND_T/32.json "http://localhost:11112/upload?option=groundtruth&filename=32"
curl -X POST -H "Accept: application/text" -T ./GND_T/33.json "http://localhost:11112/upload?option=groundtruth&filename=33"
curl -X POST -H "Accept: application/text" -T ./GND_T/34.json "http://localhost:11112/upload?option=groundtruth&filename=34"

curl -s -X GET http://localhost:11112/getInfo?format=json --output ./output/info.json
curl -s -X GET http://localhost:11112/getInfo?format=csv --output ./output/info.csv
curl -s -X GET http://localhost:11112/getSpecInfo?conf=services --output ./output/infoXService.json
curl -s -X GET http://localhost:11112/getSpecInfo?conf=pathServices --output ./output/infoXPathMethod.json
curl -s -X GET http://localhost:11112/getSpecInfo?conf=internalcoverage --output ./output/internalcoverage.json
curl -s -X GET http://localhost:11112/getStats?format=json --output ./output/stats.json
curl -s -X GET http://localhost:11112/getBugReport?format=json --output ./output/bugReport.json
curl -s -X GET http://localhost:11112/getDependencies?option=xmicroservice --output ./output/dependenciesXM.json
curl -s -X GET http://localhost:11112/getDependencies?option=all --output ./output/dependencies.json

curl -s -X GET http://localhost:11112/dependenciesCovXM?format=csv --output ./output/dependenciesCovXM.csv
totDepCov=$(curl -s -X POST -H "Accept: application/text" -T ./output/dependencies.json http://localhost:11112/dependenciesCov?filename=gndt)
echo "Total dependencies coverage,$totDepCov" >> ./output/totDepCov.txt

curl -X GET http://localhost:11112/clear