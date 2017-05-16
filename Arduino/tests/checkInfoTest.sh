#! /bin/bash

# @Project Name: Watch'INT-Arduino
# @author: CHEN Muyao

# This is the test programme for Time showing.
# This programme should run with TimeTest.ino

exec 3<>/dev/ttyACM0    # Open the UART communication port
sleep 2    # Wait for Arduino setup

echo "ready"

#echo 1 >&3
#echo "OK1"
#sleep 2
# make sure the connection is established
while true; do
  read line <&3
  echo $line
  line2 = $(echo $x|grep -o [0-9])
    if [ $line2 -eq 0 ]; then
      echo "start"
      break;
  #  else echo "Error establishing la connection";exit 1
    fi
    sleep 5
done


#
echo I100OK >&3
while true; do
  read cond1 <&3
  echo $cond1
#  if [ $cond1 == "100" ]; then
#    echo "notif ok"
#  elif [[ $condi1 == "A" ]]; then
#    echo "content ok"
#  fi
#  sleep 5
done
