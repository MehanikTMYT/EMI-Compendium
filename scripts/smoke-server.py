#!/usr/bin/env python3
"""Run an installed isolated server, stop after Done, fail on timeout/crash."""
import pathlib
import queue
import subprocess
import sys
import threading
import time

root = pathlib.Path(__file__).resolve().parents[1]
server = root / 'qa-artifacts/server'
lines = queue.Queue()
java = '/usr/lib/jvm/java-25-openjdk-amd64/bin/java'
command = [java, '-Xms512M', '-Xmx2G', '@libraries/net/neoforged/neoforge/26.1.2.122/unix_args.txt', 'nogui']
process = subprocess.Popen(command, cwd=server, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
def collect():
    for line in process.stdout:
        lines.put(line)
threading.Thread(target=collect, daemon=True).start()
ready = False
deadline = time.monotonic() + 180
with (root / 'qa-artifacts/server-smoke.log').open('w') as log:
    while time.monotonic() < deadline:
        try:
            line = lines.get(timeout=0.2)
        except queue.Empty:
            if process.poll() is not None:
                break
            continue
        log.write(line)
        log.flush()
        if 'Done (' in line and not ready:
            ready = True
            process.stdin.write('stop\n')
            process.stdin.flush()
            deadline = time.monotonic() + 60
    if process.poll() is None:
        process.terminate()
        try:
            process.wait(timeout=10)
        except subprocess.TimeoutExpired:
            process.kill()
            process.wait()
    while not lines.empty():
        log.write(lines.get())
print(f'DONE={ready} EXIT={process.returncode}')
sys.exit(0 if ready and process.returncode == 0 else 1)
