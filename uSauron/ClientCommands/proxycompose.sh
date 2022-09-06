#!/bin/sh
#
curl -s -X POST -H "Accept: application/text" -T ./compose/$1 http://localhost:11112/proxyCompose --output ./compose/proxied_$1