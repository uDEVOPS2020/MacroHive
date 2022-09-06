#!/bin/sh
#
#docker exec -it 66c833a9de52 /bin/sh
#
#docker cp 66c833a9de52:sessionResources\JSONDoc\Files\swagger_8082.json .
#
export PATH="$HOME/bin:$HOME/.local/bin:$PATH"
export PATH="$PATH:/mnt/c/Program\ Files/Docker/Docker/resources/bin"
alias docker=docker.exe
alias docker-compose=docker-compose.exe

mvn clean package -DskipTests
docker build --tag=utest-microservice:latest .
