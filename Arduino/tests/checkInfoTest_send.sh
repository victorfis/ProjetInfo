#! /bin/bash

# @Project Name: Watch'INT-Arduino
# @author: CHEN Muyao

# To do the test, you should execute checkInfoTest_open.sh first then open another terminal to execute checkInfoTest_send

echo IOOO >/dev/ttyACM0; # clear all
sleep 4
echo I1OO SMS_OK >/dev/ttyACM0
sleep 4
echo I010 Mail_OK >/dev/ttyACM0
sleep 4
echo I020 2 Mail also OK >/dev/ttyACM0
sleep 4
echo I003 3 rappels OK >/dev/ttyACM0
sleep 4
echo I000 >/dev/ttyACM0
