/* Programme de test unitaire permettant de tester la fonction
 *  d'obtention de 3 chiffres distincts à partir d'un nombre composé de 3 chiffres
 
 */

#include <SoftwareSerial.h>    // Library of software Serial communication

void setup()  {
  
  Serial.begin(9600);              //on démarre la liaison série afin de voir les résultats
  int test[3];                     // le tableau dans lequel on stocke le résultat de la fonction
  infotypetest(000, &test[0]);     // appel de la fonction avec en arguments la valeur testée et le tableau
  if(test[0] == 0 && test[1] == 0 && test[2] == 0 ){    //si les résultats correspondent aux valeurs
    Serial.print("test 000 ok\n\r");                    //attendues le test est bon
  }
  else{                                                 //sinon ce n'est pas le cas
    Serial.print("test 000 pas ok\n\r");
  }
  
  infotypetest(111, &test[0]);                          //on fait des tests avec plusieurs valeurs
  if(test[0] == 1 && test[1] == 1 && test[2] == 1 ){
    Serial.print("test 111 ok\n\r");
  }
  else{
    Serial.print("test 111 pas ok\n\r");
  }
  
  infotypetest(259, &test[0]);
  if(test[0] == 2 && test[1] == 5 && test[2] == 9 ){
    Serial.print("test 259 ok\n\r");
  }
  else{
    Serial.print("test 259 ok\n\r");
  }

  infotypetest(2593, &test[0]);                       //test d'une valeur non souhaitable
  if(test[0] == 2 && test[1] == 5 && test[2] == 9 ){  //on vérifie que les valeurs du tableau 
    Serial.print("test 2593 ok\n\r");                 //n'ont pas changées
  }
  else{
    Serial.print("test 2593 pas ok\n\r");
  }
}


void loop() {
              // On n'as pas besoin de se servir de la boucle

}

void infotypetest(int typeInfo_local, int *test) {
   
    int Nb_S;  
    int NB_A;
    int Nb_R;

    if(typeInfo_local<1000){                //si le nombre comprend moins de 4 digits
      Nb_R = typeInfo_local % 10;           //on sépare le nombre en 3 chiffres
      typeInfo_local = typeInfo_local / 10;
      NB_A = typeInfo_local % 10;
      typeInfo_local = typeInfo_local / 10;
      Nb_S = typeInfo_local % 10;    

    //partie qui n'est pas dans le programme, mais nécessaire pour récupérer les résultats
      test[2]=Nb_R;                         
      test[1]=NB_A;
      test[0]=Nb_S;
      }
  }

 
