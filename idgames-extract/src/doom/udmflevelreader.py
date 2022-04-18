from typing import Dict, Optional

from doom.levelfinder import LevelData
from doom.levelreaderbase import LevelReaderBase
from doom.level import Level, LevelNamespace, LevelFormat
from doom.udmfparser import UDMFParser, UDMFParserError
from utils.lexer import LexerError


UDMF_NAMESPACE_MAP: Dict[str, LevelNamespace] = {
    'doom': LevelNamespace.DOOM,
    'heretic': LevelNamespace.HERETIC,
    'hexen': LevelNamespace.HEXEN,
    'strife': LevelNamespace.STRIFE,
    'zdoom': LevelNamespace.ZDOOM,
    'eternity': LevelNamespace.ETERNITY,
}


class UDMFLevelReader(LevelReaderBase):

    def read(self, level_data: LevelData) -> Optional[Level]:
        map_lump = level_data.files.get('TEXTMAP')
        if map_lump is None:
            self.logger.error('Cannot load UDMF level that has no TEXTMAP lump.')
            return None

        level_name = level_data.name
        text = map_lump.get_data().decode('latin1')

        try:
            parser = UDMFParser()
            parser.parse(text)
            namespace = self.map_udmf_namespace(parser.namespace.lower())
            return Level(level_name, namespace, LevelFormat.UDMF, parser.vertices, parser.lines, parser.sides, parser.sectors, parser.things)

        except LexerError as e:
            self.logger.error('Unable to lex "{}": {}'.format(level_name, e))
        except UDMFParserError as e:
            self.logger.error('Unable to parse "{}": {}'.format(level_name, e))

        return None

    def map_udmf_namespace(self, udmf_namespace: str) -> LevelNamespace:
        if udmf_namespace in UDMF_NAMESPACE_MAP:
            return UDMF_NAMESPACE_MAP[udmf_namespace]

        self.logger.warn('Unknown UDMF namespace "{}", using Doom fallback.'.format(udmf_namespace))
        return LevelNamespace.DOOM
