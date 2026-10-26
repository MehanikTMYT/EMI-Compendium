#!/usr/bin/env python3
"""Fail on packaging, namespace, translation or plugin multiplicity regressions."""
from collections import Counter
from pathlib import Path
from zipfile import ZipFile
import json
import struct
import tomllib

root = Path(__file__).resolve().parents[1]
jars = [p for p in (root / 'build/libs').glob('*.jar') if 'sources' not in p.name]
assert len(jars) == 1, jars
expected = {
    'ru/mehtm/emicompendium/EmiCompendiumPlugin.class',
    'com/yanny/ali/emi/compatibility/EmiCompatibility.class',
    'com/yanny/awi/emi/compatibility/EmiCompatibility.class',
    'com/christofmeg/justenoughbreeding/emi/EMIPlugin.class',
    'io/github/suel_ki/uei/compat/emi/EMIPlugin.class',
}
with ZipFile(jars[0]) as z:
    assert all(v == 1 for v in Counter(z.namelist()).values())
    plugins = set()
    for name in z.namelist():
        if name.endswith('.class'):
            data = z.read(name)
            assert b'ru/mehanik/emicompendium' not in data, name
            if name.startswith('ru/mehtm/'):
                assert struct.unpack('>H', data[6:8])[0] == 69, name
            if b'Ldev/emi/emi/api/EmiEntrypoint;' in data:
                plugins.add(name)
    assert plugins == expected, plugins
    metadata = tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode())
    ids = [mod['modId'] for mod in metadata['mods']]
    assert len(ids) == len(set(ids)) == 7, ids
    for mod in ['aci', 'ali', 'awi', 'alicompat', 'justenoughbreeding', 'uei', 'emiprofessions', 'emiffect']:
        assert b'Copyright' in z.read('LICENSE-' + mod), mod
    en = json.loads(z.read('assets/emi_compendium/lang/en_us.json'))
    ru = json.loads(z.read('assets/emi_compendium/lang/ru_ru.json'))
    assert en.keys() == ru.keys()
    print(f'PASS: Java 25 classes, namespace, {len(ids)} unique mod ids, {len(plugins)} distinct EMI entrypoints, licenses, {len(en)} locale keys')
    print('\n'.join(sorted(plugins)))
