# ProjetInfo
Ce projet est une montre connectée réalisée grâce à une carte Arduino R3, un module bluetoothe HC06 et un écran OLED.
Sont présents le code de l'application Android et celui de la carte Arduino.

Dans le dossier Arduino, montre.ino est le programme principal, bluetoothTest.ino est un programme d'un test intermidiaire du module bluetooth, alors que les autres fichiers sont inclus dans la bibliothèque. 

##### Setup
1. Connection
2. Arduino envoie "TIME" à Android
3. Android respond "T1234567890"(Unix time)

##### Check
1. Android reçois un message, un mail ou il y a un rappel
2. Android envoie I100[type de message]:[le_message] (ou l'expéditeur)
3. Arduino les affiche

##### Cancel
1. L'utilisateur pousse le bouton
2. Arduino envoie "Cancel"
3. Android ignore les notification
4. Android renvoie "I000"

## Suivi 09/05
### Partie Arduino
#### Avancement
- Affichage du nombre des 3 différents types de notification (SMS, Mail, Rappel)
- Affichage du contenu de message(20 charactères au maximum)

#### Travail à venir
- Ajouter le son (buzzer) lors de la reception d'un message (notification)
- Ajouter la fonction du bouton d'effacer les notifications
- Ecrire les Unit Tests 
  - 4 parties principales: Synchronisation bluetooth, affichage d'ecran, temps, Notification processing 

### Partie Android
#### Avancement

#### Travail à venir






## Partie Arduino (passé)

### Avancement
- Temps signal pour synchroniser
- Pousser bouton pour reset
- Envoyer I1 I2 I3 pour appeler différents types de notification, et I0 pour effacer.

### Problèmes à régler/Travail à venir
1. ~~ Effacer la notification par button. Idée : Envoyer un message à l'app pour renvoyer un message indiquant Infotype=0 ~~
2. ~~Affichage des plusieurs notifications~~
3. ~~Affichage du nombre des nouveaux messages~~
4. ~~Effacer la notifitacion par type de message~~
5. ~~__Gestion de la mémoire__~~
6. Autres fonctions si possible
7. ~~Mode d'affichage. Boucle ou Réponse~~
8. __Test__


#### Affichage
1. afficher "S M R" dans une structure de condition
2. ou afficher SMR séparament
3. extrait chaque chiffre pour indiquer le nombre de nouveaux messages.
4. afficher le contenu de chaque type de notif pendant 5 second  

I000 SO MO R0 => Effacé  
I100 S1 M0 R0 => "Bonjour!"(moins de 10 charactères) => afficher  
I010 S0 M1 R0 => "From NOM" => afficher  
I001 S0 M0 R1 => "R:CONTENU" => afficher  

Tous les notifs reste sur l'écran jusqu'à la reception du nouveau message ou le poussoir du bouton  

#### Interruption
A voir

#### Test
Si on arrive à communiquer avec Arduino à partir du terminal d'un PC, on pourrait écrire un programme de bash pour tester les fonctions automatiquement
