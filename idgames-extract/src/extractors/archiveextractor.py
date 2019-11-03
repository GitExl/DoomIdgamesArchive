import re

from os.path import basename
from typing import List, Optional
from zipfile import ZipFile, BadZipFile

from archives.archivebase import ArchiveBase
from archives.wadarchive import WADArchive
from archives.ziparchive import ZIPArchive
from extractors.extractorbase import ExtractorBase


class ArchiveExtractor(ExtractorBase):

    EXTENSIONS: List[str] = [
        'wad',
        'pk3',
        'pk7'
    ]

    def extract(self, info: dict) -> dict:
        try:
            main_archive = ZipFile(info['path'])
            namelist = main_archive.namelist()
        except BadZipFile:
            namelist = []
            main_archive = None
            self.logger.error('Bad ZIP file.')
        main_archive_files = [name for name in namelist]

        data_filename = self.get_data_filename(info['filename_base'], main_archive_files)
        if data_filename:
            archive = self.load_archive(main_archive, data_filename)
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
    def get_data_filename(file_basename: str, archive_files: List[str]) -> Optional[str]:
        for extension in ArchiveExtractor.EXTENSIONS:

            # Look for a file with the same basename as the ZIP.
            wad_filename = '{}.{}'.format(file_basename, extension).lower()
            for filename in archive_files:
                if basename(filename).lower() == wad_filename:
                    return filename

            # Return the first available file with the right extension.
            for filename in archive_files:
                if re.match(r'.*\.{}$'.format(extension), filename, re.IGNORECASE):
                    return filename

        return None

    def load_archive(self, zip_file: ZipFile, filename: str) -> Optional[ArchiveBase]:
        try:
            with zip_file.open(filename) as f:
                magic_bytes = f.read(4)
        except NotImplementedError:
            self.logger.error('Unimplemented compression method.')
            self.logger.stream('unimplemented_compression', '{} in {}'.format(filename, zip_file.filename))
            return None

        archive = None
        if magic_bytes[0:2] == b'PK':
            file = zip_file.open(filename)
            archive = ZIPArchive(file)

        elif magic_bytes[0:4] == b'PWAD' or magic_bytes[0:4] == b'IWAD':
            data = zip_file.open(filename)
            archive = WADArchive(data)

        elif magic_bytes[0:2] == b'7z':
            archive = None
            self.logger.error('7zip is not yet supported.')
            self.logger.stream('7zip_unsupported', '{} in {}'.format(filename, zip_file.filename))

        else:
            self.logger.error('Cannot determine type of archive.')
            self.logger.stream('archive_type_unknown', '{} in {}'.format(filename, zip_file.filename))

        return archive
