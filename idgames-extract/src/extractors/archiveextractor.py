from os.path import basename
from typing import List, Optional

from archives.archivebase import ArchiveBase
from archives.wadarchive import WADArchive
from archives.ziparchive import ZIPArchive
from extractors.extractorbase import ExtractorBase
from utils.sevenzip import SZArchive, SZFile


class ArchiveExtractor(ExtractorBase):

    EXTENSIONS: List[str] = [
        'wad',
        'pk3',
        'pk7'
    ]

    def extract(self, info: dict) -> dict:
        main_archive = SZArchive(info['path'])
        main_file = self.get_data_main_file(info['filename_base'], main_archive)
        if main_file:
            archive = self.load_main_file(main_file)
            if not archive:
                self.logger.warn('Unable to read archive.')
                return {}
        else:
            self.logger.error('Unable to find main data file.')
            self.logger.stream('no_main_data_file', info['path_idgames'])
            return {}

        return {
            'main_archive': main_archive,
            'archive': archive,
        }

    @staticmethod
    def get_data_main_file(file_basename: str, archive: SZArchive) -> Optional[SZFile]:
        for extension in ArchiveExtractor.EXTENSIONS:

            # Look for a file with the same basename as the ZIP.
            wad_filename = '{}.{}'.format(file_basename, extension).lower()
            for file in archive.files:
                if basename(file.name).lower() == wad_filename:
                    return file

            # Return the first available file with the largest file size matching the extension.
            largest: Optional[SZFile] = None
            for file in archive.files:
                if not file.name.lower().endswith(extension):
                    continue
                if largest is None or file.size > largest.size:
                    largest = file
            if largest:
                return largest

        return None

    def load_main_file(self, file: SZFile) -> Optional[ArchiveBase]:
        data = file.get_data()
        magic_bytes = data.getbuffer()[0:4]

        archive = None
        if magic_bytes[0:2] == b'PK':
            archive = ZIPArchive(data)

        elif magic_bytes[0:4] == b'PWAD' or magic_bytes[0:4] == b'IWAD':
            archive = WADArchive(data)

        elif magic_bytes[0:2] == b'7z':
            archive = None
            self.logger.error('7zip is not yet supported.')
            self.logger.stream('7zip_unsupported', '{} in {}'.format(file.path, file.archive.path))

        else:
            self.logger.error('Cannot determine type of archive.')
            self.logger.stream('archive_type_unknown', '{} in {}'.format(file.path, file.archive.path))

        return archive
