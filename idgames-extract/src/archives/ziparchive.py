from typing import IO, Optional
from zipfile import ZipFile

from archives.archivebase import ArchiveBase
from archives.ziparchivefile import ZIPArchiveFile


class ZIPArchive(ArchiveBase):

    def __init__(self, file: IO[bytes]):
        self.zip_file: Optional[ZipFile] = None

        super().__init__(file)

    def read(self, file: IO[bytes]):
        self.file = file
        self.zip_file = ZipFile(file)

        for file_info in self.zip_file.infolist():
            self.file_add(ZIPArchiveFile(self, file_info.filename, file_info.file_size))

    def get_file_data(self, file: ZIPArchiveFile) -> bytes:
        return self.zip_file.read(file.name)

    def close(self):
        super().close()

        self.zip_file.close()
