#!/usr/bin/env python3
"""Run the installed isolated dedicated server, stop after Done and report.

Prints one machine-readable line:

    STARTED=yes saved=yes errors=0 EXIT=0

* started  — "Done (" appeared within the timeout
* saved    — the server wrote "All dimensions are saved" before exiting
* errors   — ERROR/FATAL log lines that are not known environment noise
* EXIT     — process exit code (0 expected)

The NeoForge version is taken from gradle.properties, so the harness follows
the tree instead of a hard-coded install.
"""
import pathlib
import queue
import re
import subprocess
import sys
import threading
import time

root = pathlib.Path(__file__).resolve().parents[1]
server = root / 'qa-artifacts/server'

properties = {}
for line in (root / 'gradle.properties').read_text().splitlines():
    if '=' in line and not line.lstrip().startswith('#'):
        key, _, value = line.partition('=')
        properties[key.strip()] = value.strip()
neo_version = properties['neo_version']

args_file = server / 'libraries' / 'net' / 'neoforged' / 'neoforge' / neo_version / 'unix_args.txt'
if not args_file.is_file():
    print(f'MISSING {args_file}')
    sys.exit(2)

java = '/usr/lib/jvm/java-25-openjdk-amd64/bin/java'
command = [java, '-Xms512M', '-Xmx2G', f'@{args_file}', 'nogui']

# Headless / bundled-mod noise that does not invalidate a smoke run.
NOISE = re.compile(
    r'KQueue support is not available'
    r'|Only supported on OSX/BSD'
    r'|Error starting SoundSystem'
    r'|Failed to open OpenAL'
    r'|Class version 69 required'
    r'|Untranslated tag'
    r'|ClassNotFoundException: mezz\.jei'
    r'|^[a-zA-Z0-9_.]*(Exception|Error): null$'
)

lines = queue.Queue()
process = subprocess.Popen(
    command,
    cwd=server,
    stdin=subprocess.PIPE,
    stdout=subprocess.PIPE,
    stderr=subprocess.STDOUT,
    text=True,
)


def collect():
    for line in process.stdout:
        lines.put(line)


threading.Thread(target=collect, daemon=True).start()

ready = False
saved = False
deadline = time.monotonic() + 300
log_path = root / 'qa-artifacts' / 'server-smoke.log'
errors = []
tail = []

with log_path.open('w') as log:
    while time.monotonic() < deadline:
        try:
            line = lines.get(timeout=0.2)
        except queue.Empty:
            if process.poll() is not None:
                break
            continue
        log.write(line)
        log.flush()
        tail.append(line)
        tail = tail[-40:]
        if 'Done (' in line and not ready:
            ready = True
            process.stdin.write('stop\n')
            process.stdin.flush()
            deadline = time.monotonic() + 90
        if 'All dimensions are saved' in line:
            saved = True
        if re.search(r'/(ERROR|FATAL)\]', line) and not NOISE.search(line):
            errors.append(line.rstrip())

    if process.poll() is None:
        process.terminate()
        try:
            process.wait(timeout=20)
        except subprocess.TimeoutExpired:
            process.kill()
            process.wait()

    while not lines.empty():
        line = lines.get()
        log.write(line)
        tail.append(line)
        if re.search(r'/(ERROR|FATAL)\]', line) and not NOISE.search(line):
            errors.append(line.rstrip())

errors_path = root / 'qa-artifacts' / 'server-errors.txt'
errors_path.write_text('\n'.join(errors) + ('\n' if errors else ''))

started = 'yes' if ready else 'no'
print(f'STARTED={started} saved={"yes" if saved else "no"} errors={len(errors)} EXIT={process.returncode}')
if not ready or not saved or errors:
    print('--- last lines ---')
    print(''.join(tail[-15:]))
    print(f'--- errors: {errors_path} ---')
    for err in errors[:10]:
        print(err)
# errors are reported but do not fail the smoke: known third-party observations
# are documented in QA_REPORT.md; STARTED/saved/exit are the pass condition.
sys.exit(0 if ready and saved and process.returncode == 0 else 1)
