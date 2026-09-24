# Qualitätsstrategie und Abnahme

Status: P01–P09 abgeschlossen. Fachliche Abläufe, statische Analyse, Browser-/axe-Abnahme, Abhängigkeiten und Start-/Persistenzprüfung bestanden. Frischer GitHub-Checkout und GitHub-CI erfolgreich geprüft.

## Prüfungen nach Risiko

| Ebene | Was geprüft wird | Werkzeug / Nachweis |
|---|---|---|
| Domain/Service | Namen, unveränderliche Felder, Uhrzeit, expliziter Statuswechsel, unbekannte IDs | JUnit; feste Clock; fokussierte Tests ohne Webserver |
| Web | Formularbindung, Statuscodes, Redirects, Fehlermeldungen, CSRF | Spring MVC Test / MockMvc mit echter Security-Filterkette |
| Persistenz | Migrationen, Enum-Mapping, Sortierung, Änderungen, Löschen, Zeitpräzision | PostgreSQL über Testcontainers; keine H2-Ersatzdatenbank |
| Architektur | Kein Web→Repository-Zugriff; keine HTTP-Abhängigkeiten in Application/Domain; keine Paketzyklen | Kleiner ArchUnit-Test |
| Zusammenspiel | Browseranfrage bis Datenbank für die Kernabläufe | Integrationstest mit Spring-Kontext und PostgreSQL |
| Oberfläche | Bedienbarkeit, lange Namen, Fehler-/Leerzustand, schmale/breite Ansicht | Tatsächliche Browserprüfung mit dokumentiertem Ergebnis |
| Auslieferung | Frischer Checkout, Maven-Build, JAR-Start, persistente Daten nach Neustart | Reproduzierbarer manueller Start-/Abnahmedurchlauf |

Tests prüfen Verhalten und Grenzen, nicht jede Getter-Methode oder die interne Aufrufreihenfolge. Kein willkürlicher Coverage-Prozentsatz. Mockito nur an sinnvollen Grenzen; Datenbankverhalten wird gegen PostgreSQL geprüft.

## Anforderungsabdeckung

| Anforderungen | Nachweise |
|---|---|
| R01–R02 | P01: Generatorparameter, Java-Version, effektive Build-Konfiguration, startbare JAR |
| R03 | P03–P06: MockMvc plus Browserabnahme |
| R04 | P02/P03/P06: PostgreSQL-Integration und Neustartprüfung |
| R05–R06 | P02/P03: Domain-/Mapping-Tests und ungültige Priorität |
| R07 | P03: Erstellen mit gültigen und ungültigen Eingaben |
| R08 | P04: Name/Status ändern, andere Felder unverändert |
| R09 | P05: Löschen und Verhalten unbekannter ID |
| R10 | P06: README-Startweg mit gebauter JAR |
| R11 | P07: vollständiger geprüfter Abgabestand |

## Wichtige Testfälle

- Name leer, nur Whitespace, Rand-Whitespace, 200 und 201 Java-Zeicheneinheiten; zusätzlich nicht-BMP-Zeichen zur Grenzprüfung.
- Alle drei Prioritäten sowie fehlender und manipulierter Enum-Wert.
- Anlegen setzt done=false und created aus der injizierten Clock.
- Umbenennen ändert weder done noch created noch priority.
- Explizites Setzen von done ist wiederholbar und ändert weder Name noch created noch priority.
- Unbekannte IDs, fehlerhaftes ID-Format und wiederholtes Löschen.
- Erfolgreiches POST liefert 303; GET verändert niemals Daten.
- Fehlendes CSRF-Token verhindert jede Mutation.
- HTML im Namen wird als Text angezeigt; ungültige Eingaben bleiben im Formular erhalten.
- Gleiches created bei mehreren Tasks ergibt eine stabile Sortierung nach ID.
- Anwendungsneustart erhält Daten; Stop/Start der Datenbank ohne Volume-Löschung ebenfalls.

## Build-Vertrag

`./mvnw verify` führt die Unit-, Architektur- und Integrationstests aus. Surefire übernimmt `*Test`, Failsafe `*IT`; die Plugin-Konfiguration muss sicherstellen, dass Integrationstests tatsächlich gefunden werden. Eine verfügbare Docker-kompatible Laufzeit ist Voraussetzung für PostgreSQL-Testcontainers.

Ein grüner Lauf mit übersprungenen Integrationstests gilt nicht als Gesamtfreigabe. Konkrete Testzahlen, relevante Befehle, Umgebung und Ergebnis werden bei Ausführung protokolliert. Dieser Build-Vertrag wurde für die technische Grundlage mit insgesamt 67 erfolgreichen Prüfungen einschließlich P05 nachgewiesen. Zusätzliche Abnahmen stehen unter Nachweis P06.

## Definition of Done

- [x] Betroffene Anforderungen und Verträge sind erfüllt; keine undokumentierten Scope-Erweiterungen.
- [x] Gezielte Tests bestehen; für die Gesamtabgabe besteht `./mvnw verify` ohne übersprungene Pflichtprüfungen.
- [x] Migrationen funktionieren auf einer leeren PostgreSQL-Datenbank; Hibernate validiert das Schema.
- [x] Relevante Paketgrenzen sind geprüft.
- [x] UI-Änderungen sind bei 375 px und 1280 px, mit Tastatur und langen Namen visuell geprüft.
- [x] Fehlerfälle sind verständlich; keine technischen Interna oder Zugangsdaten im UI.
- [x] README entspricht dem tatsächlich getesteten Build-/Startweg.
- [x] Arbeitsprotokoll, Aufgabenstatus und betroffene Verträge sind aktuell.
- [x] Diff enthält keine sachfremden Änderungen oder unerwünschten Dateien.

## Abnahmeszenario

Aus frischem Checkout gemäß README bauen, Datenbank und JAR starten, Aufgabe mit Priorität anlegen, Namen ändern, erledigen und wieder öffnen. Anwendung neu starten: Werte bleiben erhalten. Datenbank ohne Volume-Löschung neu starten: Daten bleiben erhalten. Task löschen und erneut laden: Task fehlt. Anschließend ungültigen Namen und schmale Ansicht prüfen. Ergebnisse tatsächlich erfassen; keine Abnahme allein anhand des Codes behaupten.

## Verbindliche zusätzliche Qualitätsmaßnahmen

Die Qualitätsanforderung umfasst den gesamten Entwicklungsweg. Die Prüfungen richten sich nach den tatsächlichen Risiken und werden nicht allein durch eine hohe Testanzahl ersetzt.

| Maßnahme | Zeitpunkt | Bestehenskriterium |
|---|---|---|
| Java-/Maven-Version erzwingen | P01–P02, jeder Verify-Lauf | Java 17 und Maven 3.9.x; Build bricht bei Abweichung ab |
| Einheitliches Format und ungenutzte Imports | P01–P02, jeder Verify-Lauf | Spotless check erfolgreich; Formatänderungen gezielt mit spotless:apply |
| Paketgrenzen und Zyklen | P02, bei neuen Schichten erweitern | ArchUnit erfolgreich; keine leeren Scheintests für noch fehlende Schichten |
| Transaktions-Rollback | P02 | Eine abgebrochene Mutation hinterlässt keine Teiländerung |
| Sicherheit und Eingabegrenzen | P03–P05 | CSRF, Escaping, manipulierte IDs/Formulare und unveränderliche Felder geprüft |
| Statische Fehleranalyse | P06 | SpotBugs in den Verify-Lauf integrieren; relevante Funde beheben oder einzeln begründen |
| Barrierefreiheit | P06 | Automatisierter axe-Check plus manuelle Tastatur-/Fokusprüfung; keine offenen schweren Befunde |
| Browser-End-to-End | P06 | Kernabläufe gegen echte Anwendung und PostgreSQL automatisiert prüfen; responsive Sichtprüfung ergänzen |
| Abhängigkeitsprüfung | P06 und vor Abgabe | Produktionsabhängigkeiten gegen aktuelle Advisories prüfen; relevante Befunde und Ausnahmen dokumentieren |
| CI | P06 vorbereiten, P07 live prüfen | Java-17-Build mit Verify und isoliertem PostgreSQL läuft im Zielrepository; Mindestberechtigungen |
| Auslieferung und Persistenz | K1/P06 | JAR-Start, Migration und Neustart geprüft; vollständiger frischer Checkout bei P06 |
| Laufzeitverhalten | P06 | Repräsentative lokale Aufgabenmenge und Fehlerfall prüfen; gemessene Umgebung dokumentieren |

Große Lasttests, Mutation Testing und ein externer Penetrationstest sind ohne zusätzliche Anforderungen keine pauschalen Pflichtprüfungen für diese lokale Anwendung. Sicherheit im öffentlichen Betrieb wäre ein eigener Scope. Jede nicht durchgeführte Prüfung bleibt als solche markiert.

## Nachweis P03

Service-Tests verwenden eine feste Clock und prüfen unveränderliche Ergebnisdaten. Web-Integrationstests laufen mit vollständiger Security-Filterkette und echter PostgreSQL-Instanz. Sie prüfen gültiges Anlegen, Eingabegrenzen, Pflichtpriorität, CSRF, geschützte Serverfelder und Escaping. jsoup 1.21.2 wird ausschließlich in Tests zum Lesen von gerendertem HTML verwendet.

Die manuelle Browserprüfung erfolgte gegen die gebaute JAR mit einer separaten temporären PostgreSQL-Instanz: Leerzustand, Anlegen, Eingabefehler, 200-Zeichen-Name, HTML als Text, Neuladen und Tastaturbedienung. Bei 375 und 1280 px entsprach die Dokumentbreite der Fensterbreite. Zugeordnete Fehlermeldungen hatten gültige aria-describedby-Verweise. Das ersetzt keinen vollständigen axe- oder WCAG-Audit; diese Gesamtabnahme bleibt P06 zugeordnet.

Technische Referenzen für P03: [Spring Security: CSRF](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html) für den aktiv bleibenden Formularschutz und [Spring MVC: InitBinder](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-initbinder.html) für erlaubte Bindefelder. Die konkrete Boot-3-Konfiguration wurde durch die Tests verifiziert.

## Nachweis P04

Service- und Webtests prüfen Umbenennen, explizites Setzen des Status, wiederholbare Statuswerte, unveränderte übrige Felder, unbekannte und ungültige IDs, CSRF und manipulierte Zusatzfelder. Fehlende, mehrfache und ungültige Statuswerte werden mit 400 abgewiesen. Der vollständige Verify-Lauf umfasst 63 erfolgreiche Prüfungen ohne Fehler oder übersprungene Tests.

Im Browser wurden Umbenennen, ein ungültiger Name, Speichern per Tab/Enter, Abbrechen, Erledigen und Wiederöffnen gegen die gebaute JAR geprüft. Direkte PostgreSQL-Abfragen bestätigen den jeweiligen Status und unveränderte ID, Priorität und Erstellungszeit. Liste und Bearbeitungsformular wurden bei 375/1280 px visuell geprüft; kein horizontaler Überlauf. Die temporäre Prüfumgebung wurde anschließend beendet. Automatisierte Browser- und axe-Gesamtabnahme bleiben P06 zugeordnet.

## Nachweis P05

Löschen ist mit Service- und PostgreSQL-Webtests abgesichert: ausschließlich das Ziel aus der URL wird entfernt, zusätzliche Formular-IDs werden ignoriert, andere Aufgaben bleiben erhalten. Unbekannte und erneut gelöschte IDs liefern 404, fehlerhafte IDs 400; fehlende/ungültige CSRF-Token und GET-Anfragen verändern keine Daten. Die letzte gelöschte Aufgabe führt zum Leerzustand.

Im Browser wurden Löschaktion und Layout bei 375/1280 px geprüft, einschließlich Tab/Enter und erneutem Laden der Liste. Die Testdatenbank bestätigt null verbleibende Aufgaben. Der Verify-Lauf umfasst 67 erfolgreiche Prüfungen ohne Fehler oder übersprungene Tests. P06 bleibt für die zusätzlichen Qualitätsmaßnahmen und Gesamtabnahme offen.

## Nachweis P06

Am 24.09.2026 in einer frischen Quellkopie ohne target und node_modules geprüft: Maven Verify mit 67 erfolgreichen Tests, Spotless, Enforcer und SpotBugs; anschließend npm ci und drei erfolgreiche Browserläufe. Chromium bei 375/1280 px und WebKit bei 1280 px prüfen den vollständigen Aufgabenablauf, Formfehler, Tastatur, 200-Zeichen-Namen, Text-Escaping, Links und axe-Regeln. Screenshots mit langen Namen wurden zusätzlich visuell geprüft. Kein horizontaler Überlauf und keine axe-Verletzungen in den geprüften Zuständen.

Der separate Abnahmelauf bestätigt unveränderte Daten nach Anwendungs-/PostgreSQL-Neustart, Anzeige von 1.000 Aufgaben und eine neutrale HTTP-500-Antwort bei Datenbankausfall. Fünf vollständige HTTP-Abrufe der Liste dauerten lokal 252,21 / 142,52 / 135,76 / 100,41 / 83,60 ms; macOS/arm64, Java 17.0.8.1, Docker 29.5.2. Das sind einzelne lokale Messungen ohne konkurrierende Benutzer, kein Last- oder Produktionsleistungsnachweis.

78 Produktionsabhängigkeiten nach Sicherheitsupdates ohne OSV-Treffer; npm audit ohne Treffer. SpotBugs läuft ohne offene Meldungen mit zwei eng begründeten Konstruktor-Ausnahmen. Details, Werkzeuge, Quellen und Einschränkungen: [Abhängigkeiten](DEPENDENCIES.md).

CI ist vorbereitet, aber noch nicht auf GitHub ausgeführt. Da noch kein Git-Repository besteht, ist die geprüfte frische Quellkopie ausdrücklich kein Git-Checkout. Der tatsächliche Checkout und Live-CI bleiben P07 zugeordnet. Temporäre Testanwendungen und Datenbanken sind beendet.

## Nachweis der UI-Überarbeitung

69 Java-Prüfungen ohne Fehler oder übersprungene Tests; Spotless, Enforcer und SpotBugs erfolgreich. Drei vollständige Browserläufe (Chromium 1280/375 px, WebKit 1280 px) einschließlich axe ohne Befund. Zusätzliche Tests prüfen lesende Löschbestätigung, validierte Namensvorschau, fehlende Aufgaben, CSRF, Abbrechen beider Abläufe und Rückgängig beim Statuswechsel. Screenshots von Liste und Bestätigungen mit langen Namen visuell geprüft. Die lokale Vorschau wurde mit der neuen JAR neu gestartet.

## Nachweis P09 — Mobile First und Dialoge

69 Java-Prüfungen ohne Fehler oder übersprungene Tests; Spotless, Enforcer und SpotBugs erfolgreich. 15 Browserfälle bestanden: je Aufgabenablauf mit Dialogen, JavaScript-freier Fallback und kontrollierter Dialog-Ladefehler in Chromium (375/320/1280 px) und WebKit (375/1280 px). Jeder Fall beginnt mit leerem Bestand der ausschließlich vom Prüfskript gestarteten Datenbank.

Mobile Kontexte emulieren Touch-Bedienung; echte Geräte wurden nicht geprüft. Screenshots von Aufgabenliste, langen Namen und Dialogen visuell kontrolliert. Kein horizontaler Überlauf bei 320/375/1280 px. axe ohne Befund in den geprüften Zuständen; weiterhin keine vollständige WCAG-Zertifizierung. Fokus im Namensfeld bzw. auf Abbrechen, Escape, Rückgabe des Fokus, Nicht-Speichern beim Abbrechen und Rückgängig getestet. Bei Dialog-Ladefehler bleibt die Aufgabe unverändert und der Dialog schließbar.

CSS verwendet mobile Basisregeln und ausschließlich min-width-Layout-Erweiterungen. Mobile Eingaben haben 16 px Schriftgröße; Icon- und Statusaktionen mindestens 44 × 44 px Trefferfläche. Live-Vorschau mit finaler JAR neu gestartet; Erreichbarkeit mit HTTP 200 bestätigt.

## Nachweis P07 — GitHub-Abgabe

Das private Repository [ucarsinan/task-management](https://github.com/ucarsinan/task-management) enthält den geprüften Anwendungsstand `1bc14d3`. Ein tatsächlicher frischer GitHub-Checkout besteht Maven Verify mit 69 Java-Prüfungen ohne Fehler oder übersprungene Tests, Spotless, Enforcer, SpotBugs, npm ci, 15 Browserfälle und die Neustart-/Persistenzabnahme. Der Checkout blieb nach den Prüfungen unverändert.

Der [Quality-Lauf 36040710444](https://github.com/ucarsinan/task-management/actions/runs/36040710444) für diesen Commit ist erfolgreich abgeschlossen: Build, statische Analyse, Abhängigkeitsprüfungen, Browser-/axe-Tests und Persistenzabnahme. Der Zugriff ist privat im Eigentümerkonto; eine Einladung weiterer Empfänger wurde nicht beauftragt. R11 ist für die vereinbarte Bereitstellung im privaten Repository erfüllt.
