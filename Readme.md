# README - Flag Fury

## von Niclas van der Pütten und Markus Artemov

### Einleitung
"Flag Fury" ist ein Multiplayer-Spiel, das mit Bluetooth-Technologie funktioniert. Spieler treten in Teams gegeneinander an, um Flaggen zu erobern und zu verteidigen. Diese Anleitung beschreibt die grundlegende Bedienung des Spiels.

---

## **1. Home Fragment**
Über das Home Fragment hat der Spieler die Wahl:
- **Neues Spiel erstellen** („Create Game“)
- **Einem bestehenden Spiel beitreten** („Join Game“)

---

## **2. Create Game**
In diesem Modus kann der Host ein neues Spiel erstellen:
- **„Set Flag Position“** setzt eine Flagge an der aktuellen Position.
- Alternativ kann durch **Longpress** unabhängig von der aktuellen Position ein Punkt gesetzt werden.
- Zum Löschen eines Punktes einfach darauf tippen – eine Snackbar erscheint, in der das Löschen bestätigt werden kann.
- Nach dem Setzen der Flaggenpositionen kann das Spiel benannt und registriert werden.

---

## **3. Join Game**
- Um einem Spiel beizutreten, wird die **Spiel-ID** benötigt, die vom Host bereitgestellt wird.
- In der Lobby kann der Host Spieler über den **„Kick“-Button** entfernen.
- **Nur der Host** kann das Spiel starten.

---

## **4. Spielbeginn**
- Vor dem Start wird bei allen Spielern geprüft, ob **Bluetooth-Discovery** aktiviert ist.

---

## **5. Spielverlauf**
- Befindet sich ein Spieler **näher als 5 Meter** an einer gegnerischen Flagge, wird diese als **umkämpft** markiert.
- Nach **10 Sekunden** wird die Flagge erobert und ändert ihre Farbe.
- Wenn der **Bluetooth-Server** des feindlichen Teams erreicht wird, setzt sich die Flagge auf **neutral (grau)** zurück.

**Wichtig:**
- Das Spiel berücksichtigt keine **exakten Standorte**, daher müssen alle Mitglieder des gegnerischen Teams aus der Reichweite der Flagge sein, damit sie erobert werden kann.

---

## **6. Spielende**
- Sobald alle Punkte erobert wurden, wird ein **Gewinnerteam** bestimmt.
- Ein Abschlussscreen erscheint – über **„Schließen“** kann man zur Startansicht zurückkehren.

---

## **Hinweise & Empfehlungen**
- **Bluetooth-Discovery muss dauerhaft aktiviert bleiben.**
- Bleibt innerhalb der **Spielgrenzen**, um eine faire Partie zu gewährleisten.

Viel Spaß beim Spielen! 🎮

