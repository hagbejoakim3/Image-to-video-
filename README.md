# 🎬 PhotoParle - Application Android

[![Build & Release Debug APK](https://github.com/hagbejoakim3/Image-to-video-/actions/workflows/build-apk.yml/badge.svg)](https://github.com/hagbejoakim3/Image-to-video-/actions/workflows/build-apk.yml)
[![GitHub Release](https://img.shields.io/github/v/release/hagbejoakim3/Image-to-video-?color=blue&label=Derni%C3%A8re%20Version)](https://github.com/hagbejoakim3/Image-to-video-/releases)

> Transformez vos images et photos en vidéos parlantes animées avec synchronisation labiale et synthèse vocale.
> 
> **Dépôt GitHub** : [hagbejoakim3/Image-to-video-](https://github.com/hagbejoakim3/Image-to-video-)

---

## 📥 Téléchargement Direct de l'APK

👉 **[Cliquez ici pour télécharger la dernière version de l'APK (PhotoParle-debug.apk)](https://github.com/hagbejoakim3/Image-to-video-/releases/latest/download/PhotoParle-debug.apk)**

Deux méthodes simples permettent de télécharger l'APK une fois le projet publié sur GitHub :

### 1. Via les GitHub Releases (Recommandé)
- Rendez-vous dans l'onglet **Releases** de votre dépôt GitHub.
- Dans la dernière version publiée, téléchargez le fichier **`PhotoParle-debug.apk`** sous la section **Assets**.
- Transférez ou téléchargez-le directement sur votre téléphone Android et installez-le.

### 2. Via les GitHub Actions Artifacts
- Rendez-vous dans l'onglet **Actions** de votre dépôt GitHub.
- Cliquez sur la dernière exécution du workflow **"Build & Release Debug APK"**.
- En bas de la page dans la section **Artifacts**, cliquez sur **`PhotoParle-debug-apk`** pour le télécharger.

---

## 🚀 Fonctionnement du Workflow GitHub Actions

Le workflow automatique est situé dans [`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml).

### Déclencheurs automatiques :
- **À chaque `push`** sur la branche `main` ou `master` : compilation et publication automatique d'une Release avec l'APK.
- **À chaque création de tag** (ex. `git tag v1.0.0 && git push origin v1.0.0`) : compilation et publication d'une Release officielle.
- **Lors d'une Pull Request** : compilation de vérification et génération de l'artefact APK.
- **Déclenchement Manuel (Workflow Dispatch)** : 
  1. Allez dans l'onglet **Actions** sur GitHub.
  2. Sélectionnez le workflow **Build & Release Debug APK**.
  3. Cliquez sur **Run workflow** (avec possibilité de spécifier un tag de version).

### Étapes exécutées par le workflow :
1. Clônage du code source (`actions/checkout@v4`).
2. Configuration de l'environnement Java JDK 21 (`actions/setup-java@v4`).
3. Configuration et mise en cache de Gradle (`gradle/actions/setup-gradle@v4`).
4. Préparation automatique de l'environnement (`.env` et `debug.keystore`).
5. Compilation du fichier APK Android Debug via `./gradlew assembleDebug`.
6. Enregistrement de l'artefact téléchargeable (`actions/upload-artifact@v4`).
7. Création automatique de la Release GitHub avec l'APK joint (`softprops/action-gh-release@v2`).

---

## 📱 Fonctionnalités de l'Application

- **Import de photo** : Choisissez une photo de votre galerie ou sélectionnez un des avatars prédéfinis.
- **Texte en parole** : Écrivez le texte que l'avatar doit prononcer ou utilisez l'assistant de script IA.
- **Animation faciale en temps réel** : Articulation labiale synchronisée, mouvements de tête, respiration et clignements des yeux.
- **Sous-titres karaoké** : Mise en valeur mot à mot des paroles prononcées.
- **Styles visuels** : Studio Pro, Cyberpunk néon, Vintage Film, Hologramme, Cinéma 2.39:1.
- **Historique et lecteur vidéo** : Sauvegarde des vidéos créées dans la base de données locale avec piste audio.
