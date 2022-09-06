#!/bin/sh
#
curl -X POST -H "Accept: application/text" -T ./GND_T/gndt.json http://localhost:11112/upload?option=groundtrouth
base_v=$(curl -s -X POST -H "Accept: application/text" -T ./dependencies/base_v.json http://localhost:11112/dependenciesCov)
base_i=$(curl -s -X POST -H "Accept: application/text" -T ./dependencies/base_i.json http://localhost:11112/dependenciesCov)
pairwise_v=$(curl -s -X POST -H "Accept: application/text" -T ./dependencies/pairwise_v.json http://localhost:11112/dependenciesCov)
pairwise=$(curl -s -X POST -H "Accept: application/text" -T ./dependencies/pairwise.json http://localhost:11112/dependenciesCov)

echo "\"Technique\",\"depCoverage\"" >> ./output/dependenciesCov.csv
echo "base_valid,$base_v" >> ./output/dependenciesCov.csv
echo "base_invalid,$base_i" >> ./output/dependenciesCov.csv
echo "pairwise_valid,$pairwise_v" >> ./output/dependenciesCov.csv
echo "pairwise,$pairwise" >> ./output/dependenciesCov.csv