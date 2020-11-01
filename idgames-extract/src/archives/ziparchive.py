from os.path import dirname, splitext
from pathlib import Path
from typing import IO, List, Optional
from zipfile import ZipFile

from archives.archivebase import ArchiveBase
from archives.wadarchive import WADArchive
from archives.ziparchivefile import ZIPArchiveFile
from utils.logger import Logger


class ZIPArchive(ArchiveBase):

    def __init__(self, name: str, file: IO[bytes], logger: Logger):
        self.zip_file: Optional[ZipFile] = None
        self.child_wads: List[WADArchive] = []

        super().__init__(name, file, logger)

    def read(self, file: IO[bytes]):
        self.file = file
        self.zip_file = ZipFile(file)

        for file_info in self.zip_file.infolist():
            file_base_path = dirname(file_info.filename)
            ext = splitext(file_info.filename)[1]

            # Extract contents of WAD files in the archive root into our own list of files.
            # See https://zdoom.org/wiki/Using_ZIPs_as_WAD_replacement#How_to
            if ext == '.wad' and len(file_base_path) == 0:
                self.logger.debug('Adding files from {}.'.format(file_info.filename))

                wad_archive_file = self.zip_file.open(file_info.filename, 'r')
                wad_archive = WADArchive('{}/{}'.format(self.name, file_info.filename), wad_archive_file, self.logger)
                self.child_wads.append(wad_archive)

                for wad_file in wad_archive.files:
                    self.file_add(wad_file)

            else:
                self.file_add(ZIPArchiveFile(self, file_info.filename, file_info.file_size))

    def get_file_data(self, file: ZIPArchiveFile) -> bytes:
        return self.zip_file.read(file.name)

    def close(self):
        super().close()

        for child_wad in self.child_wads:
            child_wad.close()

        self.zip_file.close()
