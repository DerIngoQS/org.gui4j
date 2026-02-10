# Schritt 1: Einführung von Generics (Typsicherheit)
## Ziel: Beseitigung der „Raw Types“ Warnungen in den Kern-Klassen. Prompt für Codex:
## Prompt
Wir starten mit Phase 2: Internal Modernization.

Aufgabe: Führe Generics in den Kern-Paketen org.gui4j und org.gui4j.core ein. Hintergrund: Der Code nutzt aktuell fast ausschließlich 'raw' Collections (z.B. List statt List<String>). Anforderung:

Analysiere die Klassen im Paket org.gui4j.core.impl und generifiziere die internen Listen, Maps und Caches.

Nutze die Typsicherheit von dom4j 2.x, um die XML-Knoten-Listen korrekt zu typisieren (z.B. List<Element>).

Achte darauf, dass die öffentliche API in org.gui4j quelltextkompatibel bleibt, aber füge dort, wo es sinnvoll ist, Generics hinzu (z.B. bei Controllern oder Validatoren).

Verifiziere das Ergebnis mit mvn clean compile.


