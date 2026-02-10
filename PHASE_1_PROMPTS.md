# Schritt 1: Absicherung der XML-Pipeline (dom4j)
## Ziel: Update von dom4j und Erhalt der LElement-Funktionalität. Prompt für Codex:
## Prompt
"Wir starten mit Phase 1: Security Hardening.

Aufgabe: Aktualisiere dom4j in der pom.xml auf die sicherste kompatible Version (vorgeschlagen: 2.1.4 oder höher). Herausforderung: > 1. Das Projekt nutzt eine lokale Erweiterung org.dom4j.LElement und LNSAXReader, um Quelltext-Zeilennummern zu speichern. 2. Prüfe, ob die API-Änderungen in dom4j 2.x diese Erweiterungen brechen (z.B. geänderte Interfaces in Element oder SAXReader). 3. Falls Inkompatibilitäten bestehen, repariere die Klassen LElement und LNSAXReader so, dass sie mit der neuen Version funktionieren, aber die Zeilennummern-Logik erhalten bleibt. 4. Konfiguriere den LNSAXReader explizit so, dass External Entities (XXE) deaktiviert sind, um CVE-2020-10683 final zu schließen."


# Schritt 2: Logging-Modernisierung (Entfernung von High-CVEs)
## Ziel: Ablösung der veralteten commons-logging durch eine sichere Bridge und Bereinigung transitiver Abhängigkeiten.
## Prompt
**Aufgabe:** Bereinige die Logging-Infrastruktur in der `pom.xml`.
1. **Entfernung**: Lösche die direkte Abhängigkeit zu `commons-logging:1.0.2`.
2. **Bridge-Einführung**: Füge `org.slf4j:jcl-over-slf4j:1.7.36` hinzu. Dies ersetzt die Commons-Logging-API durch eine sichere SLF4J-Implementation.
3. **Backend**: Füge `org.slf4j:slf4j-simple:1.7.36` (Scope: runtime) hinzu, damit Logs während der Entwicklung und Tests ausgegeben werden.
4. **Enforcement**: Füge einen `<exclusion>` Block zu allen anderen Abhängigkeiten hinzu, die eventuell noch intern auf `commons-logging` verweisen könnten, um sicherzustellen, dass die alte JAR nicht mehr im Klassenpfad landet.
5. **Verifikation**: Führe `mvn clean compile` aus, um sicherzustellen, dass die API-Kompatibilität durch die Bridge gewahrt bleibt.


# Schritt 3: Finales Audit & KPI-Update
## Ziel: Nachweis des Sicherheits-Erfolgs (0 Criticals) und Aktualisierung der Management-Dokumentation.
## Prompt
**Aufgabe:** Führe das finale Verifikations-Audit für Phase 1 durch:

1. **Build & Test**: Führe im Terminal `mvn clean verify` aus.
   - Bestätige, dass die `Gui4jCallParserTest` trotz der neuen `dom4j`-Version (2.1.4) erfolgreich durchlaufen.
   - Falls das Mockito-Problem (ByteBuddy) den Build stoppt, füge die Property `<argLine>-Dnet.bytebuddy.experimental=true</argLine>` in der `pom.xml` unter dem `maven-surefire-plugin` hinzu, um den Testlauf zu erzwingen.

2. **Security Scan**: Analysiere den neuen OWASP-Report unter `target/dependency-check-report.html`.
   - Verifiziere, dass die Anzahl der 'Critical' Lücken für `dom4j` nun 0 ist.
   - Prüfe, ob durch die `slf4j`-Umstellung auch die 'High' Lücken verschwunden sind.

3. **Dokumentation**: 
   - Öffne `MODERNIZATION_AGENDA.md`.
   - Trage die neuen Werte (Critical: 0, High: X) in die Spalte 'Phase 1' der KPI-Tabelle ein.
   - Dokumentiere im Abschnitt 'Phase 1', dass der `LNSAXReader` erfolgreich gegen XXE gehärtet wurde und `commons-logging` durch eine sichere Bridge ersetzt wurde.

4. **Abschlussmeldung**: Gib mir die finale Anzahl der verbleibenden Sicherheitslücken und den Status der Tests zurück.