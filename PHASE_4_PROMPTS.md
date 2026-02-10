# Schritt 1: Entfernung von JApplet & Annotation-Fixes
## Ziel: Beseitigung der Java 21 Compiler-Warnungen und endgültige Entfernung toter Applet-Logik.
## Prompt

# Schritt 2: Löschen der Legacy-Dateien
## Ziel: Bereinigung des Projekt-Verzeichnisses von veralteten Build-Artefakten.
## Prompt
**Aufgabe:** Lösche die nun überflüssigen Legacy-Dateien im Projekt-Root, da die Migration auf Maven 3 und Java 21 erfolgreich abgeschlossen ist:

1. Lösche `build.xml` (altes Ant-Skript).
2. Lösche `project.xml` und `project.properties` (Maven 1 Relikte).
3. Lösche `maven.xml` (falls vorhanden).
4. Lösche `README.txt` (ersetzt durch MODERNIZATION_AGENDA.md und MIGRATION_GUIDE.md).