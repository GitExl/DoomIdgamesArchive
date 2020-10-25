from dataclasses import dataclass
from enum import Flag, auto
from typing import List, Optional


class ThingFlags(Flag):
    NONE = 0

    SKILL_1 = auto()
    SKILL_2 = auto()
    SKILL_3 = auto()
    SKILL_4 = auto()
    SKILL_5 = auto()
    AMBUSH = auto()
    SINGLE = auto()
    DM = auto()
    COOP = auto()

    # MBF
    FRIEND = auto()

    # Hexen
    DORMANT = auto()
    CLASS1 = auto()
    CLASS2 = auto()
    CLASS3 = auto()

    # Strife
    STANDING = auto()
    STRIFE_ALLY = auto()
    TRANSLUCENT = auto()
    INVISIBLE = auto()


class LineFlags(Flag):
    NONE = 0

    BLOCK = auto()
    BLOCK_MONSTER = auto()
    TWO_SIDED = auto()
    UNPEG_TOP = auto()
    UNPEG_BOTTOM = auto()
    SECRET = auto()
    BLOCK_SOUND = auto()
    HIDDEN = auto()
    MAPPED = auto()

    # Boom
    PASS_USE = auto()

    # Strife
    TRANSLUCENT = auto()
    JUMP_OVER = auto()
    BLOCK_FLOAT = auto()

    # Special triggering
    PLAYER_CROSS = auto()
    PLAYER_USE = auto()
    MONSTER_CROSS = auto()
    MONSTER_USE = auto()
    IMPACT = auto()
    PLAYER_PUSH = auto()
    MONSTER_PUSH = auto()
    MISSILE_CROSS = auto()
    REPEATS = auto()


@dataclass(frozen=True)
class Vertex:
    __slots__ = ['x', 'y']

    x: float
    y: float


@dataclass(frozen=True)
class Line:
    __slots__ = [
        'vertex_start', 'vertex_end',
        'side_front', 'side_back',
        'flags',
        'type',
        'tags',
        'arg0', 'arg1', 'arg2', 'arg3', 'arg4',
        'arg0str',
    ]

    vertex_start: int
    vertex_end: int
    side_front: int
    side_back: int
    flags: LineFlags
    type: int
    tags: List[int]
    arg0: int
    arg1: int
    arg2: int
    arg3: int
    arg4: int
    arg0str: Optional[str]


@dataclass(frozen=True)
class Side:
    __slots__ = ['sector', 'texture_upper', 'texture_mid', 'texture_lower', 'texture_x', 'texture_y']

    sector: int
    texture_upper: str
    texture_mid: str
    texture_lower: str
    texture_x: int
    texture_y: int


@dataclass(frozen=True)
class Sector:
    __slots__ = ['z_floor', 'z_ceiling', 'texture_floor', 'texture_ceiling', 'tags', 'type', 'light']

    z_floor: int
    z_ceiling: int
    texture_floor: str
    texture_ceiling: str
    tags: List[int]
    type: int
    light: int


@dataclass(frozen=True)
class Thing:
    __slots__ = [
        'x', 'y', 'z',
        'angle',
        'type',
        'flags',
        'tag',
        'special',
        'arg0', 'arg1', 'arg2', 'arg3', 'arg4',
        'arg0str',
    ]

    x: float
    y: float
    z: float
    angle: int
    type: int
    flags: ThingFlags
    tag: int
    special: int
    arg0: int
    arg1: int
    arg2: int
    arg3: int
    arg4: int
    arg0str: Optional[str]


class Level:

    def __init__(self, name: str,
                 vertices: List[Vertex] = None, lines: Optional[List[Line]] = None, sides: Optional[List[Side]] = None,
                 sectors: Optional[List[Sector]] = None, things: Optional[List[Thing]] = None):

        self.name: str = name
        self.vertices: List[Vertex] = [] if vertices is None else vertices
        self.lines: List[Line] = [] if lines is None else lines
        self.sides: List[Side] = [] if sides is None else sides
        self.sectors: List[Sector] = [] if sectors is None else sectors
        self.things: List[Thing] = [] if things is None else things
