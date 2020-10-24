from enum import Enum
from typing import List, Optional, Tuple

from doom.levelfinder import LevelData
from doom.levelreaderbase import LevelReaderBase
from doom.level import Level, Line, Sector, Side, Thing, Vertex
from utils.lexer import Lexer, LexerError, Rule


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


def parse_namespace(lexer: Lexer) -> str:
    lexer.require_token(UDMFToken.ASSIGN)
    namespace = lexer.require_token(UDMFToken.STRING)[1:-1]
    lexer.require_token(UDMFToken.END)

    return namespace


def parse_thing(lexer: Lexer) -> Thing:
    start = lexer.pos
    lexer.require_token(UDMFToken.BLOCK_START)

    x = None
    y = None
    angle = 0
    type = None
    flags = 0
    z = 0
    tag = 0
    special = 0
    args = [0, 0, 0, 0, 0]

    while True:
        token = lexer.get_token()
        if token[0] == UDMFToken.BLOCK_END:
            break
        lexer.require_token(UDMFToken.ASSIGN)

        key = token[1]
        if key == 'id':
            tag = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'x':
            x = float(lexer.require_token(UDMFToken.FLOAT))
        elif key == 'y':
            y = float(lexer.require_token(UDMFToken.FLOAT))
        elif key == 'height':
            # These can be integers in rare cases, but we can treat those as floats too.
            z_token = lexer.get_token()
            z = float(z_token[1])
        elif key == 'angle':
            angle = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'type':
            type = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'special':
            special = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'arg0':
            args[0] = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'arg1':
            args[1] = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'arg2':
            args[2] = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'arg3':
            args[3] = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'arg4':
            args[4] = int(lexer.require_token(UDMFToken.INTEGER))
        else:
            lexer.get_token()
        # TODO: flags
        # TODO: arg0str

        lexer.require_token(UDMFToken.END)

    if x is None or y is None:
        raise UDMFParserError('Thing is missing coordinates.', lexer.split_position(start))
    if type is None:
        raise UDMFParserError('Thing is missing a type.', lexer.split_position(start))

    return Thing(x, y, angle, type, flags, z, tag, special, tuple(args))


def parse_vertex(lexer: Lexer) -> Vertex:
    start = lexer.pos
    lexer.require_token(UDMFToken.BLOCK_START)

    x = None
    y = None

    while True:
        token = lexer.get_token()
        if token[0] == UDMFToken.BLOCK_END:
            break
        lexer.require_token(UDMFToken.ASSIGN)

        key = token[1]
        if key == 'x':
            x = float(lexer.require_token(UDMFToken.FLOAT))
        elif key == 'y':
            y = float(lexer.require_token(UDMFToken.FLOAT))
        else:
            lexer.get_token()

        lexer.require_token(UDMFToken.END)

    if x is None or y is None:
        raise UDMFParserError('Vertex is missing coordinates.', lexer.split_position(start))

    return Vertex(x, y)


def parse_line(lexer: Lexer, vanilla_namespace: bool) -> Line:
    start = lexer.pos
    lexer.require_token(UDMFToken.BLOCK_START)

    vertex_start = None
    vertex_end = None
    side_front = -1
    side_back = -1
    flags = 0
    type = 0
    tag = 0 if vanilla_namespace else -1
    args = [0, 0, 0, 0, 0]

    while True:
        token = lexer.get_token()
        if token[0] == UDMFToken.BLOCK_END:
            break
        lexer.require_token(UDMFToken.ASSIGN)

        key = token[1]
        if key == 'v1':
            vertex_start = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'v2':
            vertex_end = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'sidefront':
            side_front = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'sideback':
            side_back = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'special':
            type = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'arg0':
            args[0] = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'arg1':
            args[1] = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'arg2':
            args[2] = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'arg3':
            args[3] = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'arg4':
            args[4] = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'id':
            tag = int(lexer.require_token(UDMFToken.INTEGER))
        else:
            lexer.get_token()
        # TODO: flags
        # TODO: moreids
        # TODO: arg0str

        lexer.require_token(UDMFToken.END)

    if vertex_start is None or vertex_end is None:
        raise UDMFParserError('Line is missing vertices.', lexer.split_position(start))

    return Line(vertex_start, vertex_end, side_front, side_back ,flags, type, tag, tuple(args))


def parse_sector(lexer: Lexer) -> Sector:
    start = lexer.pos
    lexer.require_token(UDMFToken.BLOCK_START)

    z_floor = 0
    z_ceiling = 0
    texture_floor = None
    texture_ceiling = None
    tag = 0
    type = 0
    light = 160

    while True:
        token = lexer.get_token()
        if token[0] == UDMFToken.BLOCK_END:
            break
        lexer.require_token(UDMFToken.ASSIGN)

        key = token[1]
        if key == 'heightfloor':
            z_floor = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'heightceiling':
            z_ceiling = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'texturefloor':
            texture_floor = lexer.require_token(UDMFToken.STRING)[1:-1]
        elif key == 'textureceiling':
            texture_ceiling = lexer.require_token(UDMFToken.STRING)[1:-1]
        elif key == 'lightlevel':
            light = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'id':
            tag = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'special':
            type = int(lexer.require_token(UDMFToken.INTEGER))
        else:
            lexer.get_token()
        # TODO: moreids

        lexer.require_token(UDMFToken.END)

    if texture_floor is None or texture_ceiling is None:
        raise UDMFParserError('Sector has no floor or ceiling texture.', lexer.split_position(start))

    return Sector(z_floor, z_ceiling, texture_floor, texture_ceiling, tag, type, light)


def parse_side(lexer: Lexer) -> Side:
    start = lexer.pos
    lexer.require_token(UDMFToken.BLOCK_START)

    sector = None
    texture_upper = '-'
    texture_mid = '-'
    texture_lower = '-'
    texture_x = 0
    texture_y = 0

    while True:
        token = lexer.get_token()
        if token[0] == UDMFToken.BLOCK_END:
            break
        lexer.require_token(UDMFToken.ASSIGN)

        key = token[1]
        if key == 'sector':
            sector = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'texturetop':
            texture_upper = lexer.require_token(UDMFToken.STRING)[1:-1]
        elif key == 'texturebottom':
            texture_lower = lexer.require_token(UDMFToken.STRING)[1:-1]
        elif key == 'texturemiddle':
            texture_mid = lexer.require_token(UDMFToken.STRING)[1:-1]
        elif key == 'offsetx':
            texture_x = int(lexer.require_token(UDMFToken.INTEGER))
        elif key == 'offsety':
            texture_y = int(lexer.require_token(UDMFToken.INTEGER))
        else:
            lexer.get_token()

        lexer.require_token(UDMFToken.END)

    if sector is None:
        raise UDMFParserError('Side has no sector.', lexer.split_position(start))

    return Side(sector, texture_upper, texture_mid, texture_lower, texture_x, texture_y)


class UDMFLevelReader(LevelReaderBase):

    def read(self, level_data: LevelData) -> Optional[Level]:
        level_name = level_data.name
        text = level_data.files.get('TEXTMAP').get_data().decode('latin1')

        try:
            return self._parse(level_name, text)
        except LexerError as e:
            self.logger.error('Unable to lex "{}": {}'.format(level_name, e))
        except UDMFParserError as e:
            self.logger.error('Unable to parse "{}": {}'.format(level_name, e))

        return None

    def _parse(self, level_name: str, text: str) -> Level:
        lexer = Lexer(
            [
                Rule(UDMFToken.WHITESPACE, r'[\n\r\s]+', skip=True),
                Rule(UDMFToken.COMMENT, r'//.*?\r?\n', skip=True),
                Rule(UDMFToken.COMMENT, r'(?s)/\*.*?\*/', skip=True),
                Rule(UDMFToken.ASSIGN, '='),
                Rule(UDMFToken.END, ';'),
                Rule(UDMFToken.BLOCK_START, '{'),
                Rule(UDMFToken.BLOCK_END, '}'),
                Rule(UDMFToken.IDENTIFIER, '[A-Za-z_]+[A-Za-z0-9_]*'),
                Rule(UDMFToken.FLOAT, '[+-]?[0-9]+\\.[0-9]*([eE][+-]?[0-9]+)?'),
                Rule(UDMFToken.INTEGER, '[+-]?[1-9]\\d*|0'),
                # Rule(UDMFToken.INTEGER,     '0[0-9]+'),
                # Rule(UDMFToken.INTEGER,     '0x[0-9A-Fa-f]+'),
                Rule(UDMFToken.KEYWORD, '[^{}();"\'\n\t ]+'),
                Rule(UDMFToken.STRING, '"(?:[^"\\\\]|\\\\.)*"'),
            ]
        )
        lexer.input(text)

        vanilla_namespace = False
        things: List[Thing] = []
        vertices: List[Vertex] = []
        lines: List[Line] = []
        sides: List[Side] = []
        sectors: List[Sector] = []

        while True:
            token = lexer.get_token()
            if token is None:
                break
            if token[0] != UDMFToken.IDENTIFIER:
                raise UDMFParserError('Expected a root identifier, got "{}".'.format(token[1]), lexer.split_position(token[2]))

            identifier = token[1]
            if identifier == 'namespace':
                namespace = parse_namespace(lexer)
                if namespace == 'doom' or namespace == 'heretic' or namespace == 'strife':
                    vanilla_namespace = True

            elif identifier == 'thing':
                things.append(parse_thing(lexer))
            elif identifier == 'vertex':
                vertices.append(parse_vertex(lexer))
            elif identifier == 'linedef':
                lines.append(parse_line(lexer, vanilla_namespace))
            elif identifier == 'sidedef':
                sides.append(parse_side(lexer))
            elif identifier == 'sector':
                sectors.append(parse_sector(lexer))
            else:
                raise UDMFParserError('Unknown root identifier "{}".'.format(identifier), lexer.split_position(token[2]))

        return Level(level_name, vertices, lines, sides, sectors, things)
