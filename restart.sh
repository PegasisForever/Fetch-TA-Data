#!/usr/bin/env bash

echo "Restarting server....."

ssh k.pegasis.site '
cd /home/pegasis/yrdsb_ta_server/ || exit
sudo docker-compose restart || exit
'

echo "Server restarted."
