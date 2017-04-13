# ProjetInfo
Ce projet est une montre connectée réalisée grâce à une carte Arduino R3, un module bluetoothe HC06 et un écran OLED.
Sont présents le code de l'application Android et celui de la carte Arduino.

Dans le dossier Arduino, montre.ino est le programme principal, bluetoothTest.ino est un programme d'un test intermidiaire du module bluetooth, alors que les autres fichiers sont inclus dans la bibliothèque. 

## Partie Arduino

### Avancement
- Temps signal pour synchroniser
- Pousser bouton pour reset
- Envoyer I1 I2 I3 pour appeler différents types de notification, et I0 pour effacer.

### Problèmes à régler/Travail à venir
1. Effacer la notification par button. Idée : Envoyer un message à l'app pour renvoyer un message indiquant Infotype=0
2. Affichage des plusieurs notifications
3. Affichage du nombre des nouveaux messages
4. Effacer la notifitacion par type de message
5. __Gestion de la mémoire__
6. Autres fonctions si possible
7. Mode d'affichage. Boucle ou Réponse
8. __Test__



#### Affichage
1. afficher "S M R" dans une structure de condition
- ou afficher SMR séparament
- extrait chaque chiffre pour indiquer le nombre de nouveaux messages.
- afficher le contenu de chaque type de notif pendant 5 second
