# Umsetzungsplan

Status: Architektur angenommen; Technische Umsetzung P00–P06 und P08–P10 abgeschlossen; Empfängerübergabe P07 in Arbeit. Privates GitHub-Repository, frischer Checkout und erfolgreiche CI sind dokumentiert. Aktueller Bearbeitungsstatus ausschließlich in [todo.md](todo.md).

## Vorgehen

Zuerst die technische Startfähigkeit und PostgreSQL-Kompatibilität absichern. Danach vollständige fachliche Abläufe umsetzen: Erstellen/Anzeigen, Ändern, Löschen. Jede Funktion reicht von der Oberfläche bis zur Datenbank und erhält passende Tests. Abschließend Gesamtprüfung und Abgabevorbereitung.

## Reihenfolge

| Schritt | Ergebnis | Voraussetzung |
|---|---|---|
| P00 | Geprüfte Architektur und dokumentierter Umfang | Dokumentationsentwurf |
| P01 | Initializr-Projekt, Maven Wrapper, stabile Versionen, JAR | P00 |
| P02 | PostgreSQL, Flyway, Schema und Testanbindung | P01 |
| K1 | Technische Grundlage geprüft | P01–P02 |
| P03 | Aufgaben anlegen und anzeigen, durchgängig bis PostgreSQL | K1 |
| P04 | Namen und Erledigt-Status ändern | P03 |
| K2 | Hauptabläufe und Architekturgrenzen geprüft | P03–P04 |
| P05 | Löschen samt Fehlerfällen | K2 |
| P06 | Oberfläche, Gesamtprüfung und getestete Startanleitung | P05 |
| K3 | Alle fachlichen Anforderungen geprüft | P06 |
| P07 | Repository prüfen und Abgabe durchführen | K3 und festgelegtes Abgabeziel |

Die Teilaufgaben in `todo.md` sind die Ausführungseinheiten. Checkpoints dokumentieren Prüfergebnisse; sie sind keine zusätzlichen pauschalen Freigaberunden.

## Risiken und Maßnahmen

| Risiko | Maßnahme |
|---|---|
| Generator bietet keine passende Boot-3-Version | Vor Projektanlage prüfen; keinen stillen Wechsel der Pflichtversionen durchführen |
| Java 17 oder Container-Laufzeit fehlt | Voraussetzungen früh prüfen; fehlende Umgebung konkret melden |
| Flyway-/PostgreSQL-Kombination inkompatibel | Leere Datenbank und Migration vor UI-Arbeit mit echten PostgreSQL-Tests prüfen |
| Browser-Formulare und CSRF passen nicht zusammen | Sicherheitskonfiguration und erstes Formular bereits in P03 integriert testen |
| Tests laufen nur teilweise | Testausführung und Failsafe-Ergebnisse im Verify-Lauf kontrollieren |
| Dokumentation entfernt sich vom Code | Betroffene Verträge, Anleitung und Arbeitsprotokoll mit jeder Änderung aktualisieren |
| Zu viel Architektur für kleine Aufgabe | Ein Modul, eine Entity, ein Service; weitere Abstraktionen nur bei konkretem Bedarf |
| Abgabeziel fehlt | Lokale Umsetzung bleibt möglich; Veröffentlichung wartet auf Ziel und Sichtbarkeit |

## Änderungsregel

Neue Anforderungen werden zuerst gegen das Ziel und die Ausschlüsse geprüft. Änderungen an Paketen, Persistenz oder Schnittstellen benötigen einen aktualisierten Vertrag und bei grundsätzlichen Entscheidungen eine ADR. Bestehende unerledigte Arbeit wird nicht stillschweigend ersetzt.

## Ausführungsdetails P00–P02

Freigegeben am 24.09.2026. Projektort: `Development/task-management`.

1. Initializr-Gerüst mit Java 17, Maven, JAR und Paket `de.sinanucar.taskmanagement` beziehen. Boot-3-Kompatibilitätsanpassung gemäß ADR 0002 durchführen. Maven Wrapper beibehalten.
2. Build-Abhängigkeiten auf Spring Boot 3.5.16 ausrichten. Unit-Tests über Surefire, `*IT` über Failsafe mit `failIfNoTests=true` ausführen.
3. `TaskTest` mit ungültigen Namen, Java-Zeichenlängen und unveränderlichen Feldern anlegen. `./mvnw test` muss zunächst wegen fehlendem Modell scheitern.
4. `task.domain.Task(String name, Priority priority, Instant created)` mit `rename(String)`, `setDone(boolean)` und lesenden Zugriffen implementieren. Keine Setter für ID, priority oder created.
5. `V1__create_tasks.sql`, `TaskRepository.findAllByOrderByCreatedDescIdDesc()` und PostgreSQL-Service in `compose.yaml` ergänzen. Domain-Tests erneut ausführen.
6. `TaskRepositoryIT` prüft Migration, Persistenz-Roundtrip, Sortierung, Änderungen, Löschen und DB-Constraints gegen `postgres:17.9-alpine`. Tests lassen PostgreSQL-Ausfälle nicht als übersprungene Tests durchgehen.
7. `./mvnw verify` ausführen; Testanzahlen und tatsächliche Abhängigkeiten dokumentieren. Anschließend Compose-Start, JAR-Start und Schema-Validierung prüfen; nur selbst gestartete Prozesse beenden.
8. README, Aufgabenstatus und Arbeitsprotokoll auf den belegten Stand aktualisieren. P03–P07 bleiben offen.

## Ausführungsdetails P03

Freigegeben: Anlegen und Anzeigen einschließlich Validierung, CSRF und durchgängiger PostgreSQL-Prüfung. P04/P05 bleiben separat.

- TaskService verwendet Clock und Transaktionen. TaskData transportiert unveränderliche Daten ohne offene Persistenzsession.
- CreateTaskForm erlaubt ausschließlich name und priority. NORMAL wird nur beim GET vorgewählt; fehlende POST-Priorität ist ungültig. Name wird vor der Validierung mit strip normalisiert; Nullzeichen werden abgelehnt.
- MVC verarbeitet GET /tasks und POST /tasks. Erfolg liefert 303, Eingabefehler 400 mit erhaltenen Formularwerten. CSRF bleibt aktiviert. HTML wird escaped; Fehlerdetails verlassen den Server nicht.
- Gestaltung: Systemschrift für Bedienung, Monospace für kompakte Metadaten; #202d40 Text, #536277 Sekundärtext, #f5f7fb Hintergrund, #ffffff Flächen, #d6deea Trennlinien und #2458a6 Akzent. Links eine zeilenbasierte Liste, rechts ein Formular; unter 760 px untereinander. Die klare horizontale Listenbegrenzung und schmale blaue Formularkante gliedern die Arbeitsfläche ohne dekorative Kartenvielfalt.
- Prüfungen: Service-Snapshots und Uhrzeit, echte MVC-/Security-Kette mit PostgreSQL, ungültige Eingaben, Escaping und Feldschutz. Architekturtests um Web-/Application-Grenzen erweitern. Anschließend 375-/1280-px-Browserprüfung, Tastaturbedienung und Datenbanknachweis.

## Ausführungsdetails P04

- TaskService ergänzt getTask, renameTask und setTaskDone. Jede Mutation lädt den Task innerhalb ihrer eigenen Transaktion; fehlende Tasks führen zu einer fachlichen TaskNotFoundException und im Web zu 404.
- GET /tasks/{id}/edit stellt den bestehenden Namen bereit. POST /tasks/{id}/name bindet ausschließlich name; ID kommt aus dem Pfad. Bei ungültigem Namen bleibt der eingegebene Wert im Formular erhalten und die Datenbank unverändert.
- POST /tasks/{id}/done akzeptiert ausschließlich die eindeutigen Werte true und false. Fehlende, mehrfache oder sonstige Werte liefern 400. Wiederholtes Setzen desselben Zustands ist stabil.
- Die Liste zeigt pro Aufgabe einen Umbenennen-Link und eine statusabhängige Aktion. Erledigt wird zusätzlich als Text und durchgestrichener Name sichtbar. Bestehende Farben, Abstände, responsive Umbrüche und Fokusregeln bleiben erhalten.
- Tests prüfen Namen/Status, unveränderte priority/created, ungültige IDs, CSRF, GET-Schutz und manipulierte Zusatzfelder. Danach Browserdurchlauf bei 375/1280 px und Datenbanknachweis.

## Ausführungsdetails P05

- deleteTask lädt die Aufgabe innerhalb einer Schreibtransaktion und löscht ausschließlich diese Entity. Unbekannte IDs liefern 404.
- POST /tasks/{id}/delete nutzt den vorhandenen CSRF-Schutz und liefert nach Erfolg 303 zur Liste. Zusätzliche Formular-IDs beeinflussen das Löschziel nicht.
- Ein eindeutig beschrifteter Löschbutton ist visuell von den Statusaktionen getrennt. Ein zusätzlicher Dialog ist gemäß Vertrag nicht erforderlich.
- Service-/Webtests, PostgreSQL-Nachweis und Browserprüfung einschließlich Tastatur und 375/1280 px erfolgreich durchgeführt.

## Ausführungsdetails P06

- SpotBugs im Verify-Lauf; Ausnahmen einzeln dokumentiert und eng gefiltert.
- Isolierter Playwright-/axe-Lauf für Chromium Desktop/Mobil und WebKit, einschließlich Link- und Tastaturprüfung.
- Aufgelöste Produktionsabhängigkeiten mit OSV prüfen; Sicherheitsupdates innerhalb bestehender Bibliotheksfamilien testen.
- Abnahmeskript für Datenpersistenz nach Neustarts, 1.000 Aufgaben und Datenbankausfall.
- CI mit Leserechten und festgeschriebenen Actions vorbereitet. Vor Git-Anlage wird eine frische Quellkopie geprüft; der tatsächliche Git-Checkout und Live-CI-Nachweis folgen in P07.

## Ausführungsdetails P08

Zusätzlicher UI-Auftrag nach P06: ruhige Arbeitsoberfläche mit Schieferblau #243440, Petrol #17645f, Hintergrund #f3f6f7, Weiß #ffffff, Trennlinien #dce4e8 und Fehlerrot #a02932. Avenir Next mit System-Fallback für Überschriften, Systemschrift für Bedienung, Monospace für knappe Metadaten. Zwei getrennte Arbeitsflächen für Liste und Eingabe; innerhalb der Liste bleiben Aufgaben schlichte Zeilen. Keine Animationen oder externen Schriften.

Löschen führt zunächst zu einer lesenden Bestätigungsseite. Umbenennen validiert den gewünschten Namen in einer Vorschau und speichert erst im finalen POST. Beide Ansichten bieten Abbrechen. Statusänderungen zeigen eine einmalige Rückgängig-Aktion. Bestehende POST-Verträge, CSRF und Validierung bleiben bestehen; Bestätigung ist Bedienführung, keine Autorisierung. Integrationstests und Browserabläufe prüfen insbesondere Nicht-Speichern beim Abbrechen und Wiederherstellen des vorherigen Status.

## Ausführungsdetails P09

Bestätigte Richtung: ruhig und hochwertig; heller Hintergrund #f6f7fb, Weiß #ffffff, Text #202434, Sekundärtext #697082, Blauviolett #5149cc und Trennlinien #e6e8f0. Überschriften in Avenir Next mit System-Fallback, Bedienung in Systemschrift. Prägende Struktur ist eine kompakte Eingabe oberhalb einer großzügigen, durchgehenden Aufgabenliste.

Mobile First ist verbindlich: Basis ab 320 px, ausschließlich min-width-Erweiterungen, stabile Touch-Flächen, mobile Eingaben mit 16 px. Statuskreis links und sichtbare Stift-/Papierkorb-Aktionen rechts bzw. darunter. Native Dialoge lassen die Aufgabenliste sichtbar; serverseitige Bestätigungsseiten bleiben als Fallback erhalten. Prüfreihenfolge: zuerst mobile Chromium-Ansichten bei 375 und 320 px, anschließend Desktop und WebKit mobil/desktop.
