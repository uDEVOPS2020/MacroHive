#!/bin/sh
#
export PATH="$HOME/bin:$HOME/.local/bin:$PATH"
export PATH="$PATH:/mnt/c/Program\ Files/Docker/Docker/resources/bin"
alias docker=docker.exe
alias docker-compose=docker-compose.exe

mvn clean package -DskipTests
docker build --tag=utest-microservice:latest .

docker-compose up