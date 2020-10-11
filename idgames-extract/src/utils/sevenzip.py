import subprocess
from enum import Enum
from io import BytesIO
from os.path import basename, normpath
from pathlib import Path
from typing import Dict, List, Optional


class SZParseMode(Enum):
    NONE = 0
    HEADER = 1
    FILES = 2


class SZArchive:

    def __init__(self, path: str):
        self.path: str = path
        self.type: Optional[str] = None
        self.files: List[SZFile] = []
        self.file_names: Dict[str, int] = {}

        self._read_info()

    def _read_info(self):
        proc = subprocess.Popen(['7za', 'l', '-slt', '-so', '-sccUTF-8', self.path], stdout=subprocess.PIPE, encoding='utf8')

        mode = SZParseMode.NONE
        file: Optional[SZFile] = None

        while True:
            line = proc.stdout.readline()
            if not line:
                break
            line = line.rstrip()

            if line == '--':
                mode = SZParseMode.HEADER
            elif line == '----------':
                mode = SZParseMode.FILES

            if mode == SZParseMode.NONE:
                continue

            if line.find('=') == -1:
                continue

            parts = line.split('=', 1)
            parts[0] = parts[0].strip()
            parts[1] = parts[1].strip()

            if mode == SZParseMode.HEADER:
                if parts[0] == 'Type':
                    self.type = parts[1]

            elif mode == SZParseMode.FILES:
                if parts[0] == 'Path':
                    if file:
                        self.file_names[file.path] = len(self.files)
                        self.files.append(file)

                    path = Path(parts[1])
                    file = SZFile(self, path.as_posix())
                elif parts[0] == 'Size':
                    file.size = int(parts[1])
                elif parts[0] == 'Packed Size':
                    file.packed_size = int(parts[1])
                elif parts[0] == 'Method':
                    file.method = parts[1]

        if file:
            self.file_names[file.path] = len(self.files)
            self.files.append(file)

        proc.stdout.close()

    def get_file(self, filename: str):
        if filename in self.file_names:
            return self.files[self.file_names[filename]]

        return None

    def close(self):
        for file in self.files:
            file.close()

    def __repr__(self):
        return '{}: {}'.format(self.path, self.type)


class SZFile:

    def __init__(self, archive: SZArchive, path: str):
        self.archive: SZArchive = archive
        self.path: str = path
        self.name: str = basename(self.path)

        self.size: Optional[int] = None
        self.packed_size: Optional[int] = None
        self.method: Optional[str] = None

        self._data: Optional[BytesIO] = None

    def get_data(self) -> BytesIO:
        if self._data:
            return self._data

        proc = subprocess.Popen(['7za', 'e', '-so', '-sccUTF-8', self.archive.path, self.path], stdout=subprocess.PIPE)
        data = proc.stdout.read()
        proc.stdout.close()

        self._data = BytesIO(data)
        return self._data

    def close(self):
        if self._data:
            self._data.close()

    def __repr__(self):
        return '{}: {} bytes, {} bytes packed with {}'.format(self.path, self.size, self.packed_size, self.method)
