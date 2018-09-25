#!/bin/bash
if [ "$#" -ne 3 ]; then
    echo "Usage: ./client.sh <name> <host> <portNumber>"
    exit 1
fi
javac Client.java
java Client $1 $2 $3
