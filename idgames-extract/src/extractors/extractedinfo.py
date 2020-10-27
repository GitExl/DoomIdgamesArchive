from dataclasses import dataclass, field
from typing import Dict, List, Optional
from zipfile import ZipFile

from PIL.Image import Image

from archives.archivebase import ArchiveBase
from archives.archivelist import ArchiveList
from doom.level import Level
from idgames.game import Game


@dataclass
class ExtractedInfo:
    path: str
    path_base: str
    path_idgames: str
    path_idgames_base: str
    filename_base: str

    main_archive: Optional[ZipFile] = None
    archive: Optional[ArchiveBase] = None
    archive_list: Optional[ArchiveList] = None

    text_keys: Dict[str, any] = field(default_factory=lambda: {})
    text_contents: Optional[str] = None

    game: Game = Game.UNKNOWN
    graphics: Dict[str, Image] = field(default_factory=lambda: {})
    levels: List[Level] = field(default_factory=lambda: [])
