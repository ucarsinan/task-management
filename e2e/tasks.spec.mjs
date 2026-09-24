import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';
import { execFileSync } from 'node:child_process';

test.beforeEach(() => {
  const database = process.env.TASK_E2E_DB;
  if (!/^task-management-e2e-\d+$/.test(database || '')) throw new Error('Isolated runner database required');
  execFileSync('docker', ['exec', database, 'psql', '-U', 'task_management', '-d', 'task_management', '-c', 'TRUNCATE TABLE tasks RESTART IDENTITY']);
});

async function audit(page) {
  const results = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa']).analyze();
  expect(results.violations).toEqual([]);
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
  for (const link of await page.getByRole('link').all()) {
    const href = await link.getAttribute('href');
    if (href.startsWith('#')) {
      expect(await page.locator(href).count()).toBe(1);
    } else {
      expect((await page.request.get(new URL(href, page.url()).href)).ok()).toBe(true);
    }
  }
}

test('complete task lifecycle, keyboard, validation, links and accessibility', async ({ page }, testInfo) => {
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'Noch keine Aufgaben' })).toBeVisible();
  await audit(page);
  await page.getByRole('button', { name: 'Aufgabe anlegen', exact: true }).click();
  await expect(page.getByRole('alert')).toBeVisible();
  await expect(page.getByRole('textbox', { name: 'Name', exact: true })).toHaveAttribute('aria-invalid', 'true');
  await audit(page);
  await page.screenshot({ path: testInfo.outputPath('empty-state.png'), fullPage: true });
  const name = '<b>' + 'L'.repeat(193) + '</b>';
  await page.getByRole('textbox', { name: 'Name', exact: true }).fill(name);
  await page.getByRole('textbox', { name: 'Name', exact: true }).press('Tab');
  await expect(page.getByRole('combobox', { name: 'Priorität' })).toBeFocused();
  await page.getByRole('combobox', { name: 'Priorität' }).selectOption('URGENT');
  await page.getByRole('combobox', { name: 'Priorität' }).press('Tab');
  await expect(page.getByRole('button', { name: 'Aufgabe anlegen', exact: true })).toBeFocused();
  await page.keyboard.press('Enter');
  await expect(page.getByRole('heading', { name, exact: true })).toBeVisible();
  const created = await page.locator('time').getAttribute('datetime');
  await audit(page);
  await page.screenshot({ path: testInfo.outputPath('long-name.png'), fullPage: true });
  const dialog = page.getByRole('dialog');
  const renameLink = page.getByRole('link', { name: `Umbenennen: ${name}`, exact: true });
  await renameLink.click();
  await expect(dialog).toBeVisible();
  await expect(dialog.getByRole('textbox', { name: 'Name', exact: true })).toHaveValue(name);
  await expect(dialog.getByRole('textbox', { name: 'Name', exact: true })).toBeFocused();
  await page.keyboard.press('Escape');
  await expect(dialog).not.toBeVisible();
  await expect(renameLink).toBeFocused();
  await renameLink.click();
  await expect(dialog.getByRole('textbox', { name: 'Name', exact: true })).toHaveValue(name);
  await audit(page);
  await dialog.getByRole('textbox', { name: 'Name', exact: true }).fill(' ');
  await dialog.getByRole('button', { name: 'Änderung prüfen' }).click();
  await expect(dialog.getByRole('alert')).toBeVisible();
  await audit(page);
  await dialog.getByRole('textbox', { name: 'Name', exact: true }).fill('Geprüfte Aufgabe');
  await dialog.getByRole('textbox', { name: 'Name', exact: true }).press('Tab');
  await expect(dialog.getByRole('button', { name: 'Änderung prüfen' })).toBeFocused();
  await page.keyboard.press('Enter');
  await expect(dialog.getByRole('heading', { name: 'Änderung prüfen' })).toBeVisible();
  await audit(page);
  await page.screenshot({ path: testInfo.outputPath('name-confirmation.png'), fullPage: true });
  await dialog.getByRole('button', { name: 'Umbenennen bestätigen' }).click();
  await page.getByRole('button', { name: 'Als erledigt markieren: Geprüfte Aufgabe', exact: true }).click();
  await expect(page.getByText('Erledigt', { exact: true })).toBeVisible();
  await audit(page);
  await page.getByRole('button', { name: 'Rückgängig', exact: true }).click();
  await expect(page.getByText('Offen', { exact: true })).toBeVisible();
  await expect(page.getByRole('region', { name: 'Statusänderung' })).toHaveCount(0);
  await expect(page.getByRole('button', { name: 'Rückgängig', exact: true })).toHaveCount(0);
  await page.screenshot({ path: testInfo.outputPath('after-undo.png'), fullPage: true });
  await expect(page.locator('time')).toHaveAttribute('datetime', created);
  await expect(page.getByRole('region', { name: 'Aufgabenliste', exact: true }).getByText('Dringend', { exact: true })).toBeVisible();
  await page.getByRole('link', { name: 'Löschen: Geprüfte Aufgabe', exact: true }).click();
  await expect(dialog.getByRole('heading', { name: 'Aufgabe löschen?' })).toBeVisible();
  await expect(dialog.getByRole('link', { name: 'Abbrechen', exact: true })).toBeFocused();
  await audit(page);
  await page.screenshot({ path: testInfo.outputPath('delete-confirmation.png'), fullPage: false });
  await dialog.getByRole('link', { name: 'Abbrechen', exact: true }).click();
  await expect(dialog).not.toBeVisible();
  await expect(page.getByRole('heading', { name: 'Geprüfte Aufgabe', exact: true })).toBeVisible();
  await page.getByRole('link', { name: 'Umbenennen: Geprüfte Aufgabe', exact: true }).click();
  await dialog.getByRole('textbox', { name: 'Name', exact: true }).fill('Nicht speichern');
  await dialog.getByRole('button', { name: 'Änderung prüfen' }).click();
  await expect(dialog.getByRole('heading', { name: 'Änderung prüfen' })).toBeVisible();
  await dialog.getByRole('link', { name: 'Abbrechen', exact: true }).click();
  await expect(dialog).not.toBeVisible();
  await expect(page.getByRole('heading', { name: 'Geprüfte Aufgabe', exact: true })).toBeVisible();
  await page.getByRole('link', { name: 'Löschen: Geprüfte Aufgabe', exact: true }).click();
  await dialog.getByRole('button', { name: 'Endgültig löschen', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Noch keine Aufgaben' })).toBeVisible();
  await page.reload();
  await expect(page.getByRole('heading', { name: 'Noch keine Aufgaben' })).toBeVisible();
  const response = await page.goto('/tasks/999999/edit');
  expect(response.status()).toBe(404);
  await audit(page);
});

test('confirmation flows remain usable without JavaScript', async ({ browser, baseURL }, testInfo) => {
  const { viewport, isMobile, hasTouch } = testInfo.project.use;
  const context = await browser.newContext({ javaScriptEnabled: false, viewport, isMobile, hasTouch });
  const page = await context.newPage();
  try {
    await page.goto(`${baseURL}/tasks`);
    await page.getByRole('textbox', { name: 'Name', exact: true }).fill('Ohne JavaScript');
    await page.getByRole('button', { name: 'Aufgabe anlegen', exact: true }).click();
    await page.getByRole('link', { name: 'Umbenennen: Ohne JavaScript', exact: true }).click();
    await page.getByRole('textbox', { name: 'Name', exact: true }).fill('Fallback geprüft');
    await page.getByRole('button', { name: 'Änderung prüfen' }).click();
    await page.getByRole('button', { name: 'Umbenennen bestätigen' }).click();
    await page.getByRole('button', { name: 'Als erledigt markieren: Fallback geprüft', exact: true }).click();
    await page.getByRole('button', { name: 'Wieder öffnen: Fallback geprüft', exact: true }).click();
    await page.getByRole('button', { name: 'Rückgängig', exact: true }).click();
    await expect(page.getByRole('button', { name: 'Wieder öffnen: Fallback geprüft', exact: true })).toBeVisible();
    await expect(page.getByRole('region', { name: 'Statusänderung' })).toHaveCount(0);
    await page.reload();
    await expect(page.getByRole('button', { name: 'Wieder öffnen: Fallback geprüft', exact: true })).toBeVisible();
    await page.getByRole('link', { name: 'Löschen: Fallback geprüft', exact: true }).click();
    await page.getByRole('button', { name: 'Endgültig löschen' }).click();
    await expect(page.getByRole('heading', { name: 'Noch keine Aufgaben' })).toBeVisible();
  } finally { await context.close(); }
});

test('dialog load failure is recoverable without changing the task', async ({ page }, testInfo) => {
  await page.goto('/tasks');
  await page.getByRole('textbox', { name: 'Name', exact: true }).fill('Release vorbereiten');
  await page.getByRole('button', { name: 'Aufgabe anlegen', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Release vorbereiten', exact: true })).toBeVisible();
  await page.screenshot({ path: testInfo.outputPath('task-list.png'), fullPage: true });
  await page.route('**/tasks/*/edit', route => route.abort());
  const opener = page.getByRole('link', { name: 'Umbenennen: Release vorbereiten', exact: true });
  await opener.click();
  const dialog = page.getByRole('dialog');
  await expect(dialog.getByRole('heading', { name: 'Ansicht nicht verfügbar' })).toBeVisible();
  await expect(dialog.getByRole('button', { name: 'Dialog schließen' })).toBeFocused();
  await audit(page);
  await dialog.getByRole('button', { name: 'Dialog schließen' }).click();
  await expect(opener).toBeFocused();
  await expect(page.getByRole('heading', { name: 'Release vorbereiten', exact: true })).toBeVisible();
});
