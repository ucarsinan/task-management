# Arbeitsprotokoll

Das Protokoll hält wesentliche tatsächliche Schritte fest. Neue Einträge werden angehängt. Aktueller Aufgabenstatus: [Aufgabenliste](../tasks/todo.md). Architekturvorschläge sind keine umgesetzten oder bereits angenommenen Entscheidungen.

## LOG-001 — Aufgabenstellung analysiert

- Datum: 24.09.2026.
- Bezug: P00, R01–R11.
- Aktion: Einseitige PDF vollständig als Text gelesen und visuell geprüft. Pflichtanforderungen, implizite Listenansicht, offene Prioritätsbedienung und optionale Docker-Ausführung unterschieden.
- Ergebnis: Java 17, Maven, Framework-Major-Version 3, JAR, PostgreSQL und Browser-Kernabläufe sind die technische Grundlage.
- Prüfung: Textinhalt gegen gerenderte PDF-Seite geprüft.
- Offen: Ergänzende Bedingungen aus einer Begleitnachricht liegen nicht vor.

## LOG-002 — Projektkontext geprüft

- Datum: 24.09.2026.
- Bezug: P00.
- Aktion: Arbeitsordner und übergeordnete lokale Projektanweisungen geprüft. Kein bestehender Anwendungscode oder bestehendes Projektdokumentationspaket vorhanden.
- Ergebnis: Eigenständiges Dokumentationsverzeichnis `task-management` angelegt. Vorhandene Analyse-Zwischendateien bleiben außerhalb des Lieferverzeichnisses.
- Prüfung: Dateiliste des Ausgangsordners kontrolliert.
- Offen: Git-Repository und Remote sind noch nicht eingerichtet.

## LOG-003 — Architekturgrundlage dokumentiert

- Datum: 24.09.2026.
- Bezug: P00.
- Aktion: Spring MVC/Thymeleaf, separates SPA und Quarkus als Varianten verglichen. Spring Boot 3 mit serverseitiger Oberfläche als Vorschlag dokumentiert. Offizielle Spring-Quellen zu Systemvoraussetzungen, MVC und Datenbankinitialisierung gelesen.
- Bereiche: README, REQUIREMENTS, ARCHITECTURE, CONTRACTS, ADR 0001, CONTRIBUTING, QUALITY, Aufgabenplan und Aufgabenliste.
- Ergebnis: Ziel, Ausschlüsse, Paketgrenzen, Fachregeln, Fehlerverhalten, Tests, Arbeitsschritte und Abgabegrenzen sind im Entwurf festgehalten.
- Prüfung: Quellen stützen die grundsätzliche Framework-Kombination; konkrete Dependencies wurden noch nicht installiert oder gebaut.
- Offen: Architekturreview und spätere Versions-/Laufzeitprüfung. Anwendungscode, Anwendungstests und Browserabnahme noch nicht begonnen.

## LOG-004 — Dokumentation geprüft

- Datum: 24.09.2026.
- Bezug: P00.
- Aktion: Zehn Markdown-Dateien auf interne Links, Titel, geschlossene Codeblöcke, Platzhalter, lokale Benutzerpfade und unerwünschte Prozessreferenzen geprüft. Anforderungs-IDs R01–R11 und Plan-/Aufgaben-IDs P00–P07 abgeglichen.
- Prüfung: Lokale Python-Prüfung erfolgreich, Exit-Code 0; 13 interne Links geprüft, keine Fehler. Lieferverzeichnis enthält ausschließlich Markdown-Dateien.
- Fachliches Review: Pflichtanforderungen gegen den dokumentierten Scope abgeglichen. Datenmodell, Änderungsumfang, Formularpfade, Fehlerfälle und Prüfplan auf Widersprüche gelesen. Java 17, Framework-Major-Version 3 und JAR bleiben verbindlich; Docker für die Anwendung bleibt optional.
- Ergebnis: Dokumentationsentwurf strukturell geprüft und zur fachlichen Durchsicht bereit.
- Offen: Architektur ist weiterhin vorgeschlagen. Kein Build, kein Anwendungstest, keine Browserprüfung der Anwendung, kein Commit und kein Push durchgeführt.

## LOG-005 — Architektur angenommen und Projektanlage begonnen

- Datum: 24.09.2026. Bezug: P00–P02.
- Aktion: Architekturannahme und Umsetzungsauftrag für P00–P02 festgehalten. Projekt auf Wunsch nach `Development/task-management` verschoben; keine vorhandenen Zieldateien überschrieben.
- Prüfung: Java 17.0.8.1 und laufender Docker-Dienst vorhanden. Initializr lehnt Boot 3.5.16 mit HTTP 400 ab; kompatibles Parent-POM in Maven Central vorhanden.
- Entscheidung: offizielles Initializr-Gerüst erzeugt und gezielt auf Boot 3 angepasst; siehe ADR 0002. Maven Wrapper 3.9.16 übernommen.
- Umgebung: Compose fehlt in der Docker-Installation. Ein separates Compose-v2.39.4-Prüfprogramm wird außerhalb des Projekts verwendet; Download-Prüfsumme verifiziert. Keine globale Installation geändert.
- Offen: Modell, Migration, Build und Integrationstests folgen; noch kein Commit oder Push.

## LOG-006 — Modell und Persistenz implementiert

- Datum: 24.09.2026. Bezug: P01–P02.
- Aktion: TaskTest vor Modellimplementierung ausgeführt; erwarteter Compilerfehler durch fehlende Task-/Priority-Klassen nachgewiesen. Anschließend validiertes Task-Modell, Enum, Repository, Flyway-V1-Migration und isolierte PostgreSQL-Integrationstests implementiert.
- Bereiche: pom.xml, Maven Wrapper, .gitignore, application.yaml, compose.yaml, src/main und src/test.
- Ergebnis: Java 17, Boot 3.5.16, Maven 3.9.16, PostgreSQL 17.9, Hibernate 6.6.53.Final, Flyway 11.7.2, JDBC-Treiber 42.7.11, Testcontainers 1.21.4. Datenmodell und Änderungsgrenzen folgen CONTRACTS.
- Prüfung: Ein erster Verify-Versuch wurde wegen ungewöhnlich langsamer Cache-Dateizugriffe beendet. Thread-Aufnahmen zeigten lokale Dateileseoperationen; die genaue Betriebssystemursache ist nicht belegt. Mit einem getrennten Cache im lokalen temporären Verzeichnis war der vollständige Lauf erfolgreich.

## LOG-007 — Qualitätsanforderung erweitert

- Datum: 24.09.2026. Bezug: P02 und spätere P03–P07.
- Aktion: Verbindliche Qualitätssicherung über den ganzen Projektverlauf aufgenommen. Spotless 3.0.0 mit Google Java Format 1.24.0, Maven Enforcer und ArchUnit 1.4.1 ergänzt. Surefire/Failsafe schlagen bei fehlenden Tests fehl. Transaktions-Rollback wird gegen PostgreSQL geprüft.
- Ergebnis: Formatierung, Toolchain, vorhandene Paketgrenzen und Kern-Datenregeln sind automatisiert abgesichert. Spätere Web-Sicherheits-, Browser-, Barrierefreiheits-, Dependency-, statische Analyse- und CI-Prüfungen stehen als konkrete Kriterien in QUALITY und Aufgabenliste.
- Grenze: Noch keine Oberfläche, keine Browser-/Sicherheitsabnahme und kein live ausgeführter CI-Lauf.

## LOG-008 — Vollständiger Grundlagen-Build bestanden

- Datum: 24.09.2026. Bezug: P01–P02, K1.
- Befehl: `./mvnw -B -ntp spotless:apply verify`, mit separatem lokalen Maven-Cache und Colima-Docker-Socket für Testcontainers.
- Ergebnis: BUILD SUCCESS, 10 Modelltests, 3 Architekturprüfungen, 11 PostgreSQL-Integrationstests; 0 Fehler, 0 übersprungene Tests. Formatanwendung und anschließender Formatcheck erfolgreich; Toolchain-Regeln bestanden. Dauer des dokumentierten Laufs: 54,667 Sekunden inklusive Downloads und erstem Testcontainer-Start.
- Nachweise: Surefire-/Failsafe-XML-Berichte ausgewertet. JAR enthält Spring Boot 3.5.16 und Classfile-Version 61 (Java 17).
- Prüfumgebung: macOS/arm64, Temurin 17.0.8.1, Docker Server 29.5.2, eigenständiges Compose 2.39.4.

## LOG-009 — Start und Neustart-Persistenz geprüft

- Datum: 24.09.2026. Bezug: P02, K1.
- Aktion: Projekt-PostgreSQL über Compose gestartet; Healthcheck erfolgreich. Gebaute JAR gegen diese Datenbank gestartet; Flyway V1 und Hibernate-Schemavalidierung erfolgreich.
- Prüfkorrektur: Der erste Prüfskriptlauf brach ab, weil ein Compose-Unterprozess SIGKILL erhielt. Der direkte Compose-Versionsaufruf und die Schemaabfrage funktionierten. Wiederholung mit der installierten Docker-CLI statt Compose-Unterprozessen erfolgreich; keine Änderung am Anwendungscode erforderlich. Ursache des SIGKILL nicht abschließend geklärt.
- Ergebnis: JAR zweimal gestartet, lokal mit erwarteter 404 erreichbar. Ein eigener Prüfsatz blieb nach Anwendungs- und PostgreSQL-Neustart erhalten. Prüfsatz entfernt, eigene JAR-Prozesse und Projektcontainer gestoppt; Datenbankvolume erhalten.
- Abschluss: P00–P02 und K1 erfüllt. README und Status aktualisiert. P03–P07 bleiben offen. Kein Git-Repository initialisiert, kein Commit und kein Push durchgeführt.

## LOG-010 — Abschlussprüfung der Grundlage

- Datum: 24.09.2026. Bezug: P00–P02, K1.
- Prüfung: 16 interne Dokumentlinks gültig; 24 erfolgreiche Testfälle aus den XML-Berichten bestätigt. Projektdateien auf Platzhalter, lokale Benutzerpfade und unerwünschte Prozessmetadaten geprüft; keine Auffälligkeiten.
- Betriebszustand: Projekt-Datenbankcontainer mit Exit-Code 0 gestoppt. Eine nicht mehr benötigte Cache-Diagnosekopie wurde beendet; der erfolgreich verwendete separate Prüfcache bleibt davon unberührt.
- Ergebnis: Technische Grundlage zur Weiterarbeit bereit. Keine Veröffentlichung erfolgt.

## LOG-011 — Projektbezeichnung vereinheitlicht

- Datum: 24.09.2026.
- Aktion: Projektordner und Compose-Projekt heißen einheitlich `task-management`. Herkunftsbezogene Namensreferenzen aus der Projektdokumentation entfernt und ältere Pfadangaben auf den aktuellen Projektort angepasst. Fachliche Anforderungen unverändert erhalten.
- Prüfung: Vollständiger Maven-Verify-Lauf unter dem neuen Pfad erfolgreich; 24 Tests ohne Fehler oder übersprungene Tests. Alte Build-Ausgaben aus dem Projekt entfernt und neu erzeugt.
- Datenhaltung: Gestopptes PostgreSQL-Volume vollständig in das neutral benannte Projektvolume kopiert; Dateivergleich ohne Unterschiede.
- Abschluss: Neue Compose-Instanz erfolgreich gestartet, Migration und Aufgabenbestand geprüft. Neue Instanz wieder gestoppt; alte Container-, Netzwerk- und Volumenbezeichnungen nach erfolgreicher Migration entfernt. Abschließende Suche im gesamten Projekt inklusive neuer Build-Ausgaben ohne Herkunftsnamens-Treffer; 16 interne Dokumentlinks gültig. Kein Commit oder Push.

## LOG-012 — Anlegen und Anzeigen implementiert

- Datum: 24.09.2026. Bezug: P03.
- Aktion: Service und unveränderliche TaskData-Ergebnisse, injizierte Clock, Formularmodell, Controller, Templates und responsive CSS-Oberfläche ergänzt. Name und Priorität sind die einzigen bindbaren Formularfelder. CSRF bleibt aktiv; es werden weder Anmeldung noch Benutzerkonten eingeführt.
- Eingabepräzisierung: PostgreSQL-unzulässiges U+0000 in Namen wird in Formular und Domain abgelehnt; im Vertrag dokumentiert. Rand-Whitespace und Zeichenlänge entsprechen dem bestehenden Vertrag.
- Prüfung: Neue Service-Tests scheiterten vor Implementierung erwartungsgemäß an den fehlenden Klassen. Anschließend Anwendung implementiert; Architekturgrenzen um Web-/Application-Regeln ergänzt.

## LOG-013 — Automatisierte P03-Prüfung

- Datum: 24.09.2026. Bezug: P03.
- Testkorrektur: Zwei Assertions verwendeten einen XML-Parser für HTML5 und scheiterten am Dokumenttyp. Auf jsoup als HTML-Parser umgestellt; keine Änderung der Dokumentstruktur zur Anpassung an den Test. Ergänzend neutrale Fehlerseite bei Datenbankfehler geprüft.
- Befehl: `./mvnw -B -ntp spotless:apply verify` mit lokalem Prüfcache und Colima-Anbindung.
- Ergebnis: BUILD SUCCESS; 45 Prüfungen, keine Fehler und keine übersprungenen Tests. Aufteilung: Domain 11, Service 3, Architektur 5, Fehlerseite 1, PostgreSQL-Persistenz 11, Web/PostgreSQL 14.
- Abgedeckt: Serverzeit, unveränderliche Snapshots, Validierung, fehlende/ungültige Priorität, Mass-Binding-Schutz, CSRF, Escaping, 303-Redirect und Erhalt vorhandener Aufgaben bei Eingabefehlern.

## LOG-014 — Browserabnahme P03

- Datum: 24.09.2026. Bezug: P03.
- Umgebung: Gebaute JAR auf lokalem Prüfport, eigene temporäre PostgreSQL-Instanz; keine bestehenden Projektdaten verwendet.
- Prüfung: Leerzustand, Pflichtfehler und Anlegen mit Priorität Dringend. Drei Prüfsätze über die Oberfläche angelegt, darunter ein 200-Zeichen-Name und HTML-Markup als Text. Direkte Datenbankabfrage bestätigt drei gespeicherte offene Aufgaben, maximale Namenslänge 200 und korrekte Priorität.
- Bedienung: Tab bewegt den Fokus von Name zu Priorität und zum Button; Enter legt die Aufgabe an. Fehlende Priorität zeigt Feldmeldung und erhält den Namen. Neuladen erzeugt keine weitere Aufgabe. HTML wird nicht als Element interpretiert.
- Layout: Screenshots und DOM-Abmessungen bei 375/1280 px geprüft; kein horizontaler Überlauf. Verknüpfte Fehlerbeschreibungen vorhanden. Vollständiger automatisierter Browser-/axe-Audit bleibt P06 zugeordnet.
- Ergebnis: P03 abgeschlossen; README, Architektur, Aufgabenstatus und Qualitätsstand aktualisiert. P04/P05 und Gesamtabnahme weiterhin offen. Kein Commit oder Push.
- Abschlussprüfung: 45 erfolgreiche Testfälle aus XML-Berichten bestätigt; 16 interne Dokumentlinks gültig, keine unerwünschten Namens-/Prozessreferenzen in Projektquellen. Temporäre JAR und Browser-Testdatenbank beendet; die normale Projektdatenbank wurde nicht verändert.

## LOG-015 — Umbenennen und Statuswechsel implementiert

- Datum: 24.09.2026. Bezug: P04, K2.
- Aktion: Service-Operationen für Lesen, Umbenennen und expliziten Statuswechsel, fachliche Not-found-Exception, Umbenennungsformular und zugehörige HTTP-Abläufe ergänzt. Liste zeigt statusabhängige Aktionen; erledigte Aufgaben erhalten Textstatus und durchgestrichenen Namen.
- Prüfung: Tests zuerst ergänzt; zunächst erwartetes Scheitern an fehlenden Service-Methoden. Anschließend Implementierung entlang bestehender Paketgrenzen. Formulare binden nur freigegebene Felder; Status akzeptiert ausschließlich true oder false.

## LOG-016 — Automatisierte P04-Prüfung

- Datum: 24.09.2026. Bezug: P04.
- Befehl: `./mvnw -B -ntp spotless:apply verify` mit lokalem Prüfcache und Colima-Anbindung.
- Ergebnis: BUILD SUCCESS; 63 Prüfungen, keine Fehler und keine übersprungenen Tests. XML-Berichte bestätigt: Domain 11, Service 6, Architektur 5, Fehlerseite 1, PostgreSQL-Persistenz 11, Web/PostgreSQL 29.
- Abgedeckt: Umbenennen, unveränderte priority/created/done, Status setzen und wiederholen, ungültige bzw. fehlende/mehrfache Statuswerte, unbekannte und fehlerhafte IDs, CSRF, GET-Schutz und manipulierte Zusatzfelder.

## LOG-017 — Browserabnahme P04

- Datum: 24.09.2026. Bezug: P04, K2.
- Umgebung: Gebaute JAR und separate temporäre PostgreSQL-Instanz; normale Projektdatenbank unverändert.
- Prüfung: Aufgabe mit Priorität Dringend angelegt; ungültiger Name zeigt Feldmeldung. Umbenennen per Tab/Enter gespeichert. Erledigen und Wiederöffnen funktionieren; Abbrechen verwirft den eingegebenen Namen.
- Datenbanknachweis: ID 1 bleibt erhalten; Name erfolgreich geändert, done wechselt auf true und zurück auf false. Priorität URGENT und Erstellungszeit 2026-09-24T17:19:01.655922Z bleiben unverändert.
- Layout: Liste und Bearbeitungsformular bei 375/1280 px visuell geprüft; Dokumentbreite entspricht jeweils der Fensterbreite.
- Abschluss: Temporäre JAR und Browser-Testdatenbank beendet. README, Architektur, Qualitätsstand und Aufgabenstatus aktualisiert. P04/K2 abgeschlossen; P05–P07 offen. Kein Commit oder Push.

## LOG-018 — Löschen umgesetzt und geprüft

- Datum: 24.09.2026. Bezug: P05.
- Aktion: Transaktionale Löschoperation, POST-Route und visuell getrennten Löschbutton ergänzt. Bestehende Fehlerbehandlung, Redirect und CSRF-Schutz wiederverwendet.
- Prüfung: Tests vor Implementierung erwartungsgemäß an fehlender deleteTask-Methode gescheitert. Anschließend `./mvnw -B -ntp spotless:apply verify` mit lokalem Prüfcache und Colima-Anbindung erfolgreich.
- Ergebnis: 67 Prüfungen ohne Fehler oder übersprungene Tests: Domain 11, Service 7, Architektur 5, Fehlerseite 1, PostgreSQL-Persistenz 11, Web/PostgreSQL 32.
- Abgedeckt: Gezieltes Löschen trotz zusätzlicher Formular-ID, Erhalt anderer Aufgaben, unbekannte/fehlerhafte IDs, erneutes Löschen mit 404, CSRF, GET-Schutz und Leerzustand.
- Browser: Eigene Testaufgabe angelegt; Löschbutton bei 375/1280 px visuell geprüft, kein horizontaler Überlauf. Tab fokussiert die Löschaktion, Enter löscht die Aufgabe. Leerzustand bleibt nach Neuladen erhalten; PostgreSQL bestätigt null Aufgaben.
- Abschluss: Temporäre JAR und eigene Testdatenbank beendet; normale Projektdaten unverändert. Dokumentation aktualisiert. P05 abgeschlossen; P06/P07 offen. Kein Commit oder Push.

## LOG-019 — Statische Analyse und Sicherheitsupdates

- Datum: 24.09.2026. Bezug: P06.
- Aktion: SpotBugs 4.10.4.1 im Verify-Lauf mit maximalem Aufwand und niedriger Meldeschwelle integriert. Zwei Konstruktorbefunde einzeln bewertet und eng gefiltert; Begründungen in DEPENDENCIES.md.
- Advisory-Prüfung: OSV meldete acht Advisories für vier der 78 Produktionsartefakte. Log4j auf 2.25.5, Jackson BOM auf 2.21.5, PostgreSQL JDBC auf 42.7.12 und Tomcat auf 10.1.60 angehoben. Tomcat 10.1.58 aus dem Advisory war nicht veröffentlicht; verfügbare 10.1.60 mit Herstellerkorrekturen verwendet.
- Ergebnis: Vollständiger Verify-Lauf mit 67 Tests und SpotBugs erfolgreich. OSV-Wiederholung ohne Treffer, npm audit ohne Treffer. Python benötigte lokal den vorhandenen System-Zertifikatsspeicher; TLS-Prüfung blieb aktiv.

## LOG-020 — Automatisierte Browserabnahme und CI

- Datum: 24.09.2026. Bezug: P06.
- Aktion: Node-22-Testwerkzeuge, Lockfile, isolierter Teststarter, Playwright- und axe-Prüfungen ergänzt. Die Anwendung erhält keinen Node-Laufzeitbedarf.
- Prüfkorrektur: Projekt-Shell verwendete zunächst Node 18; Node 22 explizit gewählt und Versionsanforderung hinterlegt. Firefox konnte trotz anderem temporären Verzeichnis kein Profil öffnen; WebKit als zweite Engine erfolgreich geprüft.
- Ergebnis: Drei Browserläufe erfolgreich: Chromium Desktop/Mobil und WebKit Desktop. Kernabläufe, Tastatur, Fehlerzustände, Links, lange Namen, Escaping und axe-Regeln geprüft. Screenshots bei 375/1280 px visuell kontrolliert.
- CI: Workflow mit Leserechten und festen Action-Commits sowie Dependabot-Konfiguration vorbereitet; kein Live-Lauf oder Push.

## LOG-021 — Frische Quellkopie, Neustart und Laufzeitprüfung

- Datum: 24.09.2026. Bezug: P06, K3.
- Umgebung: Frische Quellkopie ohne Build-Ausgaben und node_modules. Java 17.0.8.1, Node 22.13.1, Docker 29.5.2 auf macOS/arm64.
- Prüfung: Maven Verify mit 67 Tests, Spotless, Enforcer und SpotBugs erfolgreich; npm ci und drei Browserläufe erfolgreich. XML-Testberichte bestätigen null Fehler und null übersprungene Tests.
- Prüfkorrekturen: Dynamischen Docker-Port nach Neustart neu ermittelt; beim Zählen der Aufgaben die zusätzliche CSS-Klasse erledigter Aufgaben berücksichtigt. Für den Datenbankfehler wird die eigene temporäre Datenbank gestoppt.
- Ergebnis: Persistenz nach Anwendungs-/Datenbankneustart bestätigt; 1.000 Aufgaben vollständig gerendert. Fünf Listenabrufe 252,21 / 142,52 / 135,76 / 100,41 / 83,60 ms. Datenbankausfall liefert neutrale 500-Fehlerseite ohne technische Details.
- Abschluss: Eigene temporäre Anwendungen und Datenbanken beendet. P06/K3 lokal abgeschlossen. Frischer Git-Checkout, Live-CI und tatsächliche Abgabe verbleiben P07. Kein Git-Repository, Commit oder Push angelegt.

## LOG-022 — Moderne Oberfläche und Bestätigungen

- Datum: 24.09.2026. Bezug: zusätzlicher UI-Auftrag P08.
- Gestaltung: Schieferblau und Petrol, ruhige weiße Arbeitsflächen, klare Typografie und Abstände; bestehende responsive Aufteilung beibehalten.
- Verhalten: Löschbestätigung mit Aufgabenname und Abbrechen. Namensvorschau mit altem/neuem Namen, finaler Bestätigung und Abbrechen. Direkter Statuswechsel mit einmaliger Rückgängig-Aktion. Serverseitige Ansichten ohne JavaScript, bestehende Schreibvalidierung und CSRF erhalten.
- Prüfung: 69 Java-Prüfungen (24 Unit-/Architekturprüfungen, 45 PostgreSQL-/Webtests), keine Fehler oder übersprungenen Tests. Spotless, Enforcer und SpotBugs erfolgreich. Drei Browserläufe mit axe, Links, Tastatur, Bestätigen, Abbrechen und Rückgängig erfolgreich.
- Sichtprüfung: Liste und Bestätigungsansichten bei 375/1280 px einschließlich 200-Zeichen-Namen geprüft; kein horizontaler Überlauf.
- Abschluss: Verträge, Architektur, Aufgaben und Qualitätsstand aktualisiert. Laufende lokale Vorschau mit neuer JAR neu gestartet; normale PostgreSQL-Daten erhalten. Kein Commit oder Push.

## LOG-023 — Bestätigtes Mobile-First-Design

- Datum: 24.09.2026. Bezug: P09.
- Auftrag: ruhige hochwertige Gestaltung, helle Grautöne mit Blauviolett, kompakte Eingabe oberhalb der Liste, sichtbare Stift-/Papierkorb-Aktionen, Dialoge über der Liste; strikt Mobile First.
- Umsetzung: CSS neu auf mobile Basis mit min-width-Erweiterungen ausgerichtet; SVG-Icons mit zugänglichen Aktionsnamen. Native Dialoge mit serverseitigen Ansichten als Fallback, Fokusführung und unveränderten CSRF-geschützten Schreibabläufen. Keine neue Bibliothek oder JSON-API.
- Korrekturen: Fokusauswahl hatte zunächst einen ausgeblendeten Zurück-Link statt des Namensfelds getroffen; Priorisierung korrigiert. Nach Umstellung der Reihenfolge wählte eine Browserassertion die Option Dringend statt des Aufgaben-Badges; Prüfung auf Aufgabenliste begrenzt. Browserfälle erhalten vor jedem Lauf einen leeren isolierten Datenbestand.
- Ergebnis: 69 Java-Prüfungen, Spotless, Enforcer und SpotBugs bestanden. 15 Browserfälle in Chromium 375/320/1280 und WebKit 375/1280 bestanden, einschließlich axe, Fokus, Escape, Abbrechen, Rückgängig, JavaScript-freiem Fallback und Ladefehler.
- Sichtprüfung: Mobile Liste und Dialoge einschließlich 320 px, Desktopliste und lange Namen geprüft. Keine horizontalen Überläufe. Touch-Kontexte sind emuliert, keine Prüfung auf physischen Mobilgeräten.
- Abschluss: Dokumentation aktualisiert. Live-Vorschau mit finaler JAR auf dem lokalen Standardport gestartet und HTTP 200 bestätigt. Normale Projektdaten erhalten. P09 abgeschlossen; GitHub-Abgabe weiterhin P07. Kein Commit oder Push.

## LOG-024 — Symbolausrichtung korrigiert

- Datum: 24.09.2026.
- Änderung: Pluszeichen bei Neue Aufgabe durch ein geometrisch zentriertes lokales SVG ersetzt. Prioritätsauswahl erhält einen einheitlichen SVG-Pfeil mit 14 px rechtem Abstand und ausreichendem Text-Innenabstand. Native Auswahlbedienung bleibt erhalten; im Forced-Colors-Modus wird der Systempfeil verwendet.
- Prüfung: Maven Verify erfolgreich, 15 Browserfälle bestanden. Mobile und Desktop-Screenshots visuell geprüft. Live-Vorschau aktualisiert; keine Datenänderung, kein Commit oder Push.

## LOG-025 — GitHub-Abgabe vorbereitet

- Datum: 24.09.2026. Bezug: P07.
- Ziel: privates Repository ucarsinan/task-management. Vorhandene Git-Identität verwenden; ein initialer Commit enthält den zusammenhängenden, lokal geprüften Projektstand.
- Prüfung: Abgabedateien, Ausschlüsse, interne Dokumentlinks und unerwünschte Referenzen vor Commit prüfen. Build-/Testartefakte und lokale Daten bleiben ausgeschlossen.
- Nächster Nachweis: echter frischer Git-Checkout, Push und GitHub-Actions-Lauf. Diese Schritte sind zu diesem Protokollzeitpunkt noch nicht abgeschlossen.
