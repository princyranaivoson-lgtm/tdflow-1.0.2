# Analyse du TDFlow fourni

## Protection et vérification

- `www/index.html` est la copie de travail de `/home/ubuntu/upload/index.html`.
- `index.html.original.bak` conserve une copie bit à bit du fichier fourni avant modification.
- Le script applicatif original et la copie enrichie ont été extraits et contrôlés avec `node --check` : aucune erreur de syntaxe détectée. Le backup est identique octet pour octet au HTML fourni.
- Le PNG TDFlow reçu est conservé tel quel dans les ressources web et Android ; l’image source est RGB sans canal alpha malgré la demande de transparence.

## Fonctions déjà présentes dans le HTML

| Domaine | Ce qui existe dans le script |
| --- | --- |
| Montage | Canvas de prévisualisation, timeline média/audio/paroles, zoom timeline, déplacement et rognage temporel des lignes, sélection et suppression. |
| Audio | Un seul élément `Audio`, réglage du volume musique et audio vidéo, découpe de la musique, estimation d’export. |
| Paroles | Import LRC/texte, synchronisation par tap (dont plein écran), édition, offset global, synchronisation/recalage, positionnement, animations par ligne. |
| Médias | Import local multi-image/vidéo, miniatures, transitions, fond animé, effets de texte. |
| Export | `VideoEncoder`, `AudioEncoder`/`AudioData` si disponibles, Canvas `VideoFrame`, muxeur MP4 embarqué `Mp4Muxer`, prévisualisation et partage. Le chemin Android attend déjà les méthodes `TDAndroid.beginSave`, `chunk`, `endSave`, `shareLast`, `keepOn` et `tdBack`. |
| Sauvegarde | Projets et fichiers gérés dans IndexedDB (`tdflow`, stores `p` et `f`), avec autosauvegarde. |
| Dépendances | Le muxeur MP4 est intégré au HTML ; les polices Google sont distantes avec substitutions de système. Le HTML réfère également à `fonts/*.woff2`, absents de la livraison. |

## Points à ne pas présenter comme vérifiés

- WebCodecs n’a pas été exécuté dans un WebView Android physique. Sa présence/compatibilité varie selon version du système et WebView.
- MediaStore, permissions et lecture/export requièrent une installation et un essai sur téléphone réel.
- La copie locale de l’audio importé et son pont JS sont inclus dans le projet, mais nécessitent encore validation appareil.
- L’image/logo officiel n’a pas été reçue ; aucun logo de remplacement ne sera fourni.
- L’absence d’accès au SDK Android local empêche de déclarer qu’un APK a été compilé ici. Le workflow CI est prévu pour fournir un APK debug lorsqu’il est poussé dans un dépôt GitHub.
