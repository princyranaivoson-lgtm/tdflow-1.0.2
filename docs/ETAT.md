# État d’implémentation et points ouverts

## Livré dans cette base

- HTML source préservé dans `index.html.original.bak` et copie exécutable identique à l’original sous `www/index.html` avant raccordement natif.
- Projet Capacitor Android généré, identifiant `com.tdflow.app`, nom `TDFlow`.
- Pont Kotlin `TDAndroidBridge` avec permissions et requête MediaStore audio, import privé en morceaux, sauvegarde MP4 dans Movies/TDFlow, partage Android et écran maintenu éveillé.
- Panneau Audio enrichi : actualisation, liste de fichiers retournés par MediaStore, recherche locale, tri par titre/artiste/album/durée, favoris conservés sur l’appareil, chargement dans le moteur Audio TDFlow déjà présent.
- Workflow GitHub Actions pour l’APK debug.

## À faire pour la conformité intégrale du cahier des charges

- L’éditeur de pochettes fonctionne avec formes carrée/arrondie/circulaire/verticale/horizontale, fond uni/dégradé ou image importée ; le PNG du logo officiel est dessiné au-dessus et aplati dans la pochette, dont le fichier final est sauvegardé dans un magasin IndexedDB ajouté par migration non destructive. Les pochettes associées s’affichent dans la liste Audio après actualisation.
- Compléter la bibliothèque : pochettes d’albums, vues dédiées Artistes/Albums/Dossiers, navigation précédent/suivant et lecteur de préécoute distinct (le bouton Utiliser charge déjà le morceau dans le lecteur unique de l’éditeur).
- Ajouter les politiques de suppression/nettoyage des copies audio privées et tester les autorisations refusées/révoquées sur appareils réels.
- Vérifier les polices embarquées et remplacer les références de fichiers locaux manquants ; Google Fonts demeure facultatif avec des polices de secours.
- Lancer et vérifier `assembleDebug` par GitHub Actions dans un dépôt réel ; aucun compte/dépôt GitHub n’a été relié à cette session. La tentative locale a échoué avant la compilation : le runtime Java présent ne fournit pas `JAVA_COMPILER` (JDK complet absent).
- Tester installation, MediaStore, AudioContext, IndexedDB, export WebCodecs et partage sur téléphone Android. Le code ne prétend pas réussir si WebCodecs manque.
- Le PNG source est en mode RGB sans canal alpha ; aucune retouche n’a été faite. Les zones noires de l’asset demeurent donc opaques.

Aucun APK n’est inclus dans l’archive, aucun test sur téléphone n’a été exécuté et aucun résultat GitHub Actions n’est disponible dans cette session.
