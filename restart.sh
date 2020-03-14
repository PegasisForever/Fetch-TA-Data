#!/usr/bin/env bash

echo "Restarting server"

ssh j.pegasis.site '
cd /home/pegasis/yrdsb_ta_server/ || exit
screen -S ta-server -p 0 -X stuff "^C"
sleep 5
screen -S ta-server -d -m java -XX:-OmitStackTraceInFastThrow -jar fetch_ta_data.jar server -p --control-port 5007
'
echo "Server restarted"
