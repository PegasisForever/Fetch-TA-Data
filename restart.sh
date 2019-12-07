echo "Restarting server"

ssh i.pegasis.site '
cd /home/pegasis/yrdsb_ta_server/ || exit
screen -S ta-server -p 0 -X stuff "^C"
sleep 5
screen -S ta-server -d -m java -jar fetch_ta_data.jar server
'
echo "Server restarted"