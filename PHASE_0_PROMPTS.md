# Schritt 1: Infrastruktur-Migration (Maven)
## Ziel: Ablösung von Ant/Maven 1 durch ein modernes Maven 3 Build-System.
## Prompt
Handle als Senior Java Engineer. Wir modernisieren das Projekt `gui4j`.

**Aufgabe:** Erstelle im Projekt-Root eine moderne `pom.xml` für Maven 3. 
- Nutze die Metadaten aus der vorhandenen `project.xml` für `gui4j` Version `1.2.3`.
- Setze `maven.compiler.source` und `target` auf `1.4` für Bytecode-Kompatibilität, verwende aber Java 17 als Build-Laufzeit.
- Füge Abhängigkeiten für `dom4j:1.6.1` und `commons-logging:1.0.2` hinzu.
- Konfiguriere das `dependency-check-maven` Plugin (OWASP) für Sicherheits-Scans und das `jacoco-maven-plugin` für Code-Coverage.
- Richte das Standard-Maven-Layout ein (`src/main/java`).


# Schritt 2: Source-Hygiene, Layout-Finalisierung & Compiler-Update
## Ziel: Sicherstellung der korrekten Projektstruktur, Behebung von Dateidefekten und Vorbereitung auf moderne Java-Compiler.
## Prompt

**Aufgabe:** Führe folgende Operationen aus, um die Phase 0 abzuschließen:

1. **Infrastruktur-Update**: Aktualisiere in der `pom.xml` die Properties `maven.compiler.source` und `maven.compiler.target` von `1.4` auf `1.8`. Dies ermöglicht das Kompilieren unter Java 17 ohne Toolchain-Zwang, während die Code-Basis stabil bleibt.
2. **Layout-Check**: Stelle sicher, dass wirklich ALLE Java-Dateien von `src/org/...` nach `src/main/java/org/...` verschoben wurden. Lösche anschließend alle verbleibenden leeren Verzeichnisse unter dem alten `src/`-Pfad.
3. **Datei-Reparatur**: Behandle die Datei `src/main/java/org/gui4j/core/swing/calendar/CalendarBean.java` spezialisiert: 
   - Konvertiere die Legacy-CR-Zeilenenden zu LF.
   - Repariere Encoding-Fehler (kaputte Umlaute) in den Kommentaren.
4. **Code-Style**: Formatiere den gesamten Quellcode im Projekt nach modernen Java-Standards (z.B. Google Java Style), aber stelle sicher, dass alle `assert`-Statements als wichtige Invarianten erhalten bleiben.
5. **Ressourcen**: Verifiziere, dass alle `.properties` und XML-Konfigurationsdateien (falls vorhanden) im Verzeichnis `src/main/resources` liegen.


# Schritt 3: Baseline-Tests (Golden Master)
## Ziel: Fixierung des Ist-Verhaltens der kritischen Reflection-Logik, bevor wir Sicherheits-Updates durchführen.
## Prompt

**Aufgabe:** Erstelle die Testklasse `src/test/java/org/gui4j/core/impl/Gui4jCallParserTest.java`.
- Schreibe JUnit 5 Tests für den `Gui4jCallParser`.
- **Hintergrund:** Diese Klasse zerlegt Strings aus dem XML in Methodenaufrufe für Controller. Dies ist eine Hochrisiko-Zone für Sicherheitslücken und Regressionsfehler.
- **Anforderung:** - Teste die korrekte Auflösung von einfachen Zugriffspfaden (z.B. "doSave") und komplexeren Pfaden.
  - Nutze Mockito, um Controller-Objekte zu simulieren.
  - Ziel ist es, das Verhalten der Version 1.2.3 zu dokumentieren, damit spätere Sicherheits-Fixes an der Reflection-Logik die Funktionalität nicht brechen.


# Schritt 4: Audit & KPI-Dokumentation
## Ziel: Offizielle Erfassung aller Sicherheitslücken und Abschluss der Vorbereitungsphase.
## Prompt

**Aufgabe:** Führe das finale Sicherheits-Audit für Phase 0 aus:

1. **Build & Scan**: Führe im Terminal `mvn clean verify` aus. 
   - Dies kompiliert das Projekt, lässt die neuen `Gui4jCallParserTest` Tests laufen und startet den OWASP Dependency-Check.
2. **Berichtsanalyse**: 
   - Öffne den Report unter `target/dependency-check-report.html`.
   - Suche gezielt nach den Abhängigkeiten `dom4j:1.6.1` und `commons-logging:1.0.2`.
3. **Datenextraktion**: 
   - Wie viele 'Critical' und 'High' Sicherheitslücken werden insgesamt für das Projekt gemeldet?
   - Welche CVE-Nummer hat die höchste Priorität bei `dom4j`?
4. **Dokumenten-Update**: 
   - Aktualisiere die `MODERNIZATION_AGENDA.md`:
     - Trage unter '3. Management-KPIs' die Anzahl der gefundenen Lücken in die Spalte 'Status Quo (Phase 0)' ein.
     - Ergänze im Abschnitt '1. Projektsnapshot', dass eine Testabdeckung für die Kern-Reflection-Logik etabliert wurde.
5. **Abschlussmeldung**: Gib mir die Anzahl der Lücken und den Status des Build-Laufs zurück.