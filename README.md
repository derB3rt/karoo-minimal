# KAROO / MINIMAL

Ein minimalistisches **3-in-1-Datenfeld** für die originale Navigationskarte des **Hammerhead Karoo (3. Generation)**.

![KAROO MINIMAL Designvorschau](docs/preview.svg)

> **Status: v0.1.0 – Android-Quellcode und Design-Prototyp.** Noch nicht auf einem Karoo kompiliert oder praktisch getestet. Die Karte im Bild ist nur eine Illustration; die Erweiterung erstellt **keine eigene Karte**.

## Konzept

Die native Karoo-Kartenseite übernimmt Navigation, Karten, Routen und Abbiegehinweise. Diese Extension liefert **ein** grafisches Datenfeld am unteren Kartenrand:

| Wert | Anzeige | Karoo-Quelle |
| --- | --- | --- |
| Geschwindigkeit | 27,8 km/h (türkis, am größten) | `SPEED` |
| Kadenz | 86 rpm (weiß) | `CADENCE` |
| Herzfrequenz | 143 bpm (korallrot) | `HEART_RATE` |

Es gibt keine eigene Karten-App, keinen direkten Bluetooth-Zugriff und keinen Netzwerkverkehr. Sensoren werden wie gewohnt mit dem Karoo verbunden. Nicht verfügbare Sensorwerte werden als „—“ angezeigt.

## Technischer Aufbau

- **Kotlin / Android**, Hammerheads offizielles `karoo-ext`-SDK
- Karoo-`KarooExtension` mit `DataTypeImpl` als grafischem Feld
- Native `RemoteViews`-Anzeige mit drei Kennzahlen
- Live-Werte über Karoo-`OnStreamState.StartStreaming`
- Reine Layoutvorschau in der App (`MainActivity`)
- Statischer [Designentwurf](docs/preview.html) als SVG

Die Breite und Höhe eines grafischen Datenfelds legt die Karoo-Oberfläche fest. Ob die Leiste in der tatsächlichen Kartenansicht gut lesbar ist, müssen wir auf dem Gerät prüfen.

## Build und Installation

Voraussetzungen: Android Studio, JDK 17, Android SDK 35, Gradle 8.9.

1. Projekt in Android Studio öffnen.
2. Für das öffentliche Karoo-Paket auf GitHub Packages sind Zugangsdaten erforderlich: `GPR_USER` und `GPR_TOKEN` als **lokale** Umgebungsvariablen (GitHub-Token mit `read:packages`) oder die Properties `gpr.user` und `gpr.key` in `~/.gradle/gradle.properties`. Keine Zugangsdaten ins Repository schreiben.
3. Wenn `gradlew` fehlt, lokal Gradle 8.9 nutzen oder mit `gradle wrapper --gradle-version 8.9` den Wrapper erzeugen.
4. Gradle Sync und `app:assembleDebug` ausführen. Erwarteter Ausgabeort: `app/build/outputs/apk/debug/app-debug.apk`.
5. Debug-APK über einen für die aktuelle Karoo-OS-Version unterstützten Sideloading-Weg installieren.
6. Im Karoo-Ride-Profil die **Kartenseite** bearbeiten und das grafische Datenfeld **MINIMAL · Speed / Cadence / HR** in einen Datenfeldplatz unten auf der Karte aufnehmen.

**Die APK ist noch nicht gebaut oder auf dem Gerät getestet.** Insbesondere die tatsächliche Feldhöhe, der Kontrast sowie die Sensoranzeige müssen in einem Hardwaretest geprüft werden.

## Struktur

- `app/src/main/java/de/derb3rt/karoominimal/MinimalExtension.kt` – Datenströme und Karoo-Extension
- `app/src/main/res/layout/field_dashboard.xml` – die tatsächliche Datenleiste
- `app/src/main/res/xml/extension_info.xml` – Registrierung als grafisches Feld
- `docs/preview.svg` und `docs/preview.html` – Design-Mock-up, **keine echte Kartenanzeige**

## Inspiration

- [Offizielles Hammerhead SDK](https://github.com/hammerheadnav/karoo-ext)
- [Vin-HkE](https://github.com/maduwatas/Vin-HkE) – mehrere Kennzahlen in einem Feld
- [BigFields](https://github.com/anpaiss/BigFields) – große, gut lesbare Datenfelder
- [Karoo-Profile konfigurieren](https://support.hammerhead.io/hc/en-us/articles/25601814671259-Karoo-OS-Customising-Profiles)

## Lizenz

MIT. Fremde Karten oder Quelltexte werden nicht übernommen.
