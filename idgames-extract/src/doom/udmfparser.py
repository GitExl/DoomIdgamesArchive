from enum import Enum
from typing import Dict, List, Optional, Tuple

from doom.level import Line, LineFlags, Sector, Side, Thing, ThingFlags, Vertex
from utils.lexer import Lexer, Rule


class UDMFParserError(Exception):
    def __init__(self, message: str, position: Tuple[int, int]):
        super(Exception, self).__init__('Line {} column {}: {}'.format(position[0], position[1], message))


class UDMFToken(Enum):
    WHITESPACE: str = 'white'
    COMMENT: str = 'comment'
    IDENTIFIER: str = 'ident'
    BLOCK_START: str = 'blkstart'
    BLOCK_END: str = 'blkend'
    ASSIGN: str = 'assign'
    END: str = 'end'
    INTEGER: str = 'int'
    FLOAT: str = 'float'
    KEYWORD: str = 'keyword'
    STRING: str = 'string'


LINE_FLAG_MAP: Dict[str, LineFlags] = {
    'blocking': LineFlags.BLOCK,
    'blockmonsters': LineFlags.BLOCK_MONSTER,
    'twosided': LineFlags.TWO_SIDED,
    'dontpegtop': LineFlags.UNPEG_TOP,
    'dontpegbottom': LineFlags.UNPEG_BOTTOM,
    'secret': LineFlags.SECRET,
    'blocksound': LineFlags.BLOCK_SOUND,
    'dontdraw': LineFlags.HIDDEN,
    'mapped': LineFlags.MAPPED,

    # Boom
    'passuse': LineFlags.PASS_USE,

    # Strife
    'translucent': LineFlags.TRANSLUCENT75,
    'jumpover': LineFlags.JUMP_OVER,
    'blockfloaters': LineFlags.BLOCK_FLOAT,

    # SPAC
    'playercross': LineFlags.PLAYER_CROSS,
    'playeruse': LineFlags.PLAYER_USE,
    'monstercross': LineFlags.MONSTER_CROSS,
    'monsteruse': LineFlags.MONSTER_USE,
    'impact': LineFlags.IMPACT,
    'playerpush': LineFlags.PLAYER_PUSH,
    'monsterpush': LineFlags.MONSTER_PUSH,
    'missilecross': LineFlags.MISSILE_CROSS,
    'repeatspecial': LineFlags.REPEATS,
}


THING_FLAG_MAP: Dict[str, ThingFlags] = {
    'skill1': ThingFlags.SKILL_1,
    'skill2': ThingFlags.SKILL_2,
    'skill3': ThingFlags.SKILL_3,
    'skill4': ThingFlags.SKILL_4,
    'skill5': ThingFlags.SKILL_5,
    'ambush': ThingFlags.AMBUSH,
    'single': ThingFlags.SINGLE,
    'dm': ThingFlags.DM,
    'coop': ThingFlags.COOP,

    # Boom
    'friend': ThingFlags.FRIEND,

    # Hexen
    'dormant': ThingFlags.DORMANT,
    'class1': ThingFlags.CLASS1,
    'class2': ThingFlags.CLASS2,
    'class3': ThingFlags.CLASS3,

    # Strife
    'standing': ThingFlags.STANDING,
    'strifeally': ThingFlags.STRIFE_ALLY,
    'translucent': ThingFlags.TRANSLUCENT,
    'invisible': ThingFlags.INVISIBLE,
}


def parse_namespace(lexer: Lexer) -> str:
    lexer.require_token(UDMFToken.ASSIGN)
    namespace = lexer.require_token(UDMFToken.STRING)
    lexer.require_token(UDMFToken.END)

    return namespace


def parse_ids(more_ids: str) -> List[int]:
    return [int(tag) for tag in more_ids.strip().split(' ')]


def get_bool(lexer: Lexer) -> bool:
    token = lexer.get_token()
    if token[0] != UDMFToken.IDENTIFIER:
        raise UDMFParserError('Expected keyword, "{}".'.format(token[0]), lexer.expand_position(token[2]))

    if token[1] == 'true':
        return True
    elif token[1] == 'false':
        return False

    raise UDMFParserError('Expected "true" or "false", got "{}".'.format(token[1]), lexer.expand_position(token[2]))


def parse_thing(lexer: Lexer) -> Thing:
    start = lexer.pos
    lexer.require_token(UDMFToken.BLOCK_START)

    x: Optional[float] = None
    y: Optional[float] = None
    z: float = 0
    angle: int = 0
    type: Optional[int] = None
    flags: ThingFlags = ThingFlags.NONE
    id: int = 0
    special: int = 0
    arg0: int = 0
    arg1: int = 0
    arg2: int = 0
    arg3: int = 0
    arg4: int = 0
    arg0str: Optional[str] = None

    while True:
        token = lexer.get_token()
        if token[0] == UDMFToken.BLOCK_END:
            break
        lexer.require_token(UDMFToken.ASSIGN)

        key = token[1]
        if key == 'id':
            id = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'x':
            x = lexer.require_token(UDMFToken.FLOAT)
        elif key == 'y':
            y = lexer.require_token(UDMFToken.FLOAT)

        # These can be integers in rare cases, but we can treat those as floats too. A bit messy.
        elif key == 'height':
            z_token = lexer.get_token()
            if z_token[0] == UDMFToken.INTEGER:
                z = float(z_token[1])
            elif z_token[0] == UDMFToken.FLOAT:
                z = z_token[1]
            else:
                raise UDMFParserError('Expected integer or float, got "{}".'.format(z_token[0]), lexer.expand_position(z_token[2]))

        elif key == 'angle':
            angle = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'type':
            type = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'special':
            special = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'arg0str':
            arg0str = lexer.require_token(UDMFToken.STRING)
        elif key == 'arg0':
            arg0 = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'arg1':
            arg1 = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'arg2':
            arg2 = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'arg3':
            arg3 = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'arg4':
            arg4 = lexer.require_token(UDMFToken.INTEGER)

        else:
            flag: Optional[ThingFlags] = THING_FLAG_MAP.get(key)
            if flag is not None:
                if get_bool(lexer):
                    flags |= flag
            else:
                lexer.get_token()

        lexer.require_token(UDMFToken.END)

    if x is None or y is None:
        raise UDMFParserError('Thing is missing coordinates.', lexer.expand_position(start))
    if type is None:
        raise UDMFParserError('Thing is missing a type.', lexer.expand_position(start))

    return Thing(x, y, z, angle, type, flags, id, special, arg0, arg1, arg2, arg3, arg4, arg0str)


def parse_vertex(lexer: Lexer) -> Vertex:
    start = lexer.pos
    lexer.require_token(UDMFToken.BLOCK_START)

    x: Optional[float] = None
    y: Optional[float] = None

    while True:
        token = lexer.get_token()
        if token[0] == UDMFToken.BLOCK_END:
            break
        lexer.require_token(UDMFToken.ASSIGN)

        key = token[1]
        if key == 'x':
            x = lexer.require_token(UDMFToken.FLOAT)
        elif key == 'y':
            y = lexer.require_token(UDMFToken.FLOAT)
        else:
            lexer.get_token()

        lexer.require_token(UDMFToken.END)

    if x is None or y is None:
        raise UDMFParserError('Vertex is missing coordinates.', lexer.expand_position(start))

    return Vertex(x, y)


def parse_line(lexer: Lexer, namespace_is_vanilla: bool) -> Line:
    start = lexer.pos
    lexer.require_token(UDMFToken.BLOCK_START)

    vertex_start: Optional[int] = None
    vertex_end: Optional[int] = None
    side_front: int = -1
    side_back: int = -1
    flags: LineFlags = LineFlags.NONE
    type: int = 0
    ids: List[int] = [0] if namespace_is_vanilla else [-1]
    arg0: int = 0
    arg1: int = 0
    arg2: int = 0
    arg3: int = 0
    arg4: int = 0
    arg0str: Optional[str] = None

    while True:
        token = lexer.get_token()
        if token[0] == UDMFToken.BLOCK_END:
            break
        lexer.require_token(UDMFToken.ASSIGN)

        key = token[1]
        if key == 'v1':
            vertex_start = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'v2':
            vertex_end = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'sidefront':
            side_front = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'sideback':
            side_back = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'special':
            type = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'arg0str':
            arg0str = lexer.require_token(UDMFToken.STRING)
        elif key == 'arg0':
            arg0 = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'arg1':
            arg1 = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'arg2':
            arg2 = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'arg3':
            arg3 = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'arg4':
            arg4 = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'id':
            ids[0] = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'moreids':
            ids.extend(parse_ids(lexer.require_token(UDMFToken.STRING)))

        else:
            flag: Optional[LineFlags] = LINE_FLAG_MAP.get(key)
            if flag is not None:
                if get_bool(lexer):
                    flags |= flag
            else:
                lexer.get_token()

        lexer.require_token(UDMFToken.END)

    if vertex_start is None or vertex_end is None:
        raise UDMFParserError('Line is missing vertices.', lexer.expand_position(start))

    return Line(vertex_start, vertex_end, side_front, side_back, flags, type, ids, arg0, arg1, arg2, arg3, arg4, arg0str)


def parse_sector(lexer: Lexer) -> Sector:
    start = lexer.pos
    lexer.require_token(UDMFToken.BLOCK_START)

    z_floor: int = 0
    z_ceiling: int = 0
    texture_floor: Optional[str] = None
    texture_ceiling: Optional[str] = None
    ids: List[int] = [0]
    type: int = 0
    light: int = 160

    while True:
        token = lexer.get_token()
        if token[0] == UDMFToken.BLOCK_END:
            break
        lexer.require_token(UDMFToken.ASSIGN)

        key = token[1]
        if key == 'heightfloor':
            z_floor = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'heightceiling':
            z_ceiling = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'texturefloor':
            texture_floor = lexer.require_token(UDMFToken.STRING)
        elif key == 'textureceiling':
            texture_ceiling = lexer.require_token(UDMFToken.STRING)
        elif key == 'lightlevel':
            light = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'id':
            ids[0] = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'moreids':
            ids.extend(parse_ids(lexer.require_token(UDMFToken.STRING)))
        elif key == 'special':
            type = lexer.require_token(UDMFToken.INTEGER)
        else:
            lexer.get_token()

        lexer.require_token(UDMFToken.END)

    if texture_floor is None or texture_ceiling is None:
        raise UDMFParserError('Sector has no floor or ceiling texture.', lexer.expand_position(start))

    return Sector(z_floor, z_ceiling, texture_floor, texture_ceiling, ids, type, light)


def parse_side(lexer: Lexer) -> Side:
    start = lexer.pos
    lexer.require_token(UDMFToken.BLOCK_START)

    sector: Optional[int] = None
    texture_upper: str = '-'
    texture_mid: str = '-'
    texture_lower: str = '-'
    texture_x: int = 0
    texture_y: int = 0

    while True:
        token = lexer.get_token()
        if token[0] == UDMFToken.BLOCK_END:
            break
        lexer.require_token(UDMFToken.ASSIGN)

        key = token[1]
        if key == 'sector':
            sector = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'texturetop':
            texture_upper = lexer.require_token(UDMFToken.STRING)
        elif key == 'texturebottom':
            texture_lower = lexer.require_token(UDMFToken.STRING)
        elif key == 'texturemiddle':
            texture_mid = lexer.require_token(UDMFToken.STRING)
        elif key == 'offsetx':
            texture_x = lexer.require_token(UDMFToken.INTEGER)
        elif key == 'offsety':
            texture_y = lexer.require_token(UDMFToken.INTEGER)
        else:
            lexer.get_token()

        lexer.require_token(UDMFToken.END)

    if sector is None:
        raise UDMFParserError('Side has no sector.', lexer.expand_position(start))

    return Side(sector, texture_upper, texture_mid, texture_lower, texture_x, texture_y)


class UDMFParser:

    def __init__(self):
        self.namespace_is_vanilla: bool = False
        self.namespace: str = 'Doom'
        self.things: List[Thing] = []
        self.vertices: List[Vertex] = []
        self.lines: List[Line] = []
        self.sides: List[Side] = []
        self.sectors: List[Sector] = []

        self.lexer: Lexer = Lexer(
            [
                Rule(UDMFToken.WHITESPACE, r'[\s\n\r]+', skip=True),
                Rule(UDMFToken.COMMENT, r'//.*?\r?\n', skip=True),
                Rule(UDMFToken.COMMENT, r'/\*[^*]*\*+(?:[^/*][^*]*\*+)*/', skip=True),
                Rule(UDMFToken.ASSIGN, '='),
                Rule(UDMFToken.END, ';'),
                Rule(UDMFToken.BLOCK_START, '{'),
                Rule(UDMFToken.BLOCK_END, '}'),
                Rule(UDMFToken.IDENTIFIER, '[A-Za-z_]+[A-Za-z0-9_]*'),
                Rule(UDMFToken.FLOAT, '[+-]?[0-9]+\\.[0-9]*(?:[eE][+-]?[0-9]+)?', process=lambda f: float(f)),
                Rule(UDMFToken.INTEGER, '[+-]?[1-9]\\d*|0', process=lambda i: int(i)),
                Rule(UDMFToken.INTEGER, '0[0-9]+', process=lambda i: int(i, 8)),
                Rule(UDMFToken.INTEGER, '0x[0-9A-Fa-f]+', process=lambda i: int(i, 16)),
                Rule(UDMFToken.KEYWORD, '[^{}();"\'\n\t ]+'),
                Rule(UDMFToken.STRING, '"(?:[^"\\\\]|\\\\.)*"', process=lambda s: s[1:-1]),
            ]
        )

    def parse(self, text: str):
        self.lexer.input(text)

        while True:
            token = self.lexer.get_token()
            if token is None:
                break
            if token[0] != UDMFToken.IDENTIFIER:
                raise UDMFParserError('Expected an identifier, got "{}".'.format(token[1]), self.lexer.expand_position(token[2]))

            identifier = token[1]
            if identifier == 'namespace':
                self.namespace = parse_namespace(self.lexer)
                self.namespace_is_vanilla = self.namespace == 'Doom' or self.namespace == 'Heretic' or self.namespace == 'Strife'

            elif identifier == 'thing':
                self.things.append(parse_thing(self.lexer))
            elif identifier == 'vertex':
                self.vertices.append(parse_vertex(self.lexer))
            elif identifier == 'linedef':
                self.lines.append(parse_line(self.lexer, self.namespace_is_vanilla))
            elif identifier == 'sidedef':
                self.sides.append(parse_side(self.lexer))
            elif identifier == 'sector':
                self.sectors.append(parse_sector(self.lexer))
            else:
                raise UDMFParserError('Unknown root identifier "{}".'.format(identifier), self.lexer.expand_position(token[2]))
