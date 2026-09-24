# Task Management

Eine kleine Webanwendung zum Erstellen, Anzeigen, Umbenennen, Erledigen und Löschen von Aufgaben. Die Daten werden in PostgreSQL gespeichert.

## Projektstand

Stand: 24.09.2026. P00–P06 und die zusätzliche UI-Überarbeitung sind implementiert und lokal geprüft. Aufgaben lassen sich über die deutsche Browseroberfläche anlegen, anzeigen, umbenennen, erledigen, wieder öffnen und löschen.

## Technischer Rahmen

- Java 17, Maven 3.9.16 über den mitgelieferten Wrapper, ausführbare JAR.
- Spring Boot 3.5.16; Initializr-Herkunft und Kompatibilitätsanpassung siehe [ADR 0002](docs/decisions/0002-initializr-compatibility.md).
- Spring MVC und Thymeleaf für die deutsche Weboberfläche, Spring Security für CSRF-Schutz.
- Spring Data JPA, PostgreSQL 17.9 und Flyway 11.7.2.
- Automatische Format-, Java-/Maven-Versions-, Architektur-, Modell-, PostgreSQL- und SpotBugs-Prüfungen.
- Gezielte Sicherheitsupdates und begründete Prüfausnahmen: [Abhängigkeiten](docs/DEPENDENCIES.md).

## Dokumentation

| Dokument | Zuständigkeit |
|---|---|
| [Anforderungen](docs/REQUIREMENTS.md) | Ziel, verbindlicher Aufgabenumfang, Annahmen und Ausschlüsse |
| [Architektur](docs/ARCHITECTURE.md) | Komponenten, Abhängigkeiten, Datenfluss und geplante Verzeichnisstruktur |
| [Entscheidungen](docs/decisions/0001-architecture-baseline.md) | Alternativen, Begründungen, Folgen und Entscheidungsstatus |
| [Verträge](docs/CONTRACTS.md) | Datenmodell, Geschäftsregeln, HTTP-Verhalten und Fehlerfälle |
| [Entwicklungsregeln](CONTRIBUTING.md) | Arbeitsablauf, Codekonventionen, Änderungsgrenzen und Protokollierung |
| [Qualität](docs/QUALITY.md) | Teststrategie, Anforderungsabdeckung und Definition of Done |
| [Umsetzungsplan](tasks/plan.md) | Reihenfolge, Risiken und Meilensteine |
| [Aufgaben](tasks/todo.md) | Verbindliche Aufgabenstatus und Abnahmekriterien |
| [Arbeitsprotokoll](docs/WORK_LOG.md) | Tatsächlich ausgeführte Arbeit, Entscheidungen und Prüfungen |

## Voraussetzungen

- JDK 17. Maven muss nicht separat installiert werden.
- Laufende Docker-kompatible Container-Engine für Integrationstests und lokale Datenbank.
- Docker Compose v2; alternativ das eigenständige `docker-compose` v2.
- Netzwerkzugriff für den ersten Download von Maven, Dependencies und Container-Images.

## Bauen und Prüfen

```sh
./mvnw verify
```

Der Lauf prüft Formatierung, Toolchain und statische Fehleranalyse, führt Unit-/Architekturtests über Surefire und PostgreSQL-Integrationstests über Failsafe aus und erzeugt `target/task-management-0.1.0.jar`. Testcontainers startet eine isolierte PostgreSQL-Instanz; die lokale Compose-Datenbank ist dafür nicht erforderlich. Ohne erreichbare Container-Engine schlägt die Gesamtprüfung fehl.

Weitere gezielte Befehle:

```sh
./mvnw test
./mvnw spotless:apply
```

`test` allein führt keine PostgreSQL-Integrationstests aus und ersetzt nicht `verify`. `spotless:apply` formatiert Java-Dateien; `verify` ändert keine Formatierung.

Bei Colima gegebenenfalls für die aktuelle Shell setzen:

```sh
export DOCKER_HOST="unix://${HOME}/.colima/default/docker.sock"
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
```

## Lokal starten

```sh
docker compose up -d --wait
java -jar target/task-management-0.1.0.jar
```

Bei eigenständigem Compose denselben Aufruf als `docker-compose up -d --wait` ausführen. Dieser alternative Startweg wurde mit Compose 2.39.4 geprüft.

Die Anwendung bindet an `127.0.0.1:8080`, PostgreSQL an `127.0.0.1:5433`. Die Aufgabenliste ist unter http://127.0.0.1:8080/tasks erreichbar. Der Einstieg unter / leitet dorthin weiter. Neue Aufgaben beginnen als offen; die Priorität wird beim Anlegen gewählt.

`TASK_DB_PORT` kann den lokalen Datenbankport überschreiben. `TASK_DB_PASSWORD` überschreibt in Compose und Anwendung denselben Wert. Der öffentliche Standard `local-development-only` ist ausschließlich eine lokale Entwicklungs-Vorgabe, kein geschütztes Geheimnis und keine Konfiguration für öffentlichen Betrieb. Reale Zugangsdaten gehören nicht ins Repository. Eine Passwortänderung per Umgebungsvariable ändert bei einem bereits initialisierten PostgreSQL-Volume nicht automatisch das Datenbankpasswort.

Die Anwendung lässt sich bei belegtem HTTP-Port mit `--server.port=8081` starten. Für normale Stop-/Startvorgänge:

```sh
docker compose stop
docker compose up -d --wait
```

Das benannte Datenbankvolume bleibt erhalten. Die Anwendung wird im Terminal mit Strg+C beendet. Destruktive Volume-Resets sind kein Bestandteil des normalen Startablaufs.

## Verifizierter Stand

- 69 Prüfungen bestanden: 11 Modelltests, 7 Service-Tests, 5 Architekturprüfungen, 1 Fehlerseitentest sowie 45 PostgreSQL-/Web-Integrationstests; keine Fehler und keine übersprungenen Tests.
- Spotless, Java-/Maven-Enforcer und kompletter Maven-Verify-Lauf erfolgreich.
- Java-17-Bytecode in der gebauten JAR geprüft.
- JAR-Start gegen Compose-PostgreSQL, Flyway-Migration und Hibernate-Schemavalidierung erfolgreich.
- Datenpersistenz nach Anwendungs- und Datenbankneustart nachgewiesen; Prüfsatz anschließend entfernt.

Die Prüfumgebung verwendete Java 17.0.8.1 auf macOS/arm64 und Docker 29.5.2. Wegen langsamer Cache-Dateizugriffe wurde ein separater Maven-Cache unter einem lokalen temporären Verzeichnis verwendet. Das ist keine erforderliche Projekteinstellung.

Für P03 zusätzlich geprüft: CSRF, manipulierte Formularfelder, Eingabevalidierung, sichere HTML-Ausgabe und die neutralen Fehlerseiten. Im Browser wurden Anlegen, Fehlermeldungen, Neuladen, Tastaturbedienung und Layout bei 375/1280 px gegen eine isolierte PostgreSQL-Datenbank geprüft.

Für P04 zusätzlich geprüft: Umbenennen, Erledigen und Wiederöffnen einschließlich unbekannter IDs, strikter Statuswerte und unveränderlicher Felder. Browserprüfung bei 375/1280 px, Speichern per Tastatur, Feldfehler und Abbrechen erfolgreich; PostgreSQL bestätigt unveränderte Priorität und Erstellungszeit.

Für P05 zusätzlich geprüft: gezieltes Löschen, erneutes Löschen mit 404, ungültige IDs, CSRF und GET-Schutz. Browserprüfung mit Tastatur, Neuladen und Leerzustand erfolgreich; PostgreSQL bestätigt die Entfernung.

Die überarbeitete Oberfläche bestätigt Löschen mit Aufgabenname und zeigt beim Umbenennen bisherigen und neuen Namen vor dem Speichern. Statusänderungen bieten Rückgängig. Umbenennen und Löschen öffnen native Dialoge über der Liste. Ohne JavaScript bleiben die serverseitigen Seiten als Fallback nutzbar.

Die Oberfläche folgt Mobile First: Basislayout ab 320 px, min-width-Erweiterungen, mobile Eingaben mit 16 px und mindestens 44 × 44 px große Icon-Aktionen. Die kompakte Eingabe steht über der Liste.

## Erweiterte Qualitätsprüfung

Zusätzlich: Node 22, npm und Python 3. Mit nvm wählt `nvm use` die Projektversion. Danach:

```sh
npm ci --ignore-scripts
npx playwright install chromium webkit
npm run test:e2e
python3 scripts/acceptance.py
./mvnw dependency:list -DincludeScope=runtime -DoutputFile=target/runtime-dependencies.txt
python3 scripts/audit-dependencies.py
npm audit --audit-level=low
```

Browser- und Abnahmeskripte starten eigene temporäre PostgreSQL-Container und die bereits gebaute JAR auf freien lokalen Ports. Sie beenden ihre Prüfumgebung anschließend. Sie verwenden keine normale Projektdatenbank. Berichte liegen in `target/`, `test-results/` und `playwright-report/`; diese Verzeichnisse gehören nicht ins Repository. Für die Browserprüfung unter Linux installiert `npx playwright install --with-deps chromium webkit` zusätzlich die Systembibliotheken.

Der Browserlauf umfasst 15 Fälle in Chromium bei 320/375/1280 px und WebKit bei 375/1280 px. Er prüft Aufgabenablauf, Eingabefehler, Tastatur, Fokus-Rückgabe, Escape, lange Namen, sichere HTML-Ausgabe, Links, axe-Regeln, Dialog-Ladefehler und Fallback ohne JavaScript. Mobile Kontexte verwenden Touch-Emulation; echte Mobilgeräte wurden nicht getestet. Der separate Abnahmelauf prüft Datenpersistenz nach Anwendungs-/Datenbankneustart, 1.000 Aufgaben und eine neutrale 500-Fehlerseite bei Datenbankausfall. Er ist ein repräsentativer lokaler Funktionstest, kein Lasttest.

Die CI in `.github/workflows/quality.yml` ist vorbereitet, verwendet festgeschriebene Actions und ausschließlich Leserechte. Ein tatsächlicher GitHub-Lauf und ein Checkout aus dem veröffentlichten Repository bleiben P07 zugeordnet. Vor der Repository-Anlage erfolgt die Reproduzierbarkeitsprüfung mit einer frischen Quellkopie ohne Build-Ausgaben und ohne node_modules. Details: [Qualität](docs/QUALITY.md) und [Abhängigkeiten](docs/DEPENDENCIES.md).


## Dokumentationsregeln

Anforderungen stehen in `REQUIREMENTS.md`, technische Verträge in `CONTRACTS.md`, Aufgabenstatus ausschließlich in `tasks/todo.md`. Das Arbeitsprotokoll dokumentiert historische Ereignisse, ersetzt aber keinen aktuellen Aufgabenstatus. Änderungen an einer Entscheidung müssen die betroffenen Verträge, Aufgaben und Prüfungen mitziehen.
