import re
import codecs
from re import RegexFlag
from typing import TextIO

from game import Game
from logger import Logger
from textparser.textkeys import TEXT_KEYS, KeyType, TEXT_GAMES, TEXT_TRUE, TEXT_FALSE


class TextParser:

    def __init__(self, logger: Logger):
        self.logger: Logger = logger
        self.info: dict = {}

    def parse(self, file: TextIO):
        pairs = []

        key = None
        values = None

        # Parse the file into key: value pairs.
        self.info['text_file'] = file.read()
        file.seek(0)

        for line in file:
            whitespace = line[0].isspace()

            if values is not None and whitespace:
                values = values + ' ' + line.strip()

            elif not whitespace:
                parts = line.split(':')
                if len(parts) < 2:
                    continue

                if key is not None:
                    pairs.append((key, values))

                key = parts[0].strip().lower()
                values = parts[1].strip()

        # Convert pairs into useful data.
        for key, value in pairs:
            self.parse_pair(key, value)

    def parse_pair(self, key: str, value: str):

        # Any key that matches a known one will have it's value assigned to that text key's info.
        for key_name, key_info in TEXT_KEYS.items():

            if self.match_key(key_info, key):

                # Parse value from the key's type.
                if key_info['type'] == KeyType.TEXT:
                    value = str(value)
                elif key_info['type'] == KeyType.BOOL:
                    value = self.parse_bool(key, value)
                elif key_info['type'] == KeyType.GAME:
                    value = self.parse_game(value)

                # Append the value based on the type.
                if 'array' in key_info and key_info['array']:
                    if key_name not in self.info:
                        self.info[key_name] = []
                    self.info[key_name].append(value)

                else:
                    if key_info['type'] == KeyType.TEXT:
                        if key_name not in self.info:
                            self.info[key_name] = ''
                        self.info[key_name] += '\n' + value
                    elif key_info['type'] == KeyType.BOOL:
                        if key_name not in self.info:
                            self.info[key_name] = False
                        self.info[key_name] |= value
                    else:
                        self.info[key_name] = value

                return

        self.logger.stream('text_parser_keys', '{}'.format(key))

    def parse_bool(self, key: str, value: str) -> bool:
        """
        Converts a value into a boolean if possible.

        :param key:
        :param value:
        :return:
        """

        value = value.lower().strip()
        if not len(value):
            return False

        if self.match_key(TEXT_TRUE, value):
            return True
        elif self.match_key(TEXT_FALSE, value):
            return False

        self.logger.stream('text_parser_value_bool'.format(key), '{} :: {}'.format(value, key))
        return False

    def parse_game(self, value: str) -> Game:
        """
        Parses a game value string into one of the recognized games.

        :param value:
        :return:
        """
        value = value.lower().strip()

        for game, game_info in TEXT_GAMES.items():
            if self.match_key(game_info, value):
                return game

        self.logger.stream('text_parser_value_game', '{}'.format(value))
        return Game.UNKNOWN

    @staticmethod
    def match_key(info: dict, value: str) -> bool:
        """
        Returns if a alue is matched by any of a key info's keys or regular expressions.

        :param info:
        :param value:
        :return:
        """

        if not len(value):
            return False

        if 'keys' in info and value in info['keys']:
            return True

        if 're' in info:
            for regexp in info['re']:
                if re.match(regexp, value, RegexFlag.IGNORECASE):
                    return True

        return False
