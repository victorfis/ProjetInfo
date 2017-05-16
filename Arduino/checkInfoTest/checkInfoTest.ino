// Info message define
#define INFO_HEADER "I"    // Header tag for notification message
int typeInfo = 0;    // Indicator for the type of infomation folowing INFO_HEADER
char content[20]={"\0"};    // Array to store the content of message or mail

void setup() {
  Serial.begin(9600);
}

void loop() {
//  if(Serial.available()) {
//    if (Serial.read() == "1")
//      Serial.print("1");
//      Serial.println();
//  }
  checkInfo();
}
void checkInfo() {
  // Check for new notification
  int typeInfo_before = typeInfo;    // Store the previous information type

  // Get the notification type
  if (Serial.available()) {
    if (Serial.find(INFO_HEADER))    // Find the header of notification "I"
    {
      typeInfo = Serial.parseInt();    // Extract type of information as integer
    }
  }

  // Divide the integer to get seperately the amount of new notif for each type
  int typeInfo_local = typeInfo;    // Define a local variable to process this calculate
  int Nb_S;    // Define the indicator of SMS
  int Nb_M;    // Define the indicator of Mail
  int Nb_R;    // Define the indicator of Remind

  Nb_R = typeInfo_local % 10;
  typeInfo_local = typeInfo_local / 10;
  Nb_M = typeInfo_local % 10;
  typeInfo_local = typeInfo_local / 10;
  Nb_S = typeInfo_local % 10;

  showNotif(Nb_S, Nb_M, Nb_R);    // Call the function to show the amount of notif
  delay(500);

  // Update the content of the array if there is any change
  if (typeInfo_before != typeInfo)
  {

    if ((Nb_S != 0) || (Nb_M != 0) || (Nb_R != 0))    // If there is any new message, get the content
    {
      for (int i = 0; i < 20 ; ++i)    // Buffer of 20 characters
      {
        content[i] = Serial.read();
        delay(10);
      }
    }
    else    // If there is not any message, show nothing
    {
      for (int i = 0; i < 20 ; ++i)
      {
        content[i] = '\0';
      }
    }
 
  }
  showContent(content);    // Call this function to show the content
  delay(500);
}

// Function showing the Notif on the serial
void showNotif(int Nb_S, int Nb_M, int Nb_R) {
  int notif = Nb_R + 10 * Nb_M + Nb_S * 100;
  Serial.println(notif);
}

// Function showing the content of message on the serial
void showContent(char content[]) {
  for (int i = 0; i < 20; ++i)
  {
    Serial.print(content[i]);
  }
  Serial.println();
}
/*----------------------------------------------------------------------------------------------------------------*/

