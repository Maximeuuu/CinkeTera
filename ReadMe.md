# Cinke Terra


## Qu'est ce que c'est ?

Cinke Terra, ou en français, les cinq terres et un jeu qui consiste à relier 21 îles principales, diviser par 5 régions, avec des voies maritimes.
Le jeu se joue entièrement à la souris, il est donc nécessaire d'en avoir une.
Il est possible de jouer à deux, mais on peut bien sûr jouer seul de notre côté.


## Les règles
La partie se déroule en deux manches, chacune composé aléatoirement de 5 à 10 tours. À chaque début de tour, vous devez piocher une carte dans le paquet. Il existe deux types de cartes : les bords noirs, et les bords blancs, tous les deux composé de 5 couleurs (rose, gris, vert, jaune et multicouleur)
Vous commencerez sur une île de départ qui dépend de la couleur de votre feutre : pas besoin de se souvenir des noms, les îles vous seront afficher en début de partie.

Vous n'avez pas le droit de :
- Faire une boucle avec la même couleur
- Croiser des routes maritimes entre eux qu'importe leur couleur
- Crée une route maritime qui ne commence pas sur une extrémité de la couleur en jeu
- Faire une route qui ne lie pas une île de la couleur de la carte pioché

Une fois toutes les cartes noires piochées, la deuxième manche se lancera avec les mêmes règles, juste une couleur différente.
Pas de panique, les îles coloriables seront surligné de leur couleur afin de vous aider dans votre partie.


## Comment lancer le jeu
1. Veuillez vous positionner dans le fichier envoyé, puis avec la commande `javac @compile.list @option.list`, compilez les fichiers java.
2. Ensuite, déplacer vous dans le fichier class avec la commande `cd class`
3. Lancer l'application avec la commande `java cinketerra.Controleur`



## Tester les limites du jeu
Une envie de jouer le rôle de debugger ? Pas de soucis ;

1. Veuillez vous positionner dans le fichier envoyé, puis avec la commande `javac @compileTest.list @option.list`, compilez les fichiers java.
2. Ensuite, déplacer vous dans le fichier class avec la commande `cd class`
3. Lancer l'application avec la commande `java debug.ControleurTests`

Avec ce mode de jeux, vous serez en possibilité de voir et donc de choisir vos cartes. Ce mode vous permettra de tester toutes les erreurs qu'on a pu rencontre lors de la programmation. 


## Les différents scénario

Il peut s'avérer compliqué de penser à toute les combinaisons que le joueur peut faire et qui pourrait emmener à un bug. C'est pour cela que il existe, en plus de nos scénario proposé, un mode libre qui vous permettra de jouer en choissisant les cartes que vous voulez jouer.