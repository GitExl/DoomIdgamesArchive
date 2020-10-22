from io import BytesIO
from typing import List

from archives.archivebase import ArchiveBase
from archives.wadarchive import WADArchive
from doom.level import Level
from extractors.extractorbase import ExtractorBase


class LevelExtractor(ExtractorBase):

    def extract(self, info: dict) -> dict:
        archive: ArchiveBase = info.get('archive', None)
        if not archive:
            self.logger.warn('Cannot extract levels without an archive.')
            return {}

        levels: List[Level] = []

        # Load levels directly from archive.
        levels.extend(self._get_levels_from_archive(archive))

        # Load levels from maps/*.wad files inside the archive (ZDoom maps/ namespace).
        level_wads = archive.file_find_all_regexp(r'maps/.*\.wad')
        for wad in level_wads:
            wad_data = BytesIO(wad.get_data())
            wad_archive = WADArchive(wad_data)
            levels.extend(self._get_levels_from_archive(wad_archive))

        return {
            'levels': levels
        }

    def _get_levels_from_archive(self, archive: ArchiveBase) -> List[Level]:
        levels: List[Level] = []

        for index, file in enumerate(archive.files):
            if file.name == 'THINGS' or file.name == 'TEXTMAP':
                level = Level(archive, index - 1)
                self.logger.debug('Found level {} ({}): {} things, {} lines, {} sides, {} sectors.'.format(
                    level.name, level.format.name, len(level.things), len(level.lines), len(level.sides),
                    len(level.sectors))
                )
                levels.append(level)

        return levels