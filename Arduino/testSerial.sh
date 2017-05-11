#! /bin/bash
exec 3<>/dev/ttyACM0
sleep 5
date +T%s >&3
while read line;do
  echo $line
  sleep 5
done <&3
