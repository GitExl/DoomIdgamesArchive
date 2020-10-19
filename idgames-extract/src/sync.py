import gzip
import json
import os
import sys
from http.client import HTTPResponse
from multiprocessing.pool import ThreadPool
from random import randint
from urllib import request
from datetime import datetime
from pathlib import Path
from typing import Dict, List

from utils.logger import Logger


class SyncFile:
    __slots__ = ['path', 'size', 'date']

    def __init__(self, path: str, size: int, date: datetime):
        self.path: str = path
        self.size: int = size
        self.date: datetime = date


def must_ignore_path(path: str, ignore: List[str]) -> bool:
    for ignore_path in ignore:
        if path.startswith(ignore_path):
            return True

    return False


def index_lslar(path: Path, ignore_paths: List[str]) -> Dict[str, SyncFile]:
    files: Dict[str, SyncFile] = {}

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
                if must_ignore_path(path_str, ignore_paths):
                    continue

                month, day, year = parts[1:4]
                if year.find(':') >= 0:
                    time = year
                    year = path_mtime.year
                else:
                    time = '0:00'

                date = datetime.strptime('{} {} {} {}'.format(year, month, day, time), '%Y %b %d %H:%M')

                size = int(parts[0])
                files[path_str] = SyncFile(path_str, size, date)

    return files


def index_local(path: Path, ignore_paths: List[str]) -> Dict[str, SyncFile]:
    files: Dict[str, SyncFile] = {}

    for file_path in path.glob('**/*'):
        if file_path.is_dir():
            continue

        path_str = str(file_path.relative_to(path))
        if must_ignore_path(path_str, ignore_paths):
            continue

        stat = file_path.stat()
        date = datetime.fromtimestamp(stat.st_mtime)
        size = stat.st_size
        files[path_str] = SyncFile(path_str, size, date)

    return files


def file_download(args):
    src_url, dest_path, mirror_list = args

    try:
        dest_path.parent.mkdir(parents=True, exist_ok=True)

        for mirror_url in mirror_list:
            mirror_src_url = '{}/{}'.format(mirror_url, src_url)

            with dest_path.open('wb') as f:
                with request.urlopen(mirror_src_url) as r:
                    last_modified = datetime.strptime(r.getheader('last-modified'), '%a, %d %b %Y %H:%M:%S %Z').timestamp()
                    f.write(r.read())
                    break

        os.utime(dest_path, (last_modified, last_modified))

        return src_url, dest_path, None

    except Exception as e:
        return src_url, dest_path, e


def random_mirror(mirrors: List[str]) -> str:
    return mirrors[randint(0, len(mirrors) - 1)]


with open('config.json', 'r') as f:
    config = json.load(f)

logger = Logger(config['paths']['logs'])


path_idgames = Path(config['paths']['idgames'])
paths_ignore = config['ignore']
mirror_list = config['mirrors']

logger.info('Downloading remote file list...')
lslar_path = path_idgames / 'ls-laR.gz'
_, _, e = file_download(('ls-laR.gz', lslar_path, mirror_list))
if e:
    logger.error('Unable to download remote file list: {}'.format(e))
    sys.exit(-1)

logger.info('Parsing remote file list...')
files_remote: Dict[str, SyncFile] = index_lslar(lslar_path, paths_ignore)
logger.info('Remote has {} files.'.format(len(files_remote)))

logger.info('Listing local files...')
files_local: Dict[str, SyncFile] = index_local(path_idgames, paths_ignore)
logger.info('We have {} files.'.format(len(files_local)))


logger.info('Figuring out what to do...')
files_add: List[SyncFile] = []
files_update: List[SyncFile] = []
files_delete: List[SyncFile] = []
for path_remote, file_remote in files_remote.items():
    file_local = files_local.get(path_remote)

    if file_local is None:
        files_add.append(file_remote)
    elif file_remote.date > file_local.date or file_remote.size != file_local.size:
        files_update.append(file_remote)

for path_local, file_local in files_local.items():
    if path_local not in files_remote:
        files_delete.append(file_local)


download_list = []

logger.info('Deleting {} files.'.format(len(files_delete)))
for file_delete in files_delete:
    path_local = path_idgames / file_delete.path
    logger.info(str(path_local))
    path_local.unlink(missing_ok=True)

logger.info('Adding {} files.'.format(len(files_add)))
for file_add in files_add:
    url_remote = file_add.path.replace(os.path.sep, '/')
    path_local = path_idgames / file_add.path
    download_list.append((url_remote, path_local, mirror_list))

logger.info('Updating {} files.'.format(len(files_update)))
for file_update in files_update:
    url_remote = file_update.path.replace(os.path.sep, '/')
    path_local = path_idgames / file_update.path
    download_list.append((url_remote, path_local, mirror_list))


results = ThreadPool(6).imap_unordered(file_download, download_list)
for src_url, dest_path, e in results:
    if e is not None:
        logger.error('Error downloading {}: {}'.format(src_url, e))
    else:
        logger.info('Downloaded {}'.format(src_url))
