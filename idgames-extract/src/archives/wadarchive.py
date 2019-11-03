from struct import Struct
from typing import BinaryIO

from archives.archivebase import ArchiveBase
from archives.wadarchivefile import WADArchiveFile


class WADArchive(ArchiveBase):
    TYPE_IWAD: str = 'IWAD'
    TYPE_PWAD: str = 'PWAD'

    S_HEADER: Struct = Struct("<4sII")
    S_LUMP: Struct = Struct("<II8s")

    def read(self, file: BinaryIO):
        self.file = file

        header = file.read(WADArchive.S_HEADER.size)
        wad_type, entry_count, dir_offset = WADArchive.S_HEADER.unpack(header)

        wad_type = wad_type.decode('latin1')
        if wad_type != WADArchive.TYPE_IWAD and wad_type != WADArchive.TYPE_PWAD:
            return

        file.seek(dir_offset)
        for _ in range(entry_count):
            entry = file.read(WADArchive.S_LUMP.size)
            offset, size, name = WADArchive.S_LUMP.unpack(entry)

            # Keep name before first null character.
            try:
                null_index = name.index(b'\x00')
                name = name[0:null_index].decode('latin1')
            except ValueError:
                name = name.decode('latin1')

            self.file_add(WADArchiveFile(self, name, size, offset))

        self.is_main = (wad_type == WADArchive.TYPE_IWAD)

    def get_file_data(self, file: WADArchiveFile) -> bytes:
        self.file.seek(file.offset)
        return self.file.read(file.size)
