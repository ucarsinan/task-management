"""Query OSV for resolved Maven runtime packages; fail on findings or incomplete results."""
import json
import re
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

lines = Path('target/runtime-dependencies.txt').read_text().splitlines()
packages = []
for line in lines:
    match = re.match(r'\s+([^ :]+):([^ :]+):jar:([^ :]+):(compile|runtime)\b', line)
    if match:
        group, artifact, version, _ = match.groups()
        packages.append({'package': {'ecosystem': 'Maven', 'name': f'{group}:{artifact}'}, 'version': version})
if not packages:
    raise RuntimeError('No runtime dependencies found; run Maven dependency:list first')
request = urllib.request.Request('https://api.osv.dev/v1/querybatch',
    data=json.dumps({'queries': packages}).encode(), headers={'Content-Type': 'application/json'})
with urllib.request.urlopen(request, timeout=60) as response:
    results = json.load(response)['results']
if len(results) != len(packages) or any(r.get('next_page_token') for r in results):
    raise RuntimeError('Incomplete advisory response')
findings = [{'package': p, 'vulnerabilities': r['vulns']} for p, r in zip(packages, results) if r.get('vulns')]
report = {'checkedAt': datetime.now(timezone.utc).isoformat(), 'source': 'https://osv.dev', 'packages': len(packages), 'findings': findings}
Path('target/dependency-audit.json').write_text(json.dumps(report, indent=2) + '\n')
print(json.dumps(report, indent=2))
raise SystemExit(1 if findings else 0)
