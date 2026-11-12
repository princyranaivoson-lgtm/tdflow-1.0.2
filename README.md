# TDFlow Android — projet Capacitor

Ce dépôt encapsule le HTML d’édition fourni dans une application Android Capacitor, conserve le moteur HTML existant et ajoute un pont natif de base pour la bibliothèque locale MediaStore et la sauvegarde/partage des MP4.

## Contenu

- `www/index.html` : copie de travail du HTML original.
- `index.html.original.bak` : sauvegarde du HTML reçu avant modification.
- `android/` : projet natif généré avec Capacitor 8.
- `android/app/src/main/java/com/tdflow/app/TDAndroidBridge.kt` : pont Android/JavaScript MediaStore et export.
- `.github/workflows/build-android.yml` : compilation automatique APK debug.
- `docs/ANALYSE.md` : inventaire des fonctions existantes et limites vérifiées.

## Ce qu’il faut encore fournir

Le logo fourni est conservé pixel pour pixel sous `tdflow-logo-original.png`, `www/assets/tdflow-logo.png` et `android/app/src/main/res/drawable-nodpi/tdflow_logo.png`. Il sert d’icône d’application et est imposé dans l’éditeur de pochettes : forme (carrée, arrondie, circulaire, verticale, horizontale), dégradé/couleur/image de fond, réglages de taille/position, export PNG aplati, stockage IndexedDB et association optionnelle avec le morceau sélectionné. Le PNG reçu est RGB (sans canal alpha) ; son noir d’arrière-plan reste opaque, car l’asset original n’a pas été modifié.

## Obtenir l’APK sans installer Android Studio sur Windows

1. Créez un dépôt GitHub privé et téléversez le contenu de cette archive à sa racine.
2. Dans GitHub, ouvrez **Actions** et activez les workflows si demandé.
3. Lancez **Build TDFlow Android APK** avec **Run workflow**, ou poussez sur `main`.
4. À la fin du job, téléchargez l’artefact `TDFlow-debug-apk` ; il contient `app-debug.apk`.
5. Transférez l’APK sur le téléphone et autorisez l’installation pour la source utilisée.

Aucune clé de signature release n’est inventée. Cet automatisme compile uniquement un APK debug, signé par la clé de débogage générée par Android Gradle.

## Construction locale (facultative)

Avec Node.js 22, Java 21 et Android SDK configuré :

```bash
npm ci
npx cap sync android
cd android
./gradlew assembleDebug
```

Sous Windows, utilisez `gradlew.bat assembleDebug`.

## Importation audio Android

L’application demande l’accès aux fichiers audio du téléphone via les permissions système ciblées selon la version d’Android. Le pont copie le morceau sélectionné vers l’espace privé de l’application avant lecture afin de ne pas modifier le fichier original. La bibliothèque ne peut afficher que les médias rendus accessibles par Android et l’autorisation accordée.

## État des essais

Le HTML source enrichi passe `node --check`, le manifeste XML est valide, Capacitor synchronise ses assets et `npm audit` ne trouve aucune vulnérabilité. La tentative `./gradlew assembleDebug` a échoué avant les tâches de compilation, car le Java présent n’inclut pas le compilateur (`JAVA_COMPILER` manquant). Aucun APK n’est donc livré ni déclaré compilé. Aucun téléphone/émulateur ni exécution GitHub Actions n’a été utilisé. WebCodecs reste soumis à la compatibilité réelle de la WebView du téléphone.
