# Localisation — Android GPS + Google Maps + PHP/MySQL

## Structure
```
Localisation/
├── app/src/main/
│   ├── java/com/example/localisation/
│   │   ├── MainActivity.java    → GPS + envoi Volley POST
│   │   └── MapsActivity.java    → Google Maps + markers
│   ├── res/layout/
│   │   ├── activity_main.xml
│   │   └── activity_maps.xml
│   └── AndroidManifest.xml
└── backend/                     → Copier dans htdocs/localisation/
    ├── schema.sql
    ├── classe/Position.php
    ├── connexion/Connexion.php
    ├── dao/IDao.php
    ├── service/PositionService.php
    ├── createPosition.php
    └── showPositions.php
```

## ⚠️ Configuration requise

### 1. Clé Google Maps
Dans AndroidManifest.xml, remplacer :
  android:value="VOTRE_CLE_GOOGLE_MAPS_ICI"
par votre clé obtenue sur https://console.cloud.google.com

### 2. IP du serveur
Dans MainActivity.java et MapsActivity.java, remplacer :
  192.168.1.1  →  IP réelle du PC sur le Wi-Fi

Pour l'émulateur Android : utiliser 10.0.2.2

### 3. Backend
- Copier le dossier backend/ dans htdocs/localisation/
- Importer schema.sql dans phpMyAdmin
- Démarrer Apache + MySQL (XAMPP/WAMP)
