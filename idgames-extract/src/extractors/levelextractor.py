from io import BytesIO
from pathlib import Path

from archives.archivebase import ArchiveBase
from archives.wadarchive import WADArchive
from doom.binarylevelreader import BinaryLevelReader
from doom.levelfinder import LevelDataFinder, LevelFormat
from doom.udmflevelreader import UDMFLevelReader
from extractors.extractedinfo import ExtractedInfo
from extractors.extractorbase import ExtractorBase
from idgames.game import Game


class LevelExtractor(ExtractorBase):

    def extract(self, info: ExtractedInfo):
        archive: ArchiveBase = info.archive
        if archive is None:
            self.logger.warn('Cannot extract levels without an archive.')
            return

        if info.game == Game.UNKNOWN:
            self.logger.warn('Cannot extract levels without a known game.')
            return

        level_data_finder = LevelDataFinder()

        # Load levels directly from the archive.
        level_data_finder.add_from_archive(archive)

        # Load levels from maps/*.wad files inside the archive (ZDoom maps/ namespace).
        wad_archives = []
        level_wads = archive.file_find_all_regexp(r'maps/.*\.wad')
        for wad in level_wads:
            wad_base_name = Path(wad.name).stem
            wad_data = BytesIO(wad.get_data())
            wad_archive = WADArchive(wad.name, wad_data, self.logger)
            level_data_finder.add_from_archive(wad_archive, wad_base_name)

            wad_archives.append(wad_archive)

        for level_data in level_data_finder.level_data:
            if level_data.format == LevelFormat.UDMF:
                reader = UDMFLevelReader(info.game, self.logger)
            else:
                reader = BinaryLevelReader(info.game, self.logger)

            level = reader.read(level_data)
            if level:
                info.levels.append(level)
                self.logger.debug('Found {} ({}): {} vertices, {} lines, {} sides, {} sectors, {} things.'.format(
                    level.name, level_data.format.name,
                    len(level.vertices), len(level.lines), len(level.sides), len(level.sectors), len(level.things))
                )

        for archive in wad_archives:
            archive.close()

        self.logger.decision('Found {} valid levels.'.format(len(info.levels)))
