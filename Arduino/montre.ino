/* 
 *  @Project Name: Watch'INT
 *  @author: CHEN Muyao, FIS Victor
 *  @date: 13/03/2017
 *  @discription: Prototype d'une montre connectée
 *  
 *  This prototype is aimed at showing the time on the OLED screen by synchronizing the time via Bluetooth
 *  Time-sychronizing messages consists of the letter T followed by ten digit time (as seconds since Jan 1 1970)
 *  For example, we can send the text on the next line using Bluetooth Terminal to set the clock to noon Jan 1 2013: T1357041600  
 *  
 *  Bluetooth TX-10(RX)
 *  Bluetooth RX-11(TX)
 *  Screen SDA_PIN-A4
 *  Screen SCL_PIN-A5
 */ 
 
#include <TimeLib.h>    // Library of Time

// Library of the screen
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>


#include <SoftwareSerial.h>    // Library of software serial communication

// Define the pins for RX, TX on the Arduino board respectively to connect with Bluetooth
SoftwareSerial mySerial(10, 11);    

// Define the pins for the I²C Bus to communicate with OLED
#define SDA_PIN A4    //SDA pin of OLED connected to A4
#define SCL_PIN A5    //SCL pin of OLED connected to A5

// Define the objet "display" of OLED
#define OLED_RESET 4
Adafruit_SSD1306 display(OLED_RESET);


#define TIME_HEADER  "T"    // Header tag for serial time sync message
#define TIME_REQUEST  7    // ASCII bell character requests a time sync message 

void setup()  {
  // Initialisation of Bluetooth communication module
  mySerial.begin(38400);    // Set the baud of port to 38400 in order to communicate with Bluetooth
  pinMode(13, OUTPUT);    // Define the indicator Led PIN 13 as output
  setSyncProvider(requestSync);    // Set function to call when sync required
  mySerial.println("Waiting for sync message");    // Show the sync require message on the bluetooth terminal
  delay(1000);    // 1 sec delay
  
  // Initialisation of OLED Screen module 
  display.begin(SSD1306_SWITCHCAPVCC, 0x3C);  // initialize with the I2C addr 0x3C (for the 128x64)
  display.clearDisplay(); // show splashscreen
  display.setTextSize(2);    // set the text size
  display.setTextColor(WHITE);    // set the text color as white (white seen as blue actually)
  display.setCursor(10,20);    // set the position of the 1st letter
  display.print("WATCH'INT");    // show the welcome page in waiting for the sync
  display.display();    // update all the changes to the screen

}

void loop(){    
  // if the bluetooth serial communication is established, then process the synchronisation
  if (mySerial.available()) {    // if something is received
    processSyncMessage();    // call a function to react to received messages
  }
 
  // if time status is set
  if (timeStatus()!= timeNotSet) {
    //display.clear();
    display.clearDisplay();
    digitalClockDisplay();    // clock is displayed
  }
  // if the time is set
  if (timeStatus() == timeSet) {
    digitalWrite(13, HIGH); // LED on if synced
  } else {
    digitalWrite(13, LOW);  // LED off if needs refresh
  }
  delay(1000);
}

void digitalClockDisplay(){
  // digital clock display of the time
  display.setTextSize(2);
  display.setTextColor(WHITE);
  display.setCursor(15,20);
  display.print(hour());    // display time
  printDigits(minute());
  printDigits(second());
  // change the line to display the date
  display.setCursor(15,40);
  display.print(day());    //display date
  display.print("/");
  display.print(month());
  display.print("/");
  display.print(year()); 
  display.display();
}

void printDigits(int digits){
  // utility function for digital clock display: prints preceding colon and leading 0
  display.print(":");
  if(digits < 10)    // if the number is between 0 and 9
    display.print('0');    // print a 0 before
  display.print(digits);    // display minutes or seconds
}


void processSyncMessage() {
  unsigned long pctime;
  const unsigned long DEFAULT_TIME = 1357041600; // Jan 1 2013

  if(mySerial.find(TIME_HEADER)) {
     pctime = mySerial.parseInt();
     if( pctime >= DEFAULT_TIME) { // check the integer is a valid time (greater than Jan 1 2013)
       setTime(pctime); // Sync Arduino clock to the time received on the serial port
     }
  }
}

time_t requestSync()
{
  mySerial.write(TIME_REQUEST);  
  return 0; // the time will be sent later in response to serial mesg
}
