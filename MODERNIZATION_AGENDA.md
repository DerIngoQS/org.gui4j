# Strategische Modernisierung: Projekt gui4j

## 1. Projektsnapshot & Zielsetzung
* **Status Quo**: Java 1.4 Framework für XML-basierte Swing-UIs (Version 1.2.3).
* **Kerntechnologie**: XML-Binding via Reflection an Controller-Objekte.
* **Build-System**: Veraltetes Maven 1 / Ant Setup mit Java 1.4 Target.
* **Testabdeckung (neu in Phase 0)**: Charakterisierungstests für die Kern-Reflection-Logik (`Gui4jCallParser`) wurden etabliert und laufen im Maven-Testzyklus.
* **Primärziel**: Schließung von Sicherheitslücken (SCA) und Herstellung der Lauffähigkeit auf modernen Java-LTS-Versionen (17/21).

## 2. Phasenplan & Meilensteine

### Phase 0: Baseline & Safety Net (Fundament)
* **Build-Migration**: Umstellung auf Maven 3.
* **Code-Hygiene**: 
    * Normalisierung der Zeilenenden (besonders `CalendarBean.java`).
    * Behebung von Encoding-Fehlern in Kommentaren.
* **Charakterisierungstests**: Erstellung von Tests für:
    * XML-Parsing inkl. Styles und Includes.
    * Reflection-Pfad-Auflösung im `Gui4jCallParser`.
* **Nullmessung (KPIs)**: Initialer Scan auf Sicherheitslücken und Testabdeckung.

### Phase 1: Security & Compatibility Hardening
* **Dependency Updates**: 
    * Absicherung von `dom4j` (Schutz vor XXE-Angriffen).
    * Ablösung/Brücke für `commons-logging`.
* **Ergebnis (abgeschlossen)**:
    * `dom4j` auf `2.1.4` aktualisiert, lokale Erweiterungen (`LElement`, `LNSAXReader`) kompatibel gehalten.
    * `LNSAXReader` explizit gegen XXE gehärtet (DOCTYPE deaktiviert, externe Entities/DTD-Loading deaktiviert, blockierender `EntityResolver`).
    * `commons-logging` entfernt und durch `jcl-over-slf4j` + `slf4j-simple` ersetzt.
* **Java Modernisierung**:
    * Ersatz von `Class.newInstance()` durch Constructor-Reflection.
    * Vorbereitung auf starke Kapselung (Java 9+): Audit von `setAccessible(true)`.
* **Serialisierung**: Ergänzung von `serialVersionUID` für persistente Klassen.

### Phase 2: Internal Modernization (Behavior-Preserving)
* **XML-Pipeline**: Erhalt der spezialisierten `org.dom4j.LElement` und `LNSAXReader` Logik, um Quelltext-Zeilennummern für die Fehlerdiagnose beizubehalten.
* **Typsicherheit**: Progressive Einführung von Generics in Core- und Parser-Pfaden.
* **Concurrency**: Refactoring des `Gui4jThreadManager` von manuellen `wait/notify` Zyklen auf `java.util.concurrent`.

### Phase 3: API & Ecosystem
* **Abwärtskompatibilität**: Erhalt der legacy API (z.B. `show()`, `hide()`) via Fassaden.
* **V2 API**: Optionale Einführung eines modernisierten API-Layers mit klaren Typen.

## 3. Management-KPIs
| Kennzahl | Tooling | Status Quo (Phase 0) | Phase 1 | Ziel |
| :--- | :--- | :--- | :--- | :--- |
| **Sicherheitsrisiko** | OWASP Dependency-Check | Critical: 1, High: 1 (`dom4j:1.6.1`: CVE-2020-10683, CVE-2018-1000632) | Critical: 0, High: 0 (`mvn clean verify`, Report vom 10.02.2026) | 0 kritische Lücken (CVEs) |
| **Code-Qualität** | SonarQube | n/a | n/a | Reduzierung der technischen Schuld (Debt) |
| **Testabdeckung** | JaCoCo | Parser-Regressionstest für Reflection-Kernpfad aktiv (`Gui4jCallParserTest`) | `Gui4jCallParserTest` grün mit `dom4j:2.1.4` (4/4) | > 40% für Core-Komponenten |
| **Build-Stabilität** | GitHub Actions / CI | `mvn clean verify` lokal erfolgreich (inkl. Tests + OWASP-Report) | `mvn clean verify` lokal erfolgreich (10.02.2026) | Automatisierter Build auf Java 17/21 |

## 4. Kritische Erfolgsfaktoren (Input benötigt)
* Bereitstellung von Kunden-Konfigurationen (`gui4jComponent.properties`).
* Test-XML-Views für Regressionsprüfungen.
