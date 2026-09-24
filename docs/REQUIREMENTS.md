# Ziel und Anforderungen

## Ziel

Eine nachvollziehbare, lokal startbare Aufgabenverwaltung liefern, deren Kernabläufe vollständig im Browser funktionieren und dauerhaft in PostgreSQL gespeichert werden. Die Lösung soll sich im technischen Interview anhand ihres Codes, ihrer Tests und ihrer dokumentierten Entscheidungen erklären lassen.

Grundlage: die bereitgestellte Aufgabenbeschreibung, vollständig gelesen und visuell geprüft. Die Originaldatei ist nicht Bestandteil des Projekts. Die folgenden Anforderungen sind eine Zusammenfassung; zusätzliche Projektentscheidungen sind separat markiert.

## Verbindliche Anforderungen aus der Aufgabe

| ID | Anforderung | Abnahme |
|---|---|---|
| R01 | Java 17, Maven, JAR, Spring Boot 3 oder Quarkus 3 ohne Snapshot | Projektkonfiguration erfüllt alle Vorgaben; Maven-Build erzeugt startbare JAR |
| R02 | Erstellung mit Spring Initializr oder Quarkus Start | Verwendeter Generator und Parameter sind bei Projektanlage protokolliert |
| R03 | Bedienung über eine einfache Weboberfläche | Kernabläufe funktionieren ohne API-Werkzeug im Browser |
| R04 | Speicherung in PostgreSQL | Aufgaben und Änderungen bleiben nach Anwendungsneustart erhalten |
| R05 | Task mit name, done, created und priority | Datentypen String, boolean, Instant und Priority werden korrekt abgebildet |
| R06 | Priority als Enum LOW, NORMAL, URGENT | Ausschließlich diese Werte sind zulässig |
| R07 | Task erstellen | Eine gültige Eingabe erzeugt eine gespeicherte Aufgabe |
| R08 | Task ändern: name beziehungsweise done | Name und Erledigt-Status lassen sich unabhängig ändern |
| R09 | Task löschen | Gelöschte Aufgabe bleibt auch nach Neuladen entfernt |
| R10 | Laufzeit direkt mit Java oder als Docker-Container | Mindestens ein dokumentierter Startweg ist geprüft |
| R11 | Abgabe als ZIP oder über GitHub/GitLab | Vollständiges Projekt kann über den gewählten Kanal bezogen werden |

Frontend-Technologie und Dependencies sind frei. Deutsch oder Englisch sind erlaubt. Docker für PostgreSQL ist optional. Eine README ist bei Erklärungsbedarf vorgesehen. Unklare Punkte dürfen eigenständig entschieden und begründet werden.

## Angenommene Konkretisierungen

Diese Regeln ergänzen die Aufgabe; sie sind keine zusätzlichen Forderungen des Auftraggebers.

| ID | Entscheidung | Zweck |
|---|---|---|
| A01 | Eine gemeinsame Aufgabenliste ohne Benutzerkonten | Kleinster vollständiger Anwendungsumfang |
| A02 | Serverseitig vergebene numerische ID | Eindeutiges Ändern und Löschen |
| A03 | done=false und serverseitiges created beim Anlegen | Eindeutiger Ausgangszustand |
| A04 | Priorität beim Anlegen auswählbar, Standard NORMAL | Pflichtfeld sinnvoll bedienbar machen |
| A05 | Priorität nach dem Anlegen unverändert | Änderungsumfang auf name und done begrenzen |
| A06 | Name nach String.strip(): 1 bis 200 Java-Zeicheneinheiten | Leere und überlange Eingaben ablehnen |
| A07 | Liste nach created absteigend, dann ID absteigend | Deterministische Anzeige |
| A08 | Deutsche Oberfläche, englische Codebezeichner | Konsistenz |
| A09 | Lokaler Einzelbenutzerbetrieb, kein öffentliches Hosting | Klarer Betriebsrahmen ohne Kontenverwaltung |

## Nicht im Umfang

Authentifizierung, Rollen, Projekte, Kommentare, Tags, Fristen, Anhänge, Suche, Pagination, Kanban, Echtzeit-Synchronisierung, REST-Schnittstelle für Dritte und Cloud-Betrieb. Ein zusätzlicher Docker-Startweg für die Anwendung ist optional und darf den geprüften JAR-Start nicht verzögern.

## Fertig bedeutet

R01 bis R11 sind nachvollziehbar erfüllt, die Qualitätskriterien aus [QUALITY.md](QUALITY.md) sind geprüft und Einschränkungen sind ehrlich dokumentiert. Ein nur teilweise umgesetztes oder ungeprüftes Kriterium gilt nicht als erledigt.

## Noch zu klären oder zu prüfen

- Architektur am 24.09.2026 angenommen; technische Grundlage geprüft. Fachliche Oberfläche und vollständige Abnahme stehen aus.
- Versionen und Generatorherkunft sind geprüft und festgeschrieben; siehe ADR 0002 und README. Java 17 und Framework-Major-Version 3 bleiben Vorgaben.
- GitHub-Zielrepository und Sichtbarkeit vor Veröffentlichung festlegen. Aktuell existiert hier kein Repository.
- Deadline, Zeitbudget und zusätzliche Bedingungen aus einer Begleitnachricht sind nicht bekannt. Es werden keine angenommen.
