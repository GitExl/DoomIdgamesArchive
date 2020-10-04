import json
from os.path import splitext, basename, relpath
from glob import glob

from extractors.archiveextractor import ArchiveExtractor
from extractors.archivelistextractor import ArchiveListExtractor
from extractors.gameextractor import GameExtractor
from extractors.graphicsextractor import GraphicsExtractor
from extractors.textextractor import TextExtractor

from writers.appdatabasewriter import AppDatabaseWriter
from writers.graphicswriter import GraphicsWriter

from idgames.ignorelist import must_ignore
from utils.logger import Logger


EXTRACTORS = [
    ArchiveExtractor,
    TextExtractor,
    # GameExtractor,
    # ArchiveListExtractor,
    # GraphicsExtractor,
]

WRITERS = [
    # GraphicsWriter,
    # AppDatabaseWriter,
]


with open('config.json', 'r') as f:
    config = json.load(f)

logger = Logger(config['paths']['logs'])


# Initialize processor instances.
extractors = []
for extractor_class in EXTRACTORS:
    extractors.append(extractor_class(logger, config))

writers = []
for writer_class in WRITERS:
    writers.append(writer_class(logger, config))

# Process every zip file in the directory tree.
for path_system in glob('{}/**/*.zip'.format(config['paths']['idgames']), recursive=True):
    path_system = str(path_system).replace('\\', '/')

    path_idgames = relpath(path_system, config['paths']['idgames']).replace('\\', '/')
    path_idgames_base = splitext(path_idgames)[0]
    path_base = splitext(path_system)[0]
    filename_base = basename(path_base)

    logger.info('Processing {}...'.format(path_system))

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
