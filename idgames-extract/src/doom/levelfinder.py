from enum import Enum
from typing import Dict, List, Optional

from archives.archivebase import ArchiveBase
from archives.archivefilebase import ArchiveFileBase


LEVEL_LUMP_NAMES = {
    'THINGS',
    'LINEDEFS',
    'SIDEDEFS',
    'VERTEXES',
    'SEGS',
    'SSECTORS',
    'NODES',
    'SECTORS',
    'REJECT',
    'BLOCKMAP',
    'BEHAVIOR',
    'SCRIPTS',
    'DIALOGUE',
}


class LevelFormat(Enum):
    DOOM = 'doom'
    HEXEN = 'hexen'
    UDMF = 'udmf'


class LevelData:

    def __init__(self, name: str):
        self.name: str = name
        self.files: Dict[str, ArchiveFileBase] = {}
        self.format: LevelFormat = LevelFormat.DOOM

    def add(self, file: ArchiveFileBase):
        self.files[file.name] = file

        if file.name == 'TEXTMAP':
            self.format = LevelFormat.UDMF
        elif self.format != LevelFormat.UDMF and file.name == 'BEHAVIOR':
            self.format = LevelFormat.HEXEN


class LevelDataFinder:

    def __init__(self):
        self.level_data: List[LevelData] = []

    def add_from_archive(self, archive: ArchiveBase, name: Optional[str] = None):
        for index, file in enumerate(archive.files):
            if file.name != 'THINGS' and file.name != 'TEXTMAP':
                continue

            level_data = LevelDataFinder._collect_level_data(archive, index - 1)
            if name is not None:
                level_data.name = name
            self.level_data.append(level_data)

    @staticmethod
    def _collect_level_data(archive: ArchiveBase, header_index: int) -> LevelData:
        header_file = archive.files[header_index]
        level_data: LevelData = LevelData(header_file.name)

        # Collect UDMF map lumps between the header and ENDMAP
        lump_index_max = min(len(archive.files), header_index + 20)
        next_lump = archive.files[header_index + 1]
        if next_lump.name == 'TEXTMAP':
            for index in range(header_index + 1, lump_index_max):
                file = archive.files[index]
                if file.name == 'ENDMAP':
                    break

                level_data.add(file)

        # Collect valid Doom\Hexen map lumps.
        else:
            for index in range(header_index + 1, lump_index_max):
                file = archive.files[index]
                if file.name not in LEVEL_LUMP_NAMES and not file.name.startswith('GL_'):
                    break

                level_data.add(file)

        return level_data
