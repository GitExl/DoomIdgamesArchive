from dataclasses import dataclass
from enum import Enum
from struct import Struct
from typing import ClassVar, Dict, List, Optional, Tuple

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
}


class LevelFormat(Enum):
    UNKNOWN = 'unknown'
    DOOM = 'doom'
    HEXEN = 'hexen'
    UDMF = 'udmf'


@dataclass(frozen=True)
class Vertex:
    __slots__ = ['x', 'y']

    x: int
    y: int

    STRUCT_DOOM: ClassVar[Struct] = Struct('<hh')
    STRUCT_HEXEN: ClassVar[Struct] = Struct('<hh')

    @staticmethod
    def unpack_from(values: Tuple, is_hexen: bool):
        return Vertex(values[0], values[1])


@dataclass(frozen=True)
class Line:
    __slots__ = ['vertex_start', 'vertex_end', 'side1', 'side2', 'flags', 'type', 'tag', 'args']

    vertex_start: int
    vertex_end: int
    side1: int
    side2: int
    flags: int
    type: int
    tag: Optional[int]
    args: Optional[Tuple[int, int, int, int, int]]

    STRUCT_DOOM: ClassVar[Struct] = Struct('<HHHHHHH')
    STRUCT_HEXEN: ClassVar[Struct] = Struct('<HHHBBBBBBHH')

    @staticmethod
    def unpack_from(values: Tuple, is_hexen: bool):
        if is_hexen:
            return Line(
                values[0], values[1], values[9], values[10], values[2], values[3],
                None, (values[4], values[5], values[6], values[7], values[8]),
            )

        return Line(values[0], values[1], values[5], values[6], values[2], values[3], values[4], None)


@dataclass(frozen=True)
class Side:
    __slots__ = ['sector', 'texture_upper', 'texture_mid', 'texture_lower', 'texture_x', 'texture_y']

    sector: int
    texture_upper: str
    texture_mid: str
    texture_lower: str
    texture_x: int
    texture_y: int

    STRUCT_DOOM: ClassVar[Struct] = Struct('<hh8s8s8sh')
    STRUCT_HEXEN: ClassVar[Struct] = Struct('<hh8s8s8sh')

    @staticmethod
    def unpack_from(values: Tuple, is_hexen: bool):
        return Side(values[5], values[2], values[4], values[3], values[0], values[1])


@dataclass(frozen=True)
class Sector:
    __slots__ = ['z_floor', 'z_ceiling', 'texture_floor', 'texture_ceiling', 'tag', 'type', 'light']

    z_floor: int
    z_ceiling: int
    texture_floor: str
    texture_ceiling: str
    tag: int
    type: int
    light: int

    STRUCT_DOOM: ClassVar[Struct] = Struct('<hh8s8shhh')
    STRUCT_HEXEN: ClassVar[Struct] = Struct('<hh8s8shhh')

    @staticmethod
    def unpack_from(values: Tuple, is_hexen: bool):
        return Sector(values[0], values[1], values[2], values[3], values[6], values[5], values[4])


@dataclass(frozen=True)
class Thing:
    __slots__ = ['x', 'y', 'angle', 'type', 'flags', 'z', 'tag', 'special', 'args']

    x: int
    y: int
    angle: int
    type: int
    flags: int
    z: Optional[int]
    tag: Optional[int]
    special: Optional[int]
    args: Optional[Tuple[int, int, int, int, int]]

    STRUCT_DOOM: ClassVar[Struct] = Struct('<hhHHH')
    STRUCT_HEXEN: ClassVar[Struct] = Struct('<HhhhHHHBBBBBB')

    @staticmethod
    def unpack_from(values: Tuple, is_hexen: bool):
        if is_hexen:
            return Thing(
                values[1], values[2], values[4], values[5], values[6], values[3], values[0], values[7],
                (values[8], values[9], values[10], values[11], values[12]),
            )

        return Thing(values[0], values[1], values[2], values[3], values[4], None, None, None, None)


class Level:

    def __init__(self, archive: ArchiveBase, header_index: int):
        self.archive: ArchiveBase = archive
        self.format: LevelFormat = LevelFormat.UNKNOWN

        self.vertices: List[Vertex] = []
        self.lines: List[Line] = []
        self.sides: List[Side] = []
        self.sectors: List[Sector] = []
        self.things: List[Thing] = []

        header_lump = archive.files[header_index]
        self.name: str = header_lump.name.upper()

        lumps = self._collect_map_lumps(header_index)
        if self.format == LevelFormat.DOOM or self.format == LevelFormat.HEXEN:
            self._load_doom_map(lumps)

    def _load_doom_map(self, lumps: Dict[str, ArchiveFileBase]):
        self.vertices = self._read_binary_data(lumps, 'VERTEXES', Vertex)
        self.lines = self._read_binary_data(lumps, 'LINEDEFS', Line)
        self.sides = self._read_binary_data(lumps, 'SIDEDEFS', Side)
        self.sectors = self._read_binary_data(lumps, 'SECTORS', Sector)
        self.things = self._read_binary_data(lumps, 'THINGS', Thing)

    def _read_binary_data(self, lumps: Dict[str, ArchiveFileBase], lump_name: str, source_class):
        lump = lumps.get(lump_name)
        if lump is None:
            return []
        data = lump.get_data()

        is_hexen = (self.format == LevelFormat.HEXEN)
        if is_hexen:
            item_struct = source_class.STRUCT_HEXEN
        else:
            item_struct = source_class.STRUCT_DOOM

        # Trim any extraneous data, iter_unpack will not accept it.
        if len(data) % item_struct.size != 0:
            aligned = item_struct.size * int(len(data) / item_struct.size)
            data = data[0:aligned]

        items = []
        for unpacked in item_struct.iter_unpack(data):
            items.append(source_class.unpack_from(unpacked, is_hexen))

        return items

    def _collect_map_lumps(self, header_index: int) -> Dict[str, ArchiveFileBase]:
        lump_index_max = min(len(self.archive.files), header_index + 20)
        next_lump = self.archive.files[header_index + 1]

        lumps: Dict[str, ArchiveFileBase] = {}

        # Collect UDMF map lumps between the header and ENDMAP
        if next_lump.name == 'TEXTMAP':
            self.format = LevelFormat.UDMF
            for index in range(header_index + 1, lump_index_max):
                file = self.archive.files[index]

                if file.name == 'ENDMAP':
                    break

                lumps[file.name] = file

        # Collect valid Doom\Hexen map lumps.
        else:
            self.format = LevelFormat.DOOM
            for index in range(header_index + 1, lump_index_max):
                file = self.archive.files[index]

                # Detect Hexen as a subtype of Doom if BEHAVIOR is present.
                if file.name == 'BEHAVIOR':
                    self.format = LevelFormat.HEXEN
                if file.name not in LEVEL_LUMP_NAMES and not file.name.startswith('GL_'):
                    break

                lumps[file.name] = file

        return lumps
