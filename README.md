# gui4j 2.0 (Modernized)

[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![License](https://img.shields.io/badge/License-LGPL-blue.svg)](LICENSE.txt)
[![Build Status](https://img.shields.io/badge/Build-Success-brightgreen.svg)]()
[![Security Scan](https://img.shields.io/badge/CVE-None-brightgreen.svg)]()

`gui4j` ist ein Hochleistungs-Framework zur Beschreibung von **Java Swing GUIs vollständig in XML**. Durch die Trennung von UI-Definition (XML) und Anwendungslogik (Java) ermöglicht es ein sauberes MVC-Pattern für Desktop-Anwendungen.

Ursprünglich im Jahr 2002 entwickelt, wurde diese Version im Jahr 2026 **vollständig modernisiert**, um heutigen Sicherheits- und Performance-Standards gerecht zu werden.

---

## 🚀 Wichtige Modernisierungs-Features (v2.0)

* **Security First**: Vollständig gehärtete XML-Pipeline. Schutz gegen XXE-Angriffe (CVE-2020-10683) durch Deaktivierung externer Entities im `LNSAXReader`.
* **Modernes Java**: Optimiert für **Java 21 (LTS)**. Keine Abhängigkeiten mehr zu Java 1.4 Legacy-Strukturen.
* **Fluent API**: Neue `Gui4jFactoryV2` für intuitives, flüssiges Laden von Views.
* **Lambda Support**: Native Unterstützung für funktionale Interfaces in Controllern und Validatoren.
* **Hohe Performance**: Thread-Management migriert auf den modernen `ExecutorService` (ThreadPoolExecutor).
* **Beseitigung technischer Schulden**: Entfernung von Applet-Support, "Raw Types" (Generifizierung) und veraltetem Boxing.

---

## 🛠 Installation & Build

Dieses Projekt verwendet **Maven 3**.

```bash
# Projekt bauen und Sicherheits-Scans ausführen
mvn clean verify

# Javadoc generieren
mvn javadoc:javadoc
```

---

## 💡 Anwendungsbeispiel

### 1. UI in XML definieren (`view.xml`)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<window title="Modernes gui4j Beispiel">
    <panel>
        <button text="Klick mich" onAction="handleAction" />
    </panel>
</window>
```

### 2. Verbindung mit einem modernen Java-Controller
```java
public class MyController {
    /**
     * Diese Methode wird via Reflection aufgerufen, wenn der Button
     * in der XML-View geklickt wird.
     */
    public void handleAction() {
        System.out.println("Aktion via Reflection ausgelöst!");
    }
}
```

### 3. Laden via Fluent API (v2)
```java
// Die neue Fluent-API ermöglicht ein intuitives Laden und Anzeigen
Gui4j.loadView("view.xml")
     .withController(new MyController())
     .show();
```

---

## 🔍 Validierungs-Werkzeuge

Das Framework enthält nun einen **statischen XML-Validator**, um Views ohne UI-Start zu prüfen – ideal für CI/CD-Pipelines:

```bash
mvn exec:java -Dexec.mainClass="org.gui4j.Gui4jXmlValidator" -Dexec.args="pfad/zu/deiner-view.xml"
```
Dieses Tool führt folgende Prüfungen durch:
* **Schema-Konformität**: Validierung gegen die internen DTD/Schema-Regeln unter Verwendung des gehärteten LNSAXReaders.
* **Reflection-Check**: Statische Analyse, ob die in XML referenzierten Controller-Methoden (z. B. `onAction`) tatsächlich in der Java-Klasse existieren.

---

## 📄 Dokumentation
* [Migration Guide](MIGRATION_GUIDE.md) – Detaillierte Anleitung für den Umstieg von v1.x auf v2.x.
* [Modernization Agenda](MODERNIZATION_AGENDA.md) – Historie der Modernisierung mit allen KPI-Messwerten von Phase 0 bis 3.

---
**Organisation**: beck et al. projects GmbH
**Initial Inception**: 2002
**Modernisiert**: 2026 (Phase 0-3 abgeschlossen)