# Entwicklungsregeln

Status: geltende Arbeitsregeln, angenommen am 24.09.2026.

## Vor jeder Änderung

1. README, betroffene Anforderungen und Verträge sowie den aktuellen Aufgabenstatus lesen.
2. Vorhandenen Code und uncommittete Änderungen prüfen. Fremde Änderungen weder überschreiben noch zurücksetzen.
3. Eine Aufgabe aus `tasks/todo.md` auswählen; Ziel und Abnahmekriterien müssen klar sein.
4. Abweichungen vom Umfang oder von Architekturgrenzen vor Umsetzung begründen und als Entscheidung dokumentieren.

## Umsetzung

- Eine fachlich zusammenhängende Änderung pro Arbeitsschritt; keine sachfremden Refactorings.
- Constructor Injection, konkrete Typen, aussagekräftige Namen. Kein Field Injection und kein Lombok in der Ausgangsarchitektur.
- Controller bleiben dünn; fachliche Mutationen laufen über TaskService und kontrollierte Domain-Methoden.
- Kein direktes Entity-Binding, keine frei setzbaren IDs oder Erstellungszeitpunkte.
- Keine allgemeinen BaseController, BaseService oder Hilfsklassen ohne konkreten Bedarf.
- Wiederverwendung erst bei tatsächlicher Wiederholung; keine vorsorglichen Erweiterungspunkte.
- Keine zusätzlichen Runtime-Dependencies ohne belegten Nutzen. Versionen nicht ungeprüft überschreiben.
- Migrationen ergänzen, bereits verwendete Migrationen nicht umschreiben.
- Fehler nicht verschlucken, keine pauschalen catch-Blöcke mit scheinbarem Erfolg.
- Java-Bezeichner und Commit-Texte auf Englisch; Projektdokumentation und Oberfläche auf Deutsch.
- Kommentare erklären nicht offensichtliche Gründe oder Grenzen. Keine Prozessdialoge oder unbelegten Qualitätsbehauptungen im Code.

## Dokumentationshoheit

| Information | Maßgebliches Dokument |
|---|---|
| Aufgabenstellung und Scope | docs/REQUIREMENTS.md |
| Komponenten und Abhängigkeiten | docs/ARCHITECTURE.md |
| Verhalten und Datenregeln | docs/CONTRACTS.md |
| Gründe für Architekturentscheidungen | docs/decisions/ |
| Qualitätskriterien | docs/QUALITY.md |
| Reihenfolge und Risiken | tasks/plan.md |
| Aktueller Aufgabenstatus | tasks/todo.md |
| Tatsächlicher Verlauf | docs/WORK_LOG.md |
| Geprüfte Bedien-/Startanleitung | README.md |

Widersprüche werden ausdrücklich aufgelöst, nicht durch zusätzliche konkurrierende Dokumente überdeckt. Ursprüngliche Aufgabenanforderungen haben Vorrang vor Architekturvorschlägen. Unvermeidbare Änderungen erhalten eine neue ADR; alte Entscheidungen werden als ersetzt markiert und verlinkt.

## Protokollierung

Jeder wesentliche Schritt erhält im Arbeitsprotokoll einen Eintrag: ID, Datum, betroffene Aufgabe, Aktion, betroffene Dateien oder Bereiche, Prüfungen mit Ergebnis und verbleibende Punkte. Dazu zählen Analyse, Entscheidungen, Implementierung, fehlgeschlagene Prüfungen, Korrekturen und Veröffentlichungen.

Kein Shell-Transkript, keine privaten Gesprächsinhalte, keine Zugangsdaten und keine vollständigen Eingabedaten protokollieren. Das Protokoll dokumentiert technische Arbeit und überprüfbare Resultate. Nicht ausgeführte Prüfungen heißen ausdrücklich „nicht ausgeführt“, nicht „bestanden“. Frühere Einträge bleiben erhalten; Korrekturen werden als neuer Eintrag ergänzt.

## Abschluss eines Arbeitsschritts

1. Relevante Tests ausführen; Ergebnisse und Blockaden festhalten.
2. Bei Oberfläche zusätzlich im Browser prüfen.
3. Diff auf fachfremde Änderungen, lokale Pfade, Zugangsdaten und unerwünschte Artefakte prüfen.
4. Betroffene Dokumentation und Aufgabenstatus aktualisieren.
5. Arbeitsprotokoll ergänzen. Eine Aufgabe erst bei erfüllten Abnahmekriterien als erledigt markieren.

## Git und Abgabe

Commits beschreiben die konkrete Änderung und bleiben fachlich zusammenhängend. Keine erfundenen Autorendaten oder nicht stattgefundenen Arbeitsschritte. Branch-Schutz, fremde Änderungen und bestehende Historie respektieren; kein Force-Push als Standardlösung.

Das Repository enthält Quellcode, Build-Dateien, Migrationen, Tests und Projektdokumentation. Nicht hinein gehören IDE-Metadaten, Build-Ausgaben, Datenbankvolumes, private Originalunterlagen, lokale Prüfartefakte oder echte Zugangsdaten. Eine `.gitignore` wird bei der Projektanlage ergänzt.

GitHub-Ziel und Sichtbarkeit müssen vor Veröffentlichung feststehen. Ein lokaler Commit oder Push allein beweist weder einen erfolgreichen Build noch eine funktionierende Anwendung. Abgabezustand und Prüfstand werden getrennt protokolliert.
