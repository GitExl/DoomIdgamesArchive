import os
from typing import Dict

from archives.archivelist import ArchiveList
from archives.wadarchive import WADArchive
from extractors.extractorbase import ExtractorBase

from idgames.game import Game
from utils.config import Config
from utils.logger import Logger


class ArchiveListExtractor(ExtractorBase):

    def __init__(self, logger: Logger, config: Config):
        super().__init__(logger, config)

        self.iwads: Dict[Game, WADArchive] = {}

        self._add_iwad(Game.DOOM2, 'doom2.wad')
        self._add_iwad(Game.DOOM, 'doom.wad')
        self._add_iwad(Game.HERETIC, 'heretic.wad')
        self._add_iwad(Game.HEXEN, 'hexen.wad')
        self._add_iwad(Game.TNT, 'tnt.wad')
        self._add_iwad(Game.PLUTONIA, 'plutonia.wad')
        self._add_iwad(Game.STRIFE, 'strife0.wad')
        self._add_iwad(Game.HACX, 'hacx.wad')

    def extract(self, info: dict) -> dict:
        if 'archive' not in info:
            self.logger.warn('Cannot create archive list without main archive.')
            return {}

        if info['game'] not in self.iwads:
            self.logger.warn('Cannot create archive list for archive for an unknown game.')
            return {}

        iwad = self.iwads[info['game']]

        self.logger.decision('Using "{}" as IWAD.'.format(os.path.basename(iwad.file.name)))

        archive_list = ArchiveList()
        archive_list.append(iwad)
        archive_list.append(info['archive'])

        return {
            'archive_list': archive_list,
        }

    def _add_iwad(self, game: Game, filename: str):
        wad_path = '{}/{}'.format(self.config.get('paths.iwads'), filename)
        self.iwads[game] = WADArchive.from_path(wad_path)
