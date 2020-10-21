from __future__ import annotations

import gzip
from datetime import datetime
from pathlib import Path
from typing import Dict, List


class FileIndexEntry:
    __slots__ = ['path', 'size', 'date']

    def __init__(self, path: str, size: int, date: datetime):
        self.path: str = path
        self.size: int = size
        self.date: datetime = date


FileIndexEntryDict = Dict[str, FileIndexEntry]


class FileIndex:

    def __init__(self, files: FileIndexEntryDict):
        self.entries: FileIndexEntryDict = files

    @staticmethod
    def must_ignore_path(path: str, ignore: List[str]) -> bool:
        for ignore_path in ignore:
            if path.startswith(ignore_path):
                return True

        return False

    @staticmethod
    def from_local_directory(path: Path, ignore_paths: List[str]) -> FileIndex:
        files: FileIndexEntryDict = {}

        for file_path in path.glob('**/*'):
            if file_path.is_dir():
                continue

            path_str = str(file_path.relative_to(path))
            if FileIndex.must_ignore_path(path_str, ignore_paths):
                continue

            stat = file_path.stat()
            date = datetime.fromtimestamp(stat.st_mtime)
            size = stat.st_size
            files[path_str] = FileIndexEntry(path_str, size, date)

        return FileIndex(files)

    @staticmethod
    def from_unix_listing_gz(path: Path, ignore_paths: List[str]) -> FileIndex:
        files: FileIndexEntryDict = {}

        current_dir = None
        path_mtime = datetime.fromtimestamp(path.stat().st_mtime)

        with gzip.open(path, 'rt') as f:
            for line in f.readlines():
                line = line.strip()
                if not len(line):
                    continue

                if line.startswith('total '):
                    continue
                elif line.endswith(':'):
                    current_dir = Path(line[:-1])
                elif dir is not None:
                    file_type = line[0]
                    if file_type == 'd':
                        continue
                    elif file_type == 'l':
                        continue

                    parts = line[30:].strip().replace('  ', ' ').split(' ')
                    if parts[4][0] == '.':
                        continue

                    path_str = str(current_dir / parts[4])
                    if FileIndex.must_ignore_path(path_str, ignore_paths):
                        continue

                    month, day, year = parts[1:4]
                    if year.find(':') >= 0:
                        time = year
                        year = path_mtime.year
                    else:
                        time = '0:00'

                    date = datetime.strptime('{} {} {} {}'.format(year, month, day, time), '%Y %b %d %H:%M')

                    size = int(parts[0])
                    files[path_str] = FileIndexEntry(path_str, size, date)

        return FileIndex(files)
