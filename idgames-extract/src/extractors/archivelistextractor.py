from archives.archivelist import ArchiveList
from archives.wadarchive import WADArchive
from extractors.extractorbase import ExtractorBase

from game import Game


iwads = {
    Game.DOOM2: WADArchive.from_path('iwads/doom2.wad'),
    Game.DOOM: WADArchive.from_path('iwads/doom.wad'),
    Game.HERETIC: WADArchive.from_path('iwads/heretic.wad'),
    Game.HEXEN: WADArchive.from_path('iwads/hexen.wad'),
    Game.TNT: WADArchive.from_path('iwads/tnt.wad'),
    Game.PLUTONIA: WADArchive.from_path('iwads/plutonia.wad'),
    Game.STRIFE: WADArchive.from_path('iwads/strife0.wad'),
    Game.HACX: WADArchive.from_path('iwads/hacx.wad'),
}


class ArchiveListExtractor(ExtractorBase):

    def extract(self, info: dict) -> dict:
        if 'archive' not in info:
            self.logger.warn('Cannot create archive list without main archive.')
            return {}

        if info['game'] not in iwads:
            self.logger.warn('Cannot create archive list for archive for an unknown game.')
            return {}

        iwad = iwads[info['game']]

        archive_list = ArchiveList()
        archive_list.append(iwad)
        archive_list.append(info['archive'])

        return {
            'archive_list': archive_list,
        }
