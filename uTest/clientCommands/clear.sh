#!/bin/sh
#

if [ $1 -eq 0 ]; then
    curl -X GET http://localhost:11111/clean
    elif [ $1 -eq 1 ]; then
    curl -X GET http://localhost:11111/cleanEnv
fi