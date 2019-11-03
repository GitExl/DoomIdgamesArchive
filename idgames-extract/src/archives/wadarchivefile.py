from archives.archivebase import ArchiveBase
from archives.archivefilebase import ArchiveFileBase


class WADArchiveFile(ArchiveFileBase):

    def __init__(self, owner: ArchiveBase, name: str, size: int, offset: int):
        super().__init__(owner, name, size)

        self.offset: int = offset
