#! /bin/bash

# @Project Name: Watch'INT-Arduino
# @author: CHEN Muyao

# To do the test, you should execute checkInfoTest_open.sh first then open another terminal to execute checkInfoTest_send
exec 3<>/dev/ttyACM0    # Open the UART communication port

# Show the contents received by the port
while true; do
  read line <&3
  echo $line
  sleep 0.5
done
