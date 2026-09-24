# Aufgaben und Status

Statuswerte: offen, in Arbeit, blockiert, erledigt. Erledigt nur mit dokumentiertem Prüfergebnis. Alle Pfade unten sind relativ zum geplanten Projektroot; Code wird schrittweise ergänzt.

## P00: Architektur prüfen

Status: erledigt — Architektur und Umsetzung von P00–P02 am 24.09.2026 bestätigt.

- [x] Anforderungen, Architektur, Verträge, Leitplanken und Qualitätsstrategie dokumentieren.
- [x] Arbeitsplan und Protokoll anlegen; Dokumentationslinks und Konsistenz prüfen.
- [x] Architekturentwurf prüfen und Entscheidungen als angenommen oder geändert kennzeichnen.

Abnahme: Scope, Stack und Grenzen sind geklärt. Prüfung: Dokumentreview anhand R01–R11 und A01–A09. Abhängigkeit: keine. Umfang: Dokumentationspaket; keine Codeänderung.

## P01: Projektanlage

Status: erledigt. Abhängigkeit: P00. Nachweis: LOG-006 bis LOG-009.

### P01a: Laufzeit und Generator

- [x] Java 17, Maven-/Wrapper-Voraussetzungen und verfügbare Boot-3-Version prüfen.
- [x] Initializr-Projekt mit Java, Maven und JAR erzeugen; Generator und Parameter protokollieren.
- [x] Dependencies, Wrapper und Versionen gezielt prüfen; vorhandene Dokumentation erhalten.

Prüfung: effektive Maven-Konfiguration und Wrapper-Version prüfen; erstes Paket bauen. Bereiche: `pom.xml`, `.mvn/wrapper/`, `mvnw`, `mvnw.cmd`, Anwendungseinstieg. Umfang: mittel.

### P01b: Repository-Grundregeln

- [x] `.gitignore` und grundlegende Konfiguration ergänzen.
- [x] Konkrete Voraussetzungen und tatsächlich erfolgreiche Build-Befehle in README festhalten.

Prüfung: Dateiliste und Diff kontrollieren; JAR-Ausgabe nachweisen. Bereiche: `.gitignore`, `application.yaml`, README. Umfang: klein.

## P02: PostgreSQL-Grundlage

Status: erledigt. Abhängigkeit: P01. Nachweis: LOG-006 bis LOG-009.

### P02a: Schema und Modell

- [x] Task und Priority entsprechend CONTRACTS implementieren.
- [x] Flyway-Migration mit Constraints und Hibernate-Validierung einrichten.
- [x] Lokale PostgreSQL-Instanz mit festgeschriebenem Image und persistentem Volume vorsehen.

Prüfung: Migration gegen leere PostgreSQL-Datenbank und Schema-Validierung. Bereiche: Task, Priority, Migration, `compose.yaml`, `application.yaml`. Umfang: mittel.

### P02b: Persistenztest

- [x] Repository, PostgreSQL-Testcontainers und Failsafe einrichten.
- [x] Mapping einschließlich Enum und Zeitpräzision sowie Sortierung testen.

Prüfung: Verify-Lauf führt den Persistenz-Integrationstest tatsächlich aus. Bereiche: Repository, Testkonfiguration, Persistenztest, `pom.xml`. Umfang: mittel.

## K1: Technische Grundlage

- [x] JAR wird gebaut; Datenbankstart, Migration und Persistenztest funktionieren.
- [x] Verwendete Versionen und Prüfergebnisse stehen im Arbeitsprotokoll.

## P03: Erstellen und Anzeigen

Status: erledigt. Abhängigkeit: K1. Nachweis: LOG-012 bis LOG-014.

### P03a: Anwendungslogik

- [x] createTask und listTasks mit injizierter Clock und Transaktionen umsetzen.
- [x] Namen und Priorität validieren; Ergebnisdaten ohne offene Persistence-Session bereitstellen.

Prüfung: Service-/Domain-Tests für gültige Werte, Grenzen und Ausgangszustand. Bereiche: Service, Ergebnisdaten, Clock-Konfiguration, Domain, Tests. Umfang: mittel.

### P03b: Webablauf

- [x] Liste, Leerzustand und Anlegeformular mit Feldfehlern implementieren.
- [x] CSRF-Schutz, Redirect und Eingabe-Escaping integrieren.
- [x] Browser-Anlegen bis PostgreSQL nachweisen.

Prüfung: MockMvc mit Security plus Browserdurchlauf. Bereiche: Controller, Formular, Template, Security-Konfiguration, Webtests. Umfang: mittel.

## P04: Ändern

Status: erledigt. Abhängigkeit: P03. Nachweis: LOG-015 bis LOG-017.

### P04a: Namen ändern

- [x] Umbenennungsformular und Service-Operation ergänzen.
- [x] Fehler und unveränderte übrige Felder prüfen.

Prüfung: gezielte Service-/Webtests plus manueller Browserablauf. Bereiche: Service, Controller/Formular, Edit-Template, Tests. Umfang: mittel.

### P04b: Erledigt-Status setzen

- [x] done explizit setzen und wieder öffnen können.
- [x] Ungültige Eingaben und unbekannte IDs entsprechend Vertrag behandeln.

Prüfung: wiederholtes Setzen desselben Wertes bleibt stabil; übrige Felder bleiben erhalten. Bereiche: Service, Controller, Listen-Template, Tests. Umfang: mittel.

## K2: Kernabläufe und Grenzen

- [x] Anlegen, Anzeigen, Umbenennen und Statuswechsel funktionieren durchgängig.
- [x] Architekturtest für verbotene Paketabhängigkeiten ergänzt und bestanden.
- [x] Verify-Lauf und Browserprüfung protokolliert.

## P05: Löschen

Status: erledigt. Abhängigkeit: K2. Nachweis: LOG-018.

- [x] Löschoperation und eindeutig benannte Formularaktion umsetzen.
- [x] Gelöschter Task bleibt verschwunden; unbekannte ID und erneutes Löschen ergeben 404.
- [x] Mutationen ohne CSRF-Token werden verhindert.

Prüfung: Service-/Web-/Persistenztests für Löschverhalten und Browserdurchlauf. Bereiche: Service, Controller, Listen-Template, relevante Tests. Umfang: mittel.

## P06: Gesamtqualität und Startanleitung

Status: erledigt. Abhängigkeit: P05. Nachweis: LOG-019 bis LOG-021.

### P06a: Oberfläche und Fehler

- [x] Fehlerseiten, Labels, Fokus, Textumbruch und verständliche Zustände prüfen und korrigieren.
- [x] 375 px und 1280 px sowie Tastaturbedienung tatsächlich prüfen.

Prüfung: Browserabnahme mit langen Namen, Leerzustand und Eingabefehlern. Bereiche: Templates, CSS, Fehlerbehandlung, Webtests. Umfang: mittel.

### P06b: Reproduzierbare Abnahme

- [x] Verify-Lauf einschließlich statischer Fehleranalyse ohne ausgelassene Pflichtprüfungen durchführen.
- [x] Automatisierte Browser-/axe-Prüfung, aktuelle Abhängigkeitsprüfung und repräsentatives Laufzeitverhalten dokumentieren.
- [x] CI für Java 17, Verify und Browserprüfungen mit minimalen Berechtigungen vorbereiten.
- [x] Aus frischer Quellkopie JAR starten und Neustart-/Datenpersistenz prüfen; echter Git-Checkout folgt P07.
- [x] README anhand dieser Ausführung vervollständigen; Einschränkungen festhalten.

Prüfung: Abnahmeszenario aus QUALITY vollständig dokumentieren. Bereiche: README, WORK_LOG, bei Fehlern eng begrenzte Korrekturen. Umfang: mittel.

## K3: Abgabereife

- [x] R01–R10 und Definition of Done geprüft.
- [x] Keine unbekannten roten Tests oder unbelegten Erfolgsmeldungen.
- [x] Tatsächliche Einschränkungen sind sichtbar dokumentiert.

## P07: Abgabe

Status: erledigt. Abhängigkeit: K3. Ziel: ucarsinan/task-management, privat. Nachweis: LOG-026 und LOG-027.

- [x] Abgabediff, Repository-Dateien und Git-Metadaten prüfen; keine privaten Unterlagen oder Zugangsdaten.
- [x] Geprüften Stand am festgelegten GitHub-Ziel bereitstellen oder als ZIP abgeben.
- [x] Frischen Git-Checkout vollständig prüfen; Commit-/Abgabestand, Zugriff, erfolgreichen CI-Lauf und R11 dokumentieren.

Prüfung: veröffentlichte Dateien und Commit-Stand mit lokal geprüftem Stand vergleichen; Zugriff für den vorgesehenen Empfänger berücksichtigen. Umfang: klein. Nachrichten oder Einladungen werden als eigener Abgabeschritt erst nach entsprechendem Auftrag versendet.

## P08: Oberfläche und sichere Bedienabläufe

Status: erledigt. Auftrag: ruhige moderne Gestaltung und Bestätigung kritischer Aktionen. Nachweis: LOG-022.

- [x] Liste und Formular visuell überarbeiten, bestehende responsive Struktur erhalten.
- [x] Löschbestätigung mit Aufgabenname und Abbrechen ergänzen.
- [x] Umbenennen mit Vorher-/Nachher-Vorschau ergänzen.
- [x] Statuswechsel mit Rückgängig-Aktion versehen.
- [x] Vollständigen Verify-Lauf, Browser-/axe-Prüfung und Sichtprüfung abschließen; laufende Vorschau aktualisieren.

## P09: Bestätigter Mobile-First-Entwurf

Status: erledigt. Nachweis: LOG-023. Auftrag: ruhig und hochwertig, helle Grautöne mit Blauviolett, kompakte Eingabe oberhalb der Liste, Statuskreis und Icon-Aktionen, Dialoge über der Liste.

- [x] CSS-Basis Mobile First mit min-width-Erweiterungen umsetzen.
- [x] Eingabe über die Liste setzen; Stift-/Papierkorb-Aktionen und Statuskreis umsetzen.
- [x] Native Dialoge mit Fokusführung, Abbrechen und serverseitigem Fallback integrieren.
- [x] Browsermatrix 320/375/1280 px mit Touch, Tastatur, axe und JavaScript-freiem Fallback abschließen.
- [x] Sichtprüfung, Dokumentation und aktualisierte Live-Vorschau abschließen.
