#!/usr/bin/env bash

docker run -d --name=ta-mongo -p 27017:27017 -v fetch_ta_data_mongo:/data/db -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=password mongo:4.2-bionic
