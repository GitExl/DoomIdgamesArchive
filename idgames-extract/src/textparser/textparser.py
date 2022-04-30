import re

from re import Pattern, RegexFlag
from typing import TextIO, Tuple, Optional, List

from idgames.engine import Engine
from idgames.game import Game
from utils.logger import Logger
from textparser.textkeys import TEXT_ENGINE, TEXT_KEYS, KeyType, TEXT_GAMES, TEXT_BOOLEAN, TEXT_DIFFICULTY


KEY_VALUE_MIN_TRAILING_WHITESPACE = 3

RE_KEY_VALUE: Pattern = re.compile(r':(?!//)')
RE_HEADER: Pattern = re.compile(r'[+]?[=\-]{2,}')
RE_WHITESPACE_COLLAPSE: Pattern = re.compile(r'\s\s+')
RE_ORDERED_LIST: Pattern = re.compile(r'[0-9]\.\s')


class TextParser:

    def __init__(self, logger: Logger):
        self.logger: Logger = logger
        self.info: dict = {}
        self.pairs: List[Tuple[str, str]] = []

    def parse(self, file: TextIO):
        key = None
        values = []
        newline_count = 0

        for line in file:
            line = line.rstrip()

            # Skip blank lines, but track how many.
            is_blank = len(line.strip()) == 0
            if is_blank:
                newline_count += 1
                continue

            # After some newlines, assume a break in between key\values.
            elif newline_count > 2:
                self.add_pair(key, values)
                key = None
                values = []

            newline_count = 0

            # Headers are treated as the key of a new key\value pair.
            header = self.detect_header(line)
            if header is not None:
                self.add_pair(key, values)
                key = header.lower()
                values = []
                continue

            # Detect key\value pairs or just values to append to the current pair.
            detect_key, detect_value = self.detect_key_value(line)
            if detect_key is not None:
                self.add_pair(key, values)
                key = detect_key
                values = [detect_value]

            elif detect_value is not None and len(detect_value):
                values.append(detect_value)

        # Add the last key\value pair if any.
        self.add_pair(key, values)

        # Convert pairs into useful data.
        for key, value in self.pairs:
            if not self.parse_pair(key, value):
                self.logger.stream('pairs', '{} :: {}'.format(key, value))
                self.logger.stream('keys', key)

    def add_pair(self, key: Optional[str], values: List[str]):
        if len(values) == 0 or key is None or len(key) == 0:
            return

        # Strip useless trailing characters from keys.
        if key[0] == '*':
            key = key.strip('* ')

        for value in values:
            self.pairs.append((key, value))

    @staticmethod
    def detect_key_value(text: str) -> Tuple[Optional[str], Optional[str]]:

        # Not long enough to contain anything useful.
        if len(text) < KEY_VALUE_MIN_TRAILING_WHITESPACE:
            return None, text.strip()

        # If it starts with enough trailing whitespace, it must just be another value part.
        start = text[0:KEY_VALUE_MIN_TRAILING_WHITESPACE]
        if start.isspace():
            return None, text.strip()

        # If there is at least one colon, this must be a new key\value pair.
        parts = RE_KEY_VALUE.split(text, 1)
        if len(parts) < 2:
            return None, text.strip()

        return parts[0].strip().lower(), parts[1].strip()

    @staticmethod
    def detect_header(text: str) -> Optional[str]:
        text = text.strip()

        # Starts with "5. "
        list_match = RE_ORDERED_LIST.match(text)
        if list_match is not None:
            text = text[list_match.end():]

        # * Detect this *
        if len(text) > 2 and text[0] == '*' and text[-1] == '*':
            return text.strip('*').strip()

        # Strip the initial character from the start and end of the string.
        initial = text[0]
        stripped_text = text.strip(initial).strip()
        if len(stripped_text) == 0:
            return ''

        startswith = RE_HEADER.match(text)
        if startswith is None:
            return None

        # Strip the initial character from the start and end of the string.
        initial = text[0]
        text = text.strip(initial).strip()

        # * Detect this *
        if len(text) > 2 and text[0] == '*' and text[-1] == '*':
            text = text.strip('*').strip()

        if len(text):
            return text.strip()

        return ''

    def parse_pair(self, key: str, value: str) -> bool:
        parser_key, parser_data = self.match_key(key, TEXT_KEYS)
        if not parser_key:
            return False

        # Parse value from the key's type.
        if parser_data['type'] == KeyType.TEXT:
            value = str(value).strip()
        elif parser_data['type'] == KeyType.BOOL:
            value = self.parse_bool(value)
        elif parser_data['type'] == KeyType.GAME:
            value = self.parse_game(value)
        elif parser_data['type'] == KeyType.DIFFICULTY:
            value = self.parse_difficulty(value)
        elif parser_data['type'] == KeyType.ENGINE:
            value = self.parse_engine(value)

        # TODO
        elif parser_data['type'] == KeyType.MAP_NUMBER:
            value = str(value).strip()
        elif parser_data['type'] == KeyType.INTEGER:
            value = str(value).strip()
        elif parser_data['type'] == KeyType.GAME_STYLE:
            value = str(value).strip()
        elif parser_data['type'] == KeyType.DATETIME:
            value = str(value).strip()

        else:
            raise Exception('Unimplemented parser for key type "{}"'.format(parser_data['type']))

        # Append the value based on the type.
        if parser_data.get('array', False):
            if parser_key not in self.info:
                self.info[parser_key] = [value]
            else:
                self.info[parser_key].append(value)

        else:
            if parser_data['type'] == KeyType.TEXT:
                if not len(value):
                    return True
                value = RE_WHITESPACE_COLLAPSE.sub(' ', value)

                if parser_key not in self.info:
                    self.info[parser_key] = value
                elif not parser_data.get('single_line', False):
                    self.info[parser_key] += ' ' + value

            elif parser_data['type'] == KeyType.DIFFICULTY:
                if value is not None:
                    if parser_key not in self.info:
                        self.info[parser_key] = value
                    else:
                        self.info[parser_key] |= value

            elif parser_data['type'] == KeyType.BOOL:
                if parser_key not in self.info:
                    self.info[parser_key] = value
                else:
                    self.info[parser_key] |= value

            else:
                self.info[parser_key] = value

        return True

    @staticmethod
    def match_key(value: str, parser_data: dict) -> Tuple[Optional[str], Optional[dict]]:
        if not len(value):
            return None, None

        for parser_key, data in parser_data.items():
            if 'keys' in data and value in data['keys']:
                return parser_key, data

            if 're' in data:
                for regexp in data['re']:
                    if re.search(regexp, value, RegexFlag.IGNORECASE):
                        return parser_key, data

        return None, None

    def parse_bool(self, value: str) -> bool:
        value = value.lower().strip()
        if not len(value):
            return False

        parser_key, data = self.match_key(value, TEXT_BOOLEAN)
        if parser_key == 'true':
            return True
        elif parser_key == 'false':
            return False

        self.logger.stream('text_parser_value_bool', '{}'.format(value))
        return False

    def parse_game(self, value: str) -> Game:
        value = value.lower().strip()

        parser_key, data = self.match_key(value, TEXT_GAMES)
        if parser_key:
            return Game(parser_key)

        self.logger.stream('text_parser_value_game', '{}'.format(value))
        return Game.UNKNOWN

    def parse_engine(self, value: str) -> Engine:
        value = value.lower().strip()

        # TODO: only allow one to match from all, otherwise many false positives
        parser_key, data = self.match_key(value, TEXT_ENGINE)
        if parser_key:
            return Engine(parser_key)

        self.logger.stream('text_parser_value_engine', '{}'.format(value))
        return Engine.UNKNOWN

    def parse_difficulty(self, value: str) -> Optional[bool]:
        value = value.lower().strip()

        parser_key, data = self.match_key(value, TEXT_DIFFICULTY)
        if parser_key is not None:
            return parser_key == 'true'

        self.logger.stream('text_parser_value_difficulty', '{}'.format(value))
        return None
