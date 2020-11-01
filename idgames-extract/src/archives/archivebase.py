import re
from os.path import basename, splitext
from re import RegexFlag
from typing import List, Optional, IO

from archives.archivefilebase import ArchiveFileBase
from utils.logger import Logger


class ArchiveBase:

    def __init__(self, name: str, file: IO[bytes], logger: Logger):
        logger.debug('Opening archive "{}"'.format(name))

        self.name = name
        self.files: List[ArchiveFileBase] = []
        self.file: IO[bytes] = file
        self.is_main: bool = False

        self.logger: Logger = logger

        self.read(file)

    @classmethod
    def from_path(cls, path: str, logger: Logger):
        file = open(path, 'rb')
        return cls(path, file, logger)

    def read(self, file: IO[bytes]):
        pass

    def file_add(self, file: ArchiveFileBase):
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

    def file_find_all_regexp(self, regexp: str) -> List[ArchiveFileBase]:
        files: List[ArchiveFileBase] = []

        for file in reversed(self.files):
            if re.match(regexp, file.name, RegexFlag.IGNORECASE):
                files.append(file)

        return files

    def close(self):
        self.logger.debug('Closing "{}"'.format(self.name))
        self.file.close()

    def get_file_data(self, file: ArchiveFileBase) -> bytes:
        pass
