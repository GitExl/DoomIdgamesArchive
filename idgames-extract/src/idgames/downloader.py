import os
import shutil
from datetime import datetime
from pathlib import Path
from typing import List
from urllib import request

from utils.logger import Logger


class Downloader:

    def __init__(self, logger: Logger, mirror_list: List[str]):
        self.logger: Logger = logger
        self.mirror_list: List[str] = mirror_list

    def download(self, src_url: str, dest_path: Path):
        self.logger.info('Downloading {}'.format(src_url))

        try:
            modified_timestamp = None

            dest_path.parent.mkdir(parents=True, exist_ok=True)
            for mirror_url in self.mirror_list:
                mirror_src_url = '{}/{}'.format(mirror_url, src_url)

                with dest_path.open('wb') as file_dest:

                    # TODO: use ftplib if the source is ftp:// so we can use the primary Berlin FTP source
                    with request.urlopen(mirror_src_url) as request_src:
                        shutil.copyfileobj(request_src, file_dest)

                        modified_time = request_src.getheader('last-modified')
                        if modified_time is not None:
                            modified_timestamp = datetime.strptime(modified_time, '%a, %d %b %Y %H:%M:%S %Z').timestamp()
                        break

            if modified_timestamp is not None:
                os.utime(dest_path, (modified_timestamp, modified_timestamp))

        except Exception as e:
            self.logger.error('Error while downloading {}: {}'.format(src_url, e))
