# ADR 0002: Initializr-Gerüst auf Spring Boot 3 ausrichten

Datum: 24.09.2026. Status: angenommen im Rahmen der Umsetzung von P01.

## Befund

Die öffentliche Initializr-Metadatenantwort bietet nur Spring Boot 4.x an. Ein direkter Generatoraufruf mit 3.5.16 wurde mit HTTP 400 abgelehnt. Das Parent-POM für Spring Boot 3.5.16 ist im Maven Central Repository verfügbar.

## Entscheidung

Das offizielle Initializr-Gerüst mit Version 4.0.8, Java 17, Maven und JAR verwenden und vor dem ersten Build auf 3.5.16 ausrichten. Boot-4-spezifische Starter werden durch ihre Boot-3-Entsprechungen ersetzt. Flyway nutzt flyway-core und flyway-database-postgresql. Testcontainers verwendet die durch Boot 3 verwalteten Artefakte junit-jupiter und postgresql. Unnötige Generator-Beispieltests und leere POM-Metadaten werden nicht übernommen.

Generator: https://start.spring.io/starter.zip

Parameter: type=maven-project, language=java, bootVersion=4.0.8, baseDir=task-management, groupId=de.sinanucar, artifactId=task-management, name=TaskManagement, packageName=de.sinanucar.taskmanagement, packaging=jar, javaVersion=17, dependencies=web,data-jpa,validation,thymeleaf,flyway,postgresql,testcontainers.

Der generierte Maven Wrapper verwendet Maven 3.9.16. Die Anwendung trägt Version 0.1.0. Kein Snapshot wird als Framework eingesetzt.

## Begründung und Prüfung

Dieser Anpassungsweg erhält sowohl die Initializr-Herkunft als auch die verbindliche Framework-Major-Version 3. Ein Wechsel zu Boot 4 würde die Aufgabenstellung verletzen. Der Eingriff betrifft Build-Metadaten; die Architektur bleibt unverändert. Eine spätere Wiederholung muss erneut die tatsächlich verfügbaren Versionen prüfen.

Der Build und die PostgreSQL-Integration werden ausdrücklich verifiziert; der erfolgreiche Generator-Download allein ist kein Kompatibilitätsnachweis.

Quelle: [Spring Boot Parent 3.5.16 in Maven Central](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-parent/3.5.16/).
