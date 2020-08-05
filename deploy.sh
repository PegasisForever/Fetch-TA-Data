#!/usr/bin/env bash

echo "Deploying server....."

docker push pegasis0/fetch-ta:latest

ssh k.pegasis.site '
cd /home/pegasis/yrdsb_ta_server/ || exit
sudo docker-compose down || exit
sudo docker pull pegasis0/fetch-ta:latest || exit
sudo docker-compose up -d || exit
'

echo "Server deployed."
