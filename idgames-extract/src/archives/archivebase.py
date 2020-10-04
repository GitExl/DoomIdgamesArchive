import re
from os.path import basename, splitext
from re import RegexFlag
from typing import List, BinaryIO, Optional, IO

from archives.archivefilebase import ArchiveFileBase


class ArchiveBase:

    def __init__(self, file: IO[bytes]):
        self.files: List[ArchiveFileBase] = []
        self.file: IO[bytes] = file
        self.is_main: bool = False

        self.read(file)

    @classmethod
    def from_path(cls, path: str):
        file = open(path, 'rb')
        return cls(file)

    def read(self, file: IO[bytes]):
        pass

    def file_add(self, file: ArchiveFileBase):
        file.owner = self
        self.files.append(file)

    def file_find_basename(self, file_basename: str) -> Optional[ArchiveFileBase]:
        file_basename = file_basename.lower()

        for file in reversed(self.files):
            filename = basename(splitext(file.name)[0]).lower()
            if file_basename == filename:
                return file

        return None

    def file_find_regexp(self, regexp: str) -> Optional[ArchiveFileBase]:
        for file in reversed(self.files):
            if re.match(regexp, file.name, RegexFlag.IGNORECASE):
                return file

        return None

    def close(self):
        self.file.close()

    def get_file_data(self, file: ArchiveFileBase) -> bytes:
        pass
