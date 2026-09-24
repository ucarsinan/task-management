# ADR 0001: Ein Java-Projekt mit serverseitiger Oberfläche

Datum: 24.09.2026

Status: angenommen am 24.09.2026. Technische Grundlage P00–P02 implementiert und geprüft.

## Kontext

Die Aufgabe fordert Java 17, Maven, JAR, Spring Boot 3 oder Quarkus 3, PostgreSQL und eine einfache Browseroberfläche. Der Kern umfasst das Anlegen, Umbenennen, Erledigen und Löschen von Tasks. Die Anwendung soll lokal reproduzierbar startbar und im Interview erklärbar sein.

## Betrachtete Varianten

| Variante | Vorteil | Nachteil | Bewertung |
|---|---|---|---|
| Spring Boot 3 + MVC + Thymeleaf | Ein Build, ein Prozess, direkte Formularabläufe | Vollständige Seitenwechsel; keine SPA-Interaktionen | Empfehlung für den vorliegenden Umfang |
| Spring Boot 3 + REST + separates SPA-Frontend | Frontend unabhängig erweiterbar; clientseitige Interaktionen | Zweite Toolchain, zusätzliche Schnittstellen und Fehlerpfade | Möglich, aber hier ohne fachliche Notwendigkeit |
| Quarkus 3 + Qute | Ebenfalls als kompakte Java-Webanwendung umsetzbar | Anderes Framework; kein durch die Aufgabe belegter Vorteil | Zulässige Alternative, falls Framework-Erfahrung dafür spricht |

## Entscheidung

Spring Boot 3 mit Java 17, Maven Wrapper, Spring MVC, Thymeleaf, Bean Validation, Spring Data JPA, PostgreSQL und Flyway. Deutsche Oberfläche. Servergerendertes HTML und normale Formulare genügen. Spring Security schützt mutierende Formulare mit CSRF-Tokens, ohne Anmeldung oder Benutzerverwaltung einzuführen.

Ein Service koordiniert die Use-Cases. Das Domänenmodell trägt JPA-Annotationen. Separate Formularmodelle und Ergebnisdaten verhindern die Bindung von Benutzereingaben an beliebige Entity-Felder. Paketgrenzen werden gezielt durch einen Architekturtest geprüft.

## Folgen und Kompromisse

- Build und Auslieferung umfassen eine JAR; kein Node-Laufzeitsystem erforderlich.
- Geschäftslogik bleibt außerhalb der Controller und Templates.
- Frameworkabhängige JPA-Annotationen in der Domain werden zugunsten eines kleinen Modells akzeptiert.
- Die Oberfläche benötigt für Kernabläufe kein JavaScript.
- CSRF-Schutz ersetzt keine Authentifizierung; Betrieb zunächst nur lokal.
- Neue Anforderungen wie Mehrbenutzerbetrieb, externe API oder umfangreiche Interaktionen erfordern eine neue Entscheidung.

## Versionsstrategie

Die Aufgabenstellung hat Vorrang vor Generator-Defaults: Java 17 und Spring Boot 3 bleiben verbindlich. Bei Projektanlage eine stabile, verfügbare 3.x-Version prüfen; niemals stillschweigend auf Version 4, Snapshot oder neueres Java wechseln. Initializr bietet inzwischen keine geeignete 3.x-Version mehr an. Der verwendete Anpassungsweg ist in [ADR 0002](0002-initializr-compatibility.md) dokumentiert und durch Build und Integrationstests geprüft.

Maven und PostgreSQL-Image werden festgeschrieben; kein `latest`. Spring-verwaltete Dependencies folgen dem Boot-Dependency-Management, sofern kein begründeter Ausnahmefall vorliegt. Konkrete Versions- und Generatornachweise stehen in ADR 0002 und im Arbeitsprotokoll.

## Quellen

Am 24.09.2026 für die Architekturplanung gelesen:

- [Spring Boot 3.5: System Requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html): Java-17-Grundlage und Maven-Unterstützung der 3.x-Linie.
- [Spring: Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/): Controller, Thymeleaf und Initializr als Grundlage für servergerendertes HTML. Beispiel-Defaults ersetzen nicht die Versionsvorgaben dieser Aufgabe.
- [Spring Boot 3.5: Database Initialization](https://docs.spring.io/spring-boot/3.5/how-to/data-initialization.html): versionierte Schema-Migration und eindeutige Zuständigkeit für Initialisierung.

Die Quellenprüfung ersetzt keinen Build oder Kompatibilitätstest des späteren Projekts.
