"""Run restart, persistence, failure and representative-volume checks in an isolated database."""
import json
import os
import socket
import subprocess
import time
import urllib.error
import urllib.request
from pathlib import Path

container = f'task-management-acceptance-{os.getpid()}'
app = None
started = False
log = None

def docker(*args):
    return subprocess.check_output(['docker', *args], text=True).strip()

def sql(statement):
    return docker('exec', container, 'psql', '-U', 'task_management', '-d', 'task_management', '-At', '-c', statement)

def ready(check):
    for _ in range(120):
        try:
            if check():
                return
        except (OSError, subprocess.CalledProcessError):
            pass
        time.sleep(0.5)
    raise RuntimeError('Acceptance environment did not become ready')

def stop_app():
    if app and app.poll() is None:
        app.terminate()
        try:
            app.wait(timeout=20)
        except subprocess.TimeoutExpired:
            app.kill()
            app.wait()

try:
    docker('run', '--rm', '-d', '--name', container, '-p', '127.0.0.1::5432',
           '-e', 'POSTGRES_DB=task_management', '-e', 'POSTGRES_USER=task_management',
           '-e', 'POSTGRES_PASSWORD=local-development-only', 'postgres:17.9-alpine')
    started = True
    ready(lambda: 'accepting connections' in docker('exec', container, 'pg_isready', '-U', 'task_management'))
    db_port = docker('port', container, '5432/tcp').rsplit(':', 1)[1]
    with socket.socket() as sock:
        sock.bind(('127.0.0.1', 0))
        port = sock.getsockname()[1]
    url = f'http://127.0.0.1:{port}/tasks'
    Path('target').mkdir(exist_ok=True)
    log = open('target/acceptance-application.log', 'w')

    def start_app():
        global app
        app = subprocess.Popen(['java', '-jar', 'target/task-management-0.1.0.jar',
            f'--server.port={port}', f'--spring.datasource.url=jdbc:postgresql://127.0.0.1:{db_port}/task_management',
            '--spring.datasource.username=task_management', '--spring.datasource.password=local-development-only',
            '--spring.datasource.hikari.connection-timeout=1000'], stdout=log, stderr=log)
        ready(lambda: urllib.request.urlopen(url, timeout=3).status == 200)

    start_app()
    sql("INSERT INTO tasks(name,done,created,priority) VALUES ('Neustartprüfung',true,'2026-09-24T12:00:00Z','URGENT')")
    before = sql('SELECT id,name,done,created,priority FROM tasks')
    stop_app()
    docker('restart', container)
    db_port = docker('port', container, '5432/tcp').rsplit(':', 1)[1]
    ready(lambda: 'accepting connections' in docker('exec', container, 'pg_isready', '-U', 'task_management'))
    start_app()
    assert sql('SELECT id,name,done,created,priority FROM tasks') == before
    assert 'Neustartprüfung' in urllib.request.urlopen(url).read().decode()
    sql("INSERT INTO tasks(name,done,created,priority) SELECT 'Prüfaufgabe ' || n,false,now(),'NORMAL' FROM generate_series(1,999) n")
    elapsed = []
    for _ in range(5):
        begin = time.perf_counter()
        with urllib.request.urlopen(url, timeout=10) as response:
            html = response.read()
            assert response.status == 200
        elapsed.append(round((time.perf_counter() - begin) * 1000, 2))
    assert html.count(b'class="task-row') == 1000
    # The persistence check is complete; stop only this disposable fixture database.
    docker('stop', container)
    started = False
    try:
        with urllib.request.urlopen(url, timeout=40) as response:
            raise AssertionError(f'Expected failure, received {response.status}')
    except urllib.error.HTTPError as error:
        assert error.code == 500
        body = error.read().decode()
        assert 'jdbc:' not in body and 'Exception' not in body
    report = {'restartPersistence': True, 'tasks': 1000, 'requests': 5,
              'responseMs': elapsed, 'databaseFailureStatus': 500}
    Path('target/acceptance.json').write_text(json.dumps(report, indent=2) + '\n')
    print(json.dumps(report))
finally:
    stop_app()
    if log:
        log.close()
    if started:
        docker('stop', container)
