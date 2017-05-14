#! /bin/bash

# @Project Name: Watch'INT-Arduino
# @author: CHEN Muyao

# This is the test programme for Bluetooth communication.
# This programme should run with BluetoothTest.ino

exec 3<>/dev/ttyACM0    # Open the UART communication port
sleep 5    # Wait for Arduino setup

echo "Test begins"

echo "We will send 1 to Arduino"
echo 1 >&3
sleep 5
read i <&3
if [ i -eq 1 ]; then
  echo "Success"
else; then
  echo "Error"
fi

#while true; do
#  echo "This is the message sent back by Arduino" >&3
#  sleep 5    # Wait for the response

  # Read the message from Arduino
#  read line <&3
#  echo $line
#done
