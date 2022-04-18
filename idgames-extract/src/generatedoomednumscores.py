import json

from typing import Dict, Set
from idgames.engine import Engine
from utils.config import Config
from utils.logger import Logger

engine_factors = {
    Engine.DOOM.value: 1.5,
    Engine.HERETIC.value: 1.5,
    Engine.HEXEN.value: 1.5,
    Engine.STRIFE.value: 1.5,
    Engine.BOOM.value: 1.0,
    Engine.MBF.value: 1.0,
    Engine.ZDOOM.value: 1.0,
    Engine.GZDOOM.value: 1.0,
    Engine.LEGACY.value: 1.0,
    Engine.SKULLTAG.value: 1.0,
    Engine.ZDAEMON.value: 1.0,
    Engine.DOOMSDAY.value: 1.0,
    Engine.EDGE.value: 1.0,
    Engine.ETERNITY.value: 1.0,
    Engine.DOOMRETRO.value: 1.0,
    Engine.ZANDRONUM.value: 1.0,
}

config = Config()

with open(config.get('extractors.engine.doomednum_table'), 'r') as f:
    engines = json.load(f)

# Create sets of doomednums for each engine.
engine_doomednums: Dict[str, Set[str]] = {}
for engine_key, doomednums in engines.items():

    clean_doomednums = []
    for doomednum in doomednums:
        if isinstance(doomednum, int):
            clean_doomednums.append(doomednum)
        elif isinstance(doomednum, str):
            a, b = doomednum.split('-')
            a = int(a)
            b = int(b)
            for num in range(a, b + 1):
                clean_doomednums.append(num)

    engine_doomednums[engine_key] = set(clean_doomednums)

# List which engines each doomednum appears in.
doomednum_engines: Dict[int, Set[str]] = {}
for engine_key, doomednums in engine_doomednums.items():
    engine_factor = engine_factors.get(engine_key, 1.0)

    for doomednum in doomednums:
        if doomednum not in doomednum_engines:
            doomednum_engines[doomednum] = set()
        doomednum_engines[doomednum].add(engine_key)

# Calculate score for each engine in each doomednum.
doomednum_scores: Dict[str, Dict[str, float]] = {}
for doomednum, engine_keys in doomednum_engines.items():
    if len(engine_keys) == len(engine_factors):
        continue

    average = 1 / len(engine_keys)
    presence_scores: Dict[str, float] = {}
    for engine_key in engine_keys:
        presence_scores[engine_key] = average * engine_factors[engine_key]

    doomednum_scores[doomednum] = presence_scores

with open(config.get('extractors.engine.doomednum_scores'), 'w') as f:
    json.dump(doomednum_scores, f, indent=4)
