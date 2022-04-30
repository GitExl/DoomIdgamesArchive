import re

from pathlib import Path
from typing import Dict, Optional, Pattern

IGNORE: Dict[Pattern, str] = {
    re.compile(r'levels/doom/a-c/bedlam2\.zip'): 'X-rated',
    re.compile(r'levels/doom/g-i/ittu\.zip'): 'X-rated',
    re.compile(r'levels/doom2/a-c/cassie\.zip'): 'X-rated',
    re.compile(r'levels/doom2/deathmatch/Ports/m-o/noname3d\.zip'): 'X-rated',
    re.compile(r'levels/doom2/g-i/goatdoom\.zip'): 'Shock image',
    re.compile(r'levels/doom2/m-o/noname\.zip'): 'X-rated',
    re.compile(r'levels/doom2/s-u/ultra\.zip'): 'Invalid WAD file (Wolf3D)',
    re.compile(r'levels/doom2/s-u/udsm1\.zip'): 'Invalid WAD file (Wolf3D)',
    re.compile(r'levels/doom2/deathmatch/a-c/cesspool\.zip'): 'Weird 128-byte header before actual WAD header',
    re.compile(r'levels/doom2/deathmatch/a-c/arcarena\.zip'): 'X-rated',
    re.compile(r'levels/doom2/deathmatch/g-i/gem02\.zip'): 'X-rated',
    re.compile(r'levels/doom2/deathmatch/p-r/rage\.zip'): 'X-rated',
    re.compile(r'levels/doom2/deathmatch/Ports/s-u/sleeper\.zip'): 'X-rated',
    re.compile(r'levels/doom2/deathmatch/Ports/s-u/sottc\.zip'): 'X-rated',
    re.compile(r'levels/doom2/deathmatch/Ports/v-z/yrstick\.zip'): 'X-rated',
    re.compile(r'levels/doom2/Ports/m-o/mastmaga\.zip'): 'X-rated',
    re.compile(r'levels/doom2/Ports/m-o/nina2\.zip'): 'X-rated',
    re.compile(r'levels/doom2/Ports/m-o/ninadbrv\.zip'): 'X-rated',
    re.compile(r'levels/doom2/Ports/d-f/female\.zip'): 'X-rated',
    re.compile(r'levels/doom2/Ports/j-l/lplant\.zip'): 'X-rated',
    re.compile(r'levels/doom2/Ports/0-9/30kmaps\.zip'): '30k map bomb',
    re.compile(r'levels/doom2/Ports/0-9/100kmaps\.zip'): '100k map bomb',
    re.compile(r'themes/terrywads/sreality\.zip'): 'X-rated',
    re.compile(r'themes/terrywads/comsn\.zip'): 'X-rated',
    re.compile(r'themes/wolf3d/.*'): 'Nazi imagery',
    re.compile(r'themes/x-rated/.*'): 'X-rated',
}


def must_ignore(path: Path) -> Optional[str]:
    """
    Returns a reason if idgames path should be ignored, or None if it is ok to process.

    :param path:
    :return:
    """

    for regex, reason in IGNORE.items():
        if regex.match(path.as_posix()):
            return reason

    return None
