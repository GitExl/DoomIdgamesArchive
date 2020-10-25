import os
import sys
from multiprocessing.pool import ThreadPool
from pathlib import Path
from typing import List

from idgames.downloader import Downloader
from idgames.fileindex import FileIndex, FileIndexEntry
from utils.config import Config
from utils.logger import Logger


def file_download(src_url: str, dest_path: Path, downloader: Downloader):
    try:
        downloader.download(src_url, dest_path)
        return src_url, dest_path, None
    except Exception as e:
        return src_url, dest_path, e


def sync():
    config = Config()
    logger = Logger(config.get('paths.logs'))
    downloader = Downloader(logger, config.get('mirrors'))

    path_idgames = Path(config.get('paths.idgames'))
    paths_ignore = config.get('ignore')

    logger.info('Downloading remote file list...')
    remote_index_path = path_idgames / 'ls-laR.gz'
    try:
        downloader.download('ls-laR.gz', remote_index_path)
    except Exception as e:
        logger.error('Unable to download remote file list: {}'.format(e))
        sys.exit(-1)

    logger.info('Parsing remote file list...')
    files_remote = FileIndex.from_unix_listing_gz(remote_index_path, paths_ignore)
    logger.info('Remote has {} files.'.format(len(files_remote.entries)))

    logger.info('Listing local files...')
    files_local = FileIndex.from_local_directory(path_idgames, paths_ignore)
    logger.info('We have {} files.'.format(len(files_local.entries)))


    logger.info('Figuring out what to do...')
    files_add: List[FileIndexEntry] = []
    files_update: List[FileIndexEntry] = []
    files_delete: List[FileIndexEntry] = []
    for path_remote, file_remote in files_remote.entries.items():
        file_local = files_local.entries.get(path_remote)

        if file_local is None:
            files_add.append(file_remote)
        elif file_remote.date > file_local.date or file_remote.size != file_local.size:
            files_update.append(file_remote)

    for path_local, file_local in files_local.entries.items():
        if path_local not in files_remote.entries:
            files_delete.append(file_local)


    download_list: List = []

    logger.info('Deleting {} files.'.format(len(files_delete)))
    for file_delete in files_delete:
        path_local = path_idgames / file_delete.path
        logger.info(str(path_local))
        path_local.unlink(missing_ok=True)

    logger.info('Adding {} files.'.format(len(files_add)))
    for file_add in files_add:
        url_remote = file_add.path.replace(os.path.sep, '/')
        path_local = path_idgames / file_add.path
        download_list.append((url_remote, path_local, downloader))

    logger.info('Updating {} files.'.format(len(files_update)))
    for file_update in files_update:
        url_remote = file_update.path.replace(os.path.sep, '/')
        path_local = path_idgames / file_update.path
        download_list.append((url_remote, path_local, downloader))

    ThreadPool(6).starmap(file_download, download_list)


sync()
