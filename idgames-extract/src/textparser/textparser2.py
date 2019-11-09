import re

from re import RegexFlag
from typing import TextIO, Tuple, Optional

from game import Game
from logger import Logger
from textparser.textkeys2 import TEXT_KEYS, KeyType, TEXT_GAMES, TEXT_BOOLEAN


class TextParser2:

    def __init__(self, logger: Logger):
        self.logger: Logger = logger
        self.info: dict = {}

    def parse(self, file: TextIO):
        pairs = []

        key = None
        values = []

        file.seek(0)

        for line in file:
            whitespace = line[0].isspace()

            # Append indented information to the current value list.
            if values is not None and whitespace:
                values.append(line.strip())

            # Split other lines into key\value pairs separated by the first colon.
            elif not whitespace:
                parts = line.split(':', 1)
                if len(parts) < 2:
                    values.append(line.strip())
                    continue

                # Store the current key\value pair for later processing.
                if key is not None:
                    pairs.append((key, ' '.join(values)))

                # Create a new current key\value pair.
                key = parts[0].strip().lower()
                values = [parts[1].strip()]

        # Convert pairs into useful data.
        for key, value in pairs:
            self.parse_pair(key, value)

    def parse_pair(self, key: str, value: str):
        parser_key, parser_data = self.match_key(key, TEXT_KEYS)
        if not parser_key:
            return

        # Parse value from the key's type.
        if parser_data['type'] == KeyType.TEXT:
            value = str(value)
        elif parser_data['type'] == KeyType.BOOL:
            value = self.parse_bool(value)
        elif parser_data['type'] == KeyType.GAME:
            value = self.parse_game(value)

        # Append the value based on the type.
        if parser_data.get('array', False):
            if parser_key not in self.info:
                self.info[parser_key] = [value]
            else:
                self.info[parser_key].append(value)

        else:
            if parser_data['type'] == KeyType.TEXT:
                if parser_key not in self.info:
                    self.info[parser_key] = value
                else:
                    self.info[parser_key] += '\n' + value

            elif parser_data['type'] == KeyType.BOOL:
                if parser_key not in self.info:
                    self.info[parser_key] = value
                else:
                    self.info[parser_key] |= value

            else:
                self.info[parser_key] = value

    def match_key(self, value: str, parser_data: dict) -> Tuple[Optional[str], Optional[dict]]:
        if not len(value):
            return None, None

        for parser_key, data in parser_data.items():
            if 'keys' in data and value in data['keys']:
                return parser_key, data

            if 're' in data:
                for regexp in data['re']:
                    if re.match(regexp, value, RegexFlag.IGNORECASE):
                        return parser_key, data

        self.logger.stream('text_parser_keys'.format(value), '{}'.format(value))
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
            return parser_key

        self.logger.stream('text_parser_value_game', '{}'.format(value))
        return Game.UNKNOWN
