# Fachliche und technische Verträge

Status: angenommen am 24.09.2026. Diese Verträge gelten für die Implementierung; Änderungen werden über eine dokumentierte Entscheidung und passende Tests nachvollzogen.

## Task

| Feld | Java / PostgreSQL | Regel |
|---|---|---|
| id | Long / bigint identity, primary key | Serverseitig vergeben; unveränderlich |
| name | String / varchar(200), not null | Mit String.strip() normalisieren; danach 1–200 Java-Zeicheneinheiten |
| done | boolean / boolean, not null | Beim Anlegen false |
| created | Instant / timestamp with time zone, not null | Serverseitig über Clock setzen; später unverändert |
| priority | Priority / varchar(6), not null | LOW, NORMAL oder URGENT; als Enum-Name speichern |

Die Datenbank ergänzt einen CHECK für erlaubte Prioritäten und einen CHECK gegen leere beziehungsweise nur aus gewöhnlichen Leerzeichen bestehende Namen. Das Entfernen von Rand-Whitespace per String.strip() und die Längenprüfung liegen in der Anwendung. Das von PostgreSQL nicht speicherbare Nullzeichen U+0000 wird als ungültige Eingabe abgelehnt. Keine Behauptung, dass SQL-Längen- und Java-Zeichenmessung identisch sind.

created wird als Zeitpunkt gespeichert und in der Oberfläche ausdrücklich in UTC angezeigt. Datenbank-Roundtrip-Tests berücksichtigen die Mikrosekundenpräzision von PostgreSQL; Nanosekundengleichheit ist kein Abnahmekriterium.

## Use-Cases

| Operation | Eingabe | Verhalten |
|---|---|---|
| listTasks | keine | Alle Tasks nach created DESC, id DESC als unveränderliche View-Daten liefern |
| createTask | name, priority | Name normalisieren und prüfen; priority nicht null; ID/created/done serverseitig setzen |
| renameTask | id, name | Bestehenden Task laden; ausschließlich Namen ändern |
| setTaskDone | id, done | Gewünschten booleschen Zustand ausdrücklich setzen; kein blindes Toggle |
| deleteTask | id | Vorhandenen Task dauerhaft löschen |

Das Anlegeformular wählt NORMAL vor. Fehlende oder ungültige priority bei einer manipulierten Anfrage ist ein Eingabefehler, kein stiller Fallback. Die Namensregeln gelten auch bei direktem Service-Aufruf. created, priority und id bleiben bei Umbenennung oder Statusänderung unverändert.

## HTTP- und Formularverhalten

| Methode / Pfad | Zweck | Erfolg |
|---|---|---|
| GET / | Einstieg | Redirect nach /tasks |
| GET /tasks | Liste und Anlegeformular | 200 mit HTML |
| POST /tasks | Anlegen | 303 nach /tasks |
| GET /tasks/{id}/edit | Umbenennungsformular | 200 mit HTML |
| POST /tasks/{id}/name/preview | Gültigen neuen Namen vor Speicherung gegenüberstellen | 200 mit HTML |
| POST /tasks/{id}/name | Umbenennen nach Bestätigung | 303 nach /tasks |
| POST /tasks/{id}/done | Expliziten Status setzen | 303 nach /tasks |
| GET /tasks/{id}/delete/confirm | Löschbestätigung anzeigen, keine Mutation | 200 mit HTML |
| POST /tasks/{id}/delete | Bestätigte Löschung | 303 nach /tasks |

Nur POST verändert Daten. Jedes mutierende Formular enthält ein geprüftes CSRF-Token. Kein CORS-Freischalten erforderlich. Eine serverseitige Sicherheitskonfiguration erlaubt die vorgesehenen anonymen lokalen Seitenaufrufe und aktiviert keine Login-Seite.

## Fehlerverhalten

- Ungültiger Name oder Priorität: Status 400, eingegebene Werte erhalten, konkrete Feldmeldung; keine Speicherung. Beim Anlegeformular wird die Aufgabenliste erneut bereitgestellt.
- Statusänderung akzeptiert genau einen Parameter done mit dem Wert true oder false. Fehlende, mehrfache oder sonstige Werte: 400; nicht stillschweigend false setzen.
- Fehlerhaftes ID-Format: 400. Wohlgeformte, aber nicht vorhandene ID: 404, auch bei erneutem Löschen.
- Fehlendes oder ungültiges CSRF-Token: 403; keine Mutation.
- Unerwarteter Laufzeit-/Datenbankfehler: Transaktion zurückrollen; neutrale Fehlerseite mit 500. Keine Erfolgsmeldung und keine technischen Interna im Browser.
- Unverfügbare Datenbank beim Start: Start schlägt sichtbar fehl; kein Ersatz durch In-Memory-Speicherung.

Fehlermeldungen erscheinen auf Deutsch. Benutzereingaben werden in Templates escaped; kein ungefiltertes HTML. Laufzeitlogs enthalten keine Zugangsdaten oder vollständigen Formulareingaben.

## Oberfläche

Eine Aufgabenliste mit verständlichem Leerzustand und Anlegeformular. Jede Aufgabe zeigt Namen, Erledigt-Status, Priorität und Erstellungszeit. Benannte Aktionen ermöglichen Umbenennen, Statuswechsel und Löschen. Statusinformationen sind nicht ausschließlich farblich erkennbar.

Formularfelder haben sichtbare Labels, Fehler sind zugeordnet, Fokus ist sichtbar, und alle Kernaktionen sind per Tastatur bedienbar. Bei 375 px und 1280 px Breite darf kein horizontaler Seiten-Overflow auftreten; lange Namen müssen umbrechen. Löschaktionen sind visuell eindeutig getrennt von Statusaktionen. Vor einer Löschung erscheint ein Dialog über der Liste mit Aufgabenname, Hinweis auf dauerhafte Entfernung, Abbrechen und Endgültig löschen. Umbenennen erfolgt ebenfalls im Dialog und zeigt vor dem finalen POST bisherigen und neuen Namen. Abbrechen speichert keine Änderung. Die Vorschau validiert den Namen und benötigt CSRF; die finalen POST-Routen validieren unabhängig davon erneut. Die Bestätigungen sind Bedienführung, keine zusätzliche Autorisierungsschicht.

Statuswechsel erfolgen direkt und zeigen auf der Zielseite eine Rückgängig-Aktion für den vorherigen Zustand. Die Aktion sendet einen expliziten booleschen Wert per CSRF-geschütztem POST. Das Rückgängig-Formular sendet zusätzlich undo=true. Nach erfolgreichem Rückgängig wird kein neuer Statushinweis erzeugt; die Aufgabe bleibt mit wiederhergestelltem Status an ihrer bestehenden Listenposition. Die Meldung wird als einmaliges Flash-Attribut geführt und ist keine dauerhafte Änderungshistorie. Im lokalen Einzelbenutzerbetrieb setzt Rückgängig den vorherigen Zustand; konkurrierende Änderungen mehrerer Benutzer sind nicht Teil des Vertrags.

## Betriebsgrenze

Anwendung und veröffentlichter Datenbankport werden lokal an Loopback gebunden. Die Datenbank verwendet ein benanntes Volume. Normaler Stop/Start darf das Volume nicht entfernen. Destruktive Reset-Befehle gehören nicht in den normalen Startablauf. Öffentliche Bereitstellung und Mehrbenutzerbetrieb sind nicht durch diese Verträge abgedeckt.

## Mobile First und progressive Dialoge

Die Basisstile gelten für schmale Bildschirme ab 320 px. Layout-Erweiterungen verwenden ausschließlich min-width-Breakpoints: ab 375 px stehen Priorität und Anlegen nebeneinander, ab 701 px nutzt die Eingabe eine Zeile. Mobile Eingaben verwenden 16 px Schrift; Icon-Aktionen und Statuskreis haben mindestens 44 × 44 px große Trefferflächen. Eingabe steht im Dokument vor der Liste; visuelle Reihenfolge und Tastaturreihenfolge bleiben nachvollziehbar.

Native dialog-Elemente halten die Liste im Hintergrund inaktiv. Beim Öffnen liegt der Fokus im Namensfeld beziehungsweise auf Abbrechen bei einer Löschung; Escape und Schließen geben den Fokus an die auslösende Aktion zurück. Abbrechen speichert nichts. SVG-Icons sind dekorativ, Aktionsnamen stehen als aria-label bereit; Tooltips ergänzen sichtbare Fokus-/Hover-Rückmeldung.

Ein kleines gleichursprüngliches Skript lädt die vorhandenen serverseitigen Ansichten in den Dialog und sendet ausschließlich die Namensvorschau per Fetch. Die finalen Mutationen bleiben reguläre CSRF-geschützte POST-Formulare. Übernommene Formular-IDs werden umbenannt, zugehörige Labels und Fehlerreferenzen angepasst. Ohne JavaScript oder dialog-Unterstützung folgen Links den eigenständigen Seiten. Ladefehler zeigen eine neutrale Meldung mit Schließmöglichkeit. Es gibt keine neue JSON-API.
