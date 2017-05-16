#! /bin/bash

# @Project Name: Watch'INT-Arduino
# @author: CHEN Muyao

# This is the test programme for Time showing.
# This programme should run with TimeTest.ino

exec 3<>/dev/ttyACM0    # Open the UART communication port
sleep 5    # Wait for Arduino setup
date +T%s >&3    # Send the unix time

# Read the time from Arduino
while read line;do
  echo $line
  sleep 5
done <&3
