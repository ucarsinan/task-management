# Abhängigkeiten und Prüfausnahmen

## Sicherheitsupdates vom 24.09.2026

Die aufgelösten Produktionsabhängigkeiten werden über Maven ermittelt und mit der OSV-API abgeglichen. Eine Abfrage ohne Ergebnisse oder mit unvollständiger Antwort gilt nicht als bestanden. Netzwerkfehler brechen den Prüflauf ab. Die Prüfung übermittelt ausschließlich öffentliche Paketnamen und Versionen.

| Familie | Vorher | Verwendet | Anlass |
|---|---|---|---|
| Log4j API | 2.24.3 | 2.25.5 | GHSA-qv9r-c865-cp47 |
| Jackson BOM | 2.21.4 | 2.21.5 | GHSA-5gvw-p9qm-jgwh, GHSA-5jmj-h7xm-6q6v, GHSA-mhm7-754m-9p8w |
| Tomcat | 10.1.55 | 10.1.60 | GHSA-9xv2-5v5q-p794, GHSA-gcx9-497g-6cp6, GHSA-h3x4-894j-xpx5 und weitere Herstellerkorrekturen |
| PostgreSQL JDBC | 42.7.11 | 42.7.12 | GHSA-j92g-9f8w-j867 |

Die Properties in `pom.xml` überschreiben gezielt die Boot-Verwaltung innerhalb der bestehenden Bibliotheksfamilien. Spring Boot bleibt 3.5.16 und Java bleibt 17. Bei einem späteren Boot-Update prüfen, ob diese Overrides entfallen können. Tomcat 10.1.58 war trotz OSV-Verweis nicht in Maven Central verfügbar; 10.1.60 ist veröffentlicht und enthält zusätzliche Sicherheitskorrekturen.

Nach den Updates: 78 aufgelöste Produktionsartefakte ohne OSV-Treffer; npm audit ohne Treffer. Das ist eine zeitgebundene Datenbankabfrage und kein Nachweis der Abwesenheit unbekannter Schwachstellen. Build-Plugins und Container-/JDK-Systempakete sind nicht Bestandteil der Maven-Runtime-Abfrage. Die Bibliotheksupdates sind mit Verify und Browserabnahme geprüft.

Quellen: [OSV-API](https://google.github.io/osv.dev/api/), [Apache Tomcat Sicherheitsmeldungen](https://tomcat.apache.org/security-10.html). Einzelmeldungen sind über `https://osv.dev/vulnerability/` mit der jeweiligen GHSA-ID abrufbar.

## Statische Analyse

SpotBugs 4.10.4.1 läuft bei jedem Verify mit maximalem Aufwand und niedriger Meldeschwelle. Zwei Ausnahmen sind in `config/spotbugs-exclude.xml` auf Klasse, Konstruktor und Fehlermuster begrenzt:

- `TaskController`, `EI_EXPOSE_REP2`: Die Spring-Service-Referenz wird absichtlich als gemeinsame Bean injiziert. Sie ist kein vom HTTP-Aufrufer geliefertes veränderliches Datenobjekt.
- `Task`, `CT_CONSTRUCTOR_THROW`: JPA benötigt eine nicht finale Entity. Der validierende Konstruktor darf ungültige Werte ablehnen. Die Entity hält keine privilegierten Ressourcen und definiert keinen Finalizer. Neue Ressourcen oder Finalizer erfordern eine erneute Bewertung.

Keine pauschalen Paket- oder Kategorien-Ausschlüsse. Neue Befunde lassen den Build scheitern. Referenz: [SpotBugs-Filter](https://spotbugs.readthedocs.io/en/stable/filter.html).

## Browserwerkzeuge

Node 22 ist nur für die Qualitätsprüfungen nötig. Playwright 1.63.0 und axe-core/playwright 4.13.0 sind exakt festgeschrieben; `package-lock.json` hält die transitive Auflösung fest. Die Anwendung selbst benötigt keinen Node-Prozess und keinen Frontend-Build.

Chromium (320/375/1280 px) und WebKit (375/1280 px) werden geprüft; mobile Kontexte verwenden Touch-Emulation. Firefox startete auf dem lokalen macOS-Host nicht, weil sein Profilverzeichnis nicht geöffnet werden konnte; auch ein alternatives temporäres Verzeichnis half nicht. Firefox ist daher kein bestätigter Browser. Der zweite geprüfte Browsermotor ist WebKit.

axe prüft WCAG-A/AA-Regeln bis 2.2, soweit automatisiert erkennbar. Tastatur, Layout und lange Namen werden zusätzlich geprüft. Keine vollständige WCAG-Konformitätszertifizierung. Referenz: [Playwright Accessibility Testing](https://playwright.dev/docs/accessibility-testing).
