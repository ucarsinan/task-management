import { spawn, execFileSync } from 'node:child_process';
import { setTimeout as delay } from 'node:timers/promises';
import { createWriteStream, mkdirSync } from 'node:fs';
import net from 'node:net';

const container = `task-management-e2e-${process.pid}`;
const docker = (...args) => execFileSync('docker', args, { encoding: 'utf8' }).trim();
const freePort = () => new Promise((resolve, reject) => {
  const server = net.createServer();
  server.on('error', reject);
  server.listen(0, '127.0.0.1', () => { const port = server.address().port; server.close(() => resolve(port)); });
});
const waitFor = async (check) => {
  for (let i = 0; i < 120; i++) {
    if (await check()) return;
    await delay(500);
  }
  throw new Error('Test environment did not become ready');
};
let app;
let started = false;
try {
  docker('run', '--rm', '-d', '--name', container, '-p', '127.0.0.1::5432',
    '-e', 'POSTGRES_DB=task_management', '-e', 'POSTGRES_USER=task_management',
    '-e', 'POSTGRES_PASSWORD=local-development-only', 'postgres:17.9-alpine');
  started = true;
  await waitFor(() => { try { docker('exec', container, 'pg_isready', '-U', 'task_management'); return true; } catch { return false; } });
  const dbPort = docker('port', container, '5432/tcp').split(':').at(-1);
  const port = await freePort();
  mkdirSync('target', { recursive: true });
  const log = createWriteStream('target/e2e-application.log');
  app = spawn('java', ['-jar', 'target/task-management-0.1.0.jar', `--server.port=${port}`,
    `--spring.datasource.url=jdbc:postgresql://127.0.0.1:${dbPort}/task_management`,
    '--spring.datasource.username=task_management', '--spring.datasource.password=local-development-only'], { stdio: ['ignore', 'pipe', 'pipe'] });
  app.stdout.pipe(log); app.stderr.pipe(log);
  const baseURL = `http://127.0.0.1:${port}`;
  await waitFor(async () => {
    if (app.exitCode !== null) throw new Error('Application exited; see target/e2e-application.log');
    try { return (await fetch(`${baseURL}/tasks`)).ok; } catch { return false; }
  });
  const tests = spawn(process.execPath, ['node_modules/@playwright/test/cli.js', 'test'], {
    stdio: 'inherit', env: { ...process.env, TASK_E2E_URL: baseURL, TASK_E2E_DB: container },
  });
  process.exitCode = await new Promise((resolve, reject) => { tests.on('error', reject); tests.on('exit', code => resolve(code ?? 1)); });
} finally {
  if (app && app.exitCode === null) {
    const stopped = new Promise(resolve => app.once('exit', resolve));
    app.kill('SIGTERM'); await stopped;
  }
  if (started) docker('stop', container);
}
