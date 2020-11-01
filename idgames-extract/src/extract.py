import cProfile
from os.path import splitext, basename, relpath
from glob import glob

from extractors.archiveextractor import ArchiveExtractor
from extractors.archivelistextractor import ArchiveListExtractor
from extractors.extractedinfo import ExtractedInfo
from extractors.gameextractor import GameExtractor
from extractors.levelextractor import LevelExtractor
from extractors.graphicsextractor import GraphicsExtractor
from extractors.mapinfoextractor import MapInfoExtractor
from extractors.textextractor import TextExtractor
from utils.config import Config

from writers.appdatabasewriter import AppDatabaseWriter
from writers.graphicswriter import GraphicsWriter

from idgames.ignorelist import must_ignore
from utils.logger import Logger


EXTRACTORS = [
    ArchiveExtractor,
    TextExtractor,
    GameExtractor,
    # ArchiveListExtractor,
    MapInfoExtractor,
    # LevelExtractor,
    # GraphicsExtractor,
]

WRITERS = [
    # GraphicsWriter,
    # AppDatabaseWriter,
]


def extract():
    config = Config()
    logger = Logger(config.get('paths.logs'), Logger.VERBOSITY_DEBUG)

    # Initialize processor instances.
    extractors = []
    for extractor_class in EXTRACTORS:
        extractors.append(extractor_class(logger, config))
    writers = []
    for writer_class in WRITERS:
        writers.append(writer_class(logger, config))

    # Process every zip file in the directory tree.
    path_system_files = glob('{}/**/*.zip'.format(config.get('paths.idgames')), recursive=True)
    for path_system in path_system_files:
        path_system = str(path_system).replace('\\', '/')
        path_base = splitext(path_system)[0]
        path_idgames = relpath(path_system, config.get('paths.idgames')).replace('\\', '/')
        path_idgames_base = splitext(path_idgames)[0]
        filename_base = basename(path_base)

        logger.info('Processing {}...'.format(path_system))

        # Ignore some files we'd rather not analyse.
        if must_ignore(path_idgames):
            logger.info('Ignoring')
            continue

        info = ExtractedInfo(
            path_system,
            path_base,
            path_idgames,
            path_idgames_base,
            filename_base,
        )

        # Run all extractors and writers in sequence.
        for extractor in extractors:
            extractor.extract(info)
        for writer in writers:
            writer.write(info)

        # Clean up extractors.
        for extractor in reversed(extractors):
            extractor.cleanup(info)

    # Close extractor and writer classes.
    for writer in reversed(writers):
        writer.close()
    for extractor in reversed(extractors):
        extractor.close()


#cProfile.run('extract()', sort='tottime')
extract()
