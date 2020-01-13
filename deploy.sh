echo "Copying file....."
rsync -z ./out/artifacts/fetch_ta_data_jar/fetch_ta_data.jar pegasis@i.pegasis.site:/home/pegasis/yrdsb_ta_server/fetch_ta_data.jar.temp || exit
echo "File copied"

ssh i.pegasis.site '
cd /home/pegasis/yrdsb_ta_server/ || exit
screen -S ta-server -p 0 -X stuff "^C"
sleep 5
rm ./fetch_ta_data.jar
cp ./fetch_ta_data.jar.temp ./fetch_ta_data.jar
screen -S ta-server -d -m java -jar fetch_ta_data.jar server -p --control-port 5007
'
echo "Server restarted"