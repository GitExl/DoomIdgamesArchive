import cProfile
import time
from pathlib import Path
from typing import List, Optional, Tuple

from extractors.archiveextractor import ArchiveExtractor
from extractors.archivelistextractor import ArchiveListExtractor
from extractors.engineextractor import EngineExtractor
from extractors.extractedinfo import ExtractedInfo
from extractors.extractorbase import ExtractorBase
from extractors.gameextractor import GameExtractor
from extractors.levelextractor import LevelExtractor
from extractors.graphicsextractor import GraphicsExtractor
from extractors.mapinfoextractor import MapInfoExtractor
from extractors.propertyextractor import PropertyExtractor
from extractors.textextractor import TextExtractor
from idgames.dbstorage import DBStorage
from idgames.entry import Entry
from utils.config import Config

from writers.graphicswriter import GraphicsWriter

from idgames.ignorelist import must_ignore
from utils.logger import Logger
from writers.writerbase import WriterBase


EXTRACTORS = [
    ArchiveExtractor,
    TextExtractor,
    GameExtractor,
    PropertyExtractor,
    ArchiveListExtractor,
    # MapInfoExtractor,
    LevelExtractor,
    EngineExtractor,
    # GraphicsExtractor,
]

WRITERS = [
    GraphicsWriter,
]


class Extract:

    def __init__(self, config: Config, logger: Logger, storage: DBStorage):
        self.config: Config = config
        self.logger: Logger = logger
        self.storage: DBStorage = storage

        # Initialize processor instances.
        self.extractors: List[ExtractorBase] = []
        for extractor_class in EXTRACTORS:
            self.extractors.append(extractor_class(logger, config))

        self.writers: List[WriterBase] = []
        for writer_class in WRITERS:
            self.writers.append(writer_class(logger, config))

    def process_file(self, path_local: Path) -> Tuple[Optional[ExtractedInfo], Optional[Entry]]:
        path_local_base = path_local.parents[0] / path_local.stem
        path_idgames = path_local.relative_to(self.config.get('paths.idgames'))
        path_idgames_base = path_idgames.parents[0] / path_idgames.stem
        filename_base = path_local.stem

        entry = self.storage.get_entry_by_path(path_idgames)

        # Bail if the file has not been updated since the last scan.
        if entry is not None and entry.file_modified >= int(path_local.stat().st_mtime):
            return None, None

        # Ignore some files we'd rather not analyse.
        ignore_reason = must_ignore(path_idgames)
        if ignore_reason is not None:
            self.logger.info('Ignoring {} because: {}'.format(path_idgames, ignore_reason))
            return None, None

        self.logger.info('Processing {}...'.format(path_idgames))
        info = ExtractedInfo(
            path_local,
            path_local_base,
            path_idgames,
            path_idgames_base,
            filename_base,
        )

        # Run all extractors and writers in sequence.
        for extractor in self.extractors:
            extractor.extract(info)
        for writer in self.writers:
            writer.write(info)

        # Clean up extractors.
        for extractor in reversed(self.extractors):
            extractor.cleanup(info)

        return info, entry

    def close(self):
        # Close extractor and writer classes.
        for writer in reversed(self.writers):
            writer.close()
        for extractor in reversed(self.extractors):
            extractor.close()


def run():
    config = Config()
    logger = Logger(config.get('paths.logs'), Logger.VERBOSITY_DEBUG)
    storage = DBStorage(config)
    extract = Extract(config, logger, storage)

    time_now = int(time.time())

    idgames_local_root = Path(config.get('paths.idgames'))
    paths_system = list(idgames_local_root.rglob('*.zip'))
    for path_system in paths_system:
        info, entry = extract.process_file(path_system)
        if info is None:
            continue

        if entry is None:
            entry = Entry(
                info.path_idgames.as_posix(),
                int(info.path_local.stat().st_mtime),
                time_now,
            )
        else:
            entry.entry_updated = time_now

        entry.title = info.title
        entry.game = info.game
        entry.engine = info.engine

        entry.id = storage.save_entry(entry)
        storage.save_entry_authors(entry, info.authors)
        storage.save_entry_levels(entry, info.levels)
        storage.commit()

    logger.info('Removing dead entries...')
    storage.remove_dead_entries(paths_system)
    logger.info('Removing orphaned authors...')
    storage.remove_orphan_authors()
    logger.info('Removing orphaned levels...')
    storage.remove_orphan_levels()
    storage.commit()

    storage.close()
    extract.close()


#cProfile.run('run()', sort='tottime')
run()
