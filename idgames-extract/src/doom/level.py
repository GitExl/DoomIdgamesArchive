from dataclasses import dataclass
from typing import List, Optional, Tuple


@dataclass(frozen=True)
class Vertex:
    __slots__ = ['x', 'y']

    x: float
    y: float


@dataclass(frozen=True)
class Line:
    __slots__ = ['vertex_start', 'vertex_end', 'side_front', 'side_back', 'flags', 'type', 'tag', 'args']

    vertex_start: int
    vertex_end: int
    side_front: int
    side_back: int
    flags: int
    type: int
    tag: int
    args: Tuple[int, int, int, int, int]


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
    __slots__ = ['z_floor', 'z_ceiling', 'texture_floor', 'texture_ceiling', 'tag', 'type', 'light']

    z_floor: int
    z_ceiling: int
    texture_floor: str
    texture_ceiling: str
    tag: int
    type: int
    light: int


@dataclass(frozen=True)
class Thing:
    __slots__ = ['x', 'y', 'angle', 'type', 'flags', 'z', 'tag', 'special', 'args']

    x: float
    y: float
    angle: int
    type: int
    flags: int
    z: float
    tag: int
    special: int
    args: Tuple[int, int, int, int, int]


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
