from enum import Enum
from typing import Tuple

from utils.lexer import Lexer, Rule


class MapInfoParserError(Exception):
    def __init__(self, message: str, position: Tuple[int, int]):
        super(Exception, self).__init__('Line {} column {}: {}'.format(position[0], position[1], message))


class MapInfoToken(Enum):
    WHITESPACE: str = 'white'
    COMMENT: str = 'comment'
    IDENTIFIER: str = 'ident'
    BLOCK_START: str = 'bstart'
    BLOCK_END: str = 'bend'
    ASSIGN: str = 'assign'
    INTEGER: str = 'int'
    FLOAT: str = 'float'
    STRING: str = 'str'
    SEPARATOR: str = 'sep'


class MapInfoParser:

    def __init__(self):
        self.lexer: Lexer = Lexer(
            [
                Rule(MapInfoToken.WHITESPACE, r'[\s\n\r]+', skip=True),
                Rule(MapInfoToken.COMMENT, r'(?:;|//).*?\r?\n', skip=True),
                Rule(MapInfoToken.COMMENT, r'/\*[^*]*\*+(?:[^/*][^*]*\*+)*/', skip=True),
                Rule(MapInfoToken.ASSIGN, '='),
                Rule(MapInfoToken.SEPARATOR, ','),
                Rule(MapInfoToken.BLOCK_START, '{'),
                Rule(MapInfoToken.BLOCK_END, '}'),
                Rule(MapInfoToken.FLOAT, '[+-]?[0-9]*\\.[0-9]*(?:[eE][+-]?[0-9]+)?', process=lambda f: float('0' + f) if f[0] == '.' else float(f)),
                Rule(MapInfoToken.INTEGER, '[+-]?[1-9]\\d*|0', process=lambda i: int(i)),
                Rule(MapInfoToken.IDENTIFIER, '[\\?!\\$\\:`@A-Za-z0-9_\\-]+'),
                Rule(MapInfoToken.STRING, '"(?:[^"\\\\]|\\\\.)*(?:"|$)', process=lambda s: s[1:-1]),
            ]
        )

    def parse(self, text: str):
        self.lexer.input(text)

        while True:
            token = self.lexer.get_token()
            if token is None:
                break

            # TODO: parse!
            continue
