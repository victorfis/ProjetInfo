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

//Define pin for buzzer
#define buzzer 9

// Define the objet "display" of OLED
#define OLED_RESET 4
Adafruit_SSD1306 display(OLED_RESET);

// Time message define
#define TIME_HEADER  "T"    // Header tag for serial time sync message
#define TIME_REQUEST  7    // ASCII bell character requests a time sync message 

// Info message define
#define INFO_HEADER "I"    // Header tag for notification message
int typeInfo = 0;    // Indicator for the type of infomation folowing INFO_HEADER
char content[20];

const int BUTTON = 7;    //button define

void setup()  {

  // Initialisation of buzzer
  pinMode(buzzer, OUTPUT); // Set buzzer - pin 9 as an output

  // Initialisation of button
  pinMode(BUTTON, INPUT);

  // Initialisation of Bluetooth communication module
  mySerial.begin(38400);    // Set the baud of port to 38400 in order to communicate with Bluetooth
  pinMode(13, OUTPUT);    // Define the indicator Led PIN 13 as output
  
  // Time synchronisation request
  setSyncProvider(requestSync);    // Set function to call when sync required
  mySerial.println("TIME");    // Show the sync require message on the bluetooth terminal
  
  delay(1000);    // 1 sec delay
  
  // Initialisation of OLED Screen module 
  display.begin(SSD1306_SWITCHCAPVCC, 0x3C);  // initialize with the I2C addr 0x3C (for the 128x64)
  display.clearDisplay(); // show splashscreen
  display.setTextSize(2);    // set the text size
  display.setTextColor(WHITE);    // set the text color as white (white seen as blue actually)
  display.setCursor(10,20);    // set the position of the 1st letter
  display.print("WATCH'INT");    // show the welcome page in waiting for the sync
  
  tone(buzzer, 2093); // 0.1 sec of buzz indicating the watch boot
  delay(100);        
  noTone(buzzer);
  
  display.display();    // update all the changes to the screen

  // if the bluetooth serial communication is established, then process the synchronisation
  while (!mySerial.available()) {    // if sync signal is not received
    delay(10);    // wait for the signal 
  }
  processSyncMessage();    // call a function to react to received messages
  
  tone(buzzer, 2093);    // Buzz indicating successful sync 
  delay(100);
  tone(buzzer, 1568); 
  delay(100);
  noTone(buzzer);
}

/*----------------------------------RESET---------------------------------------------*/
void (*resetFunc)(void) = 0;    // Set the pointer to the beginning in order to reset
/*------------------------------------------------------------------------------------*/

void loop(){ 
  // Detect the reset signal
  int held = 0;
  while ((digitalRead(BUTTON) == HIGH) && (held < 10)){
    delay(100);
    held++;
  }

  if(held >= 10){
    resetFunc();    // if the button is held pushed for 1 sec, watch reset
  }

  checkInfo();    // Notification checking
  delay(800);
  timeshow();    // Time showing

}

/*-------------------------------------AFFICHAGE DU TEMPS ET DE L'HEURE-------------------------------------------------------*/
void timeshow(){
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
/*------------------------------------------------------------------------------------------------------------------------------*/

/*-----------------------------------------CHECK INFO--------------------------------------------------------------------*/
void checkInfo() {
  /* Check for new notification*/
  int typeInfo_before = typeInfo;
  
  if(mySerial.available()) {
    if (mySerial.find(INFO_HEADER))
    {
      typeInfo = mySerial.parseInt();    // extract type of information
    }
  }
      
    
    int typeInfo_local = typeInfo;
    int Nb_S;
    int Nb_M;
    int Nb_R;

    Nb_R = typeInfo_local%10;
    typeInfo_local = typeInfo_local/10;
    Nb_M = typeInfo_local%10;
    typeInfo_local = typeInfo_local/10;
    Nb_S = typeInfo_local%10;
    showNotif(Nb_S, Nb_M, Nb_R);

    if (typeInfo_before != typeInfo)
    {

    if ((Nb_S != 0) || (Nb_M != 0) || (Nb_R != 0))
    {
      for (int i = 0; i < 20 ; ++i)
      {
        content[i] = mySerial.read();
        delay(10);
      }

    } 
    else 
    {
      for (int i = 0; i < 20 ; ++i)
        {
          content[i] = '\0';
        }
    }  
  } 
    showContent(content);
  
  
}

void showNotif(int Nb_S, int Nb_M, int Nb_R) {
  display.setTextSize(1);
  display.setTextColor(WHITE);
  display.setCursor(1,1);
  display.print("S : ");
  display.print(Nb_S);
  display.print("  ");
  display.print("M : ");
  display.print(Nb_M);
  display.print("  ");
  display.print("R : ");
  display.print(Nb_R);
  display.display();
}

void showContent(char content[]) {
  display.setTextSize(1);
  display.setTextColor(WHITE);
  display.setCursor(10,10);
  for (int i = 0; i < 20; ++i)
  {
    display.print(content[i]);
  }
  display.display();
}
/*--------------------------------------------------------------------------------------------------------*/
