from dataclasses import dataclass, field
from pathlib import Path
from typing import Dict, List, Optional
from zipfile import ZipFile

from PIL.Image import Image

from archives.archivebase import ArchiveBase
from archives.archivelist import ArchiveList
from doom.level import Level
from idgames.engine import Engine
from idgames.entry import Entry
from idgames.game import Game


@dataclass
class ExtractedInfo:
    path_local: Path
    path_local_base: Path
    path_idgames: Path
    path_idgames_base: Path
    filename_base: str
    entry: Optional[Entry] = None

    main_archive: Optional[ZipFile] = None
    archive: Optional[ArchiveBase] = None
    archive_list: Optional[ArchiveList] = None

    text_keys: Dict[str, any] = field(default_factory=lambda: {})
    text_contents: Optional[str] = None

    title: Optional[str] = None
    game: Game = Game.UNKNOWN
    engine: Engine = Engine.UNKNOWN
    is_singleplayer: Optional[bool] = None
    is_cooperative: Optional[bool] = None
    is_deathmatch: Optional[bool] = None
    authors: List[str] = field(default_factory=lambda: [])
    graphics: Dict[str, Image] = field(default_factory=lambda: {})
    levels: List[Level] = field(default_factory=lambda: [])
