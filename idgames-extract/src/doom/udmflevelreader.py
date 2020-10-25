from typing import Optional

from doom.levelfinder import LevelData
from doom.levelreaderbase import LevelReaderBase
from doom.level import Level
from doom.udmfparser import UDMFParser, UDMFParserError
from utils.lexer import LexerError


class UDMFLevelReader(LevelReaderBase):

    def read(self, level_data: LevelData) -> Optional[Level]:
        level_name = level_data.name
        text = level_data.files.get('TEXTMAP').get_data().decode('latin1')

        try:
            parser = UDMFParser()
            parser.parse(text)
            return Level(level_name, parser.vertices, parser.lines, parser.sides, parser.sectors, parser.things)
        except LexerError as e:
            self.logger.error('Unable to lex "{}": {}'.format(level_name, e))
        except UDMFParserError as e:
            self.logger.error('Unable to parse "{}": {}'.format(level_name, e))

        return None
