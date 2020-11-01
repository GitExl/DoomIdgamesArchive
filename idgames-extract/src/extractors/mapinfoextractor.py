import re
from typing import Optional, Pattern

from archives.archivebase import ArchiveBase
from archives.archivefilebase import ArchiveFileBase
from doom.mapinfoparser import MapInfoParser
from extractors.extractedinfo import ExtractedInfo
from extractors.extractorbase import ExtractorBase
from utils.lexer import LexerError

FILE_ORDER = [
    'ZMAPINFO',
    'EMAPINFO',
    'RMAPINFO',
    'MAPINFO',
]

RE_CLEAN: Pattern = re.compile('[\x00\xff\x1a]')


class MapInfoExtractor(ExtractorBase):

    def extract(self, info: ExtractedInfo):
        archive: ArchiveBase = info.archive
        if archive is None:
            self.logger.warn('Cannot extract map info without an archive.')
            return

        file: Optional[ArchiveFileBase] = None
        for filename in FILE_ORDER:
            file = archive.file_find_basename(filename)
            if file is not None:
                break

        if file is None:
            return

        if file.name.upper() == 'EMAPINFO':
            self.logger.warn('EMAPINFO not yet supported.')
            return

        text = file.get_data().decode('latin1')
        text = RE_CLEAN.sub('', text)

        parser = MapInfoParser()
        try:
            parser.parse(text)
        except LexerError as e:
            self.logger.stream('mapinfo_lexer_error', info.path_idgames)
            self.logger.stream('mapinfo_lexer_error', str(e))
