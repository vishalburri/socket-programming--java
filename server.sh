#!/bin/bash
if [ "$#" -ne 3 ]; then
    echo "Usage: ./server.sh <maxclients> <host> <portNumber>"
    exit 1
fi
javac Server.java
java Server $1 $2 $3
