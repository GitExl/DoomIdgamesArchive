from os.path import splitext, basename, relpath
from pathlib import Path

from extractors.archiveextractor import ArchiveExtractor
from extractors.archivelistextractor import ArchiveListExtractor
from extractors.gameextractor import GameExtractor
from extractors.graphicsextractor import GraphicsExtractor
from extractors.textextractor import TextExtractor

from writers.databasewriter import DatabaseWriter
from writers.graphicswriter import GraphicsWriter

from ignorelist import must_ignore
from logger import Logger


SOURCE_DIR = './idgames'

EXTRACTORS = [
    ArchiveExtractor,
    TextExtractor,
    GameExtractor,
    ArchiveListExtractor,
    GraphicsExtractor,
]

WRITERS = [
    GraphicsWriter,
    DatabaseWriter,
]

logger = Logger()

# Initialize processor instances.
extractors = []
for extractor_class in EXTRACTORS:
    extractors.append(extractor_class(logger))

writers = []
for writer_class in WRITERS:
    writers.append(writer_class(logger))

# Process every zip file in the directory tree.
for path_system in Path('.').glob('{}/**/*.zip'.format(SOURCE_DIR)):
    path_system = str(path_system).replace('\\', '/')

    logger.info('Processing {}...'.format(path_system))

    path_idgames = relpath(path_system, SOURCE_DIR).replace('\\', '/')
    path_idgames_base = splitext(path_idgames)[0]
    path_base = splitext(path_system)[0]
    filename_base = basename(path_base)

    # Ignore some files we'd rather not analyse.
    if must_ignore(path_idgames):
        logger.info('Ignoring')
        continue

    info = {
        'path': path_system,
        'path_base': path_base,
        'path_idgames': path_idgames,
        'path_idgames_base': path_idgames_base,
        'filename_base': filename_base,
    }

    # Run all extractors in sequence.
    for extractor in extractors:
        info.update(extractor.extract(info))

    # Close any archive list left open. This will also close any associated archives.
    if 'archive_list' in info and info['archive_list']:
        info['archive_list'].close()
    if 'main_archive' in info and info['main_archive']:
        info['main_archive'].close()

    # Run all writers.
    for writer in writers:
        writer.write(info)
