import zipfile
from os.path import basename
from typing import IO, Optional, Set
from zipfile import ZipFile, ZipInfo

from archives.archivebase import ArchiveBase
from archives.wadarchive import WADArchive
from archives.ziparchive import ZIPArchive
from extractors.extractedinfo import ExtractedInfo
from extractors.extractorbase import ExtractorBase
from utils.sevenzip import SZArchive


class ArchiveExtractor(ExtractorBase):

    EXTENSIONS: Set[str] = [
        'wad',
        'pk3',
        'pk7',
        'pkz',
        'pke',
        'ipk3',
        'pkz',
        'ipk7',
    ]

    def extract(self, info: ExtractedInfo):
        main_fileinfo = None

        try:
            self.logger.debug('Opening "{}"'.format(info.path_local))
            main_archive = ZipFile(info.path_local)
            main_fileinfo = self.get_data_main_fileinfo(info.filename_base, main_archive)

        except zipfile.BadZipFile:
            main_archive = None
            self.logger.error('Bad ZIP file.')

        if main_fileinfo:
            self.logger.decision('Using "{}" as the main data file.'.format(main_fileinfo.filename))

            # Some archives contain files with compression type "Imploded", which Python zipfile cannot
            # decompress. We use the slower 7zip CLI fallback here instead.
            if not self.is_compression_type_supported(main_fileinfo.compress_type):
                self.logger.debug('Opening "{}" as using 7zip process'.format(main_fileinfo.filename))

                main_archive_7z = SZArchive(info.path_local)
                file_7z = main_archive_7z.get_file(main_fileinfo.filename)
                if not file_7z:
                    self.logger.warn('Cannot find file {} in archive.'.format(main_fileinfo.filename))
                    return
                file = file_7z.get_data()

            else:
                file = main_archive.open(main_fileinfo.filename)

            # We do not close the file, as it may be used to read data from later if needed. It is closed when
            # the archive\archivelist is closed later anyway.
            archive = self.load_main_file(file, main_fileinfo.filename, main_archive.filename)
            if not archive:
                self.logger.warn('Unable to read archive.')
                return

        else:
            self.logger.error('Unable to find main data file.')
            self.logger.stream('no_main_data_file', info.path_idgames.as_posix())
            return

        info.main_archive = main_archive
        info.archive = archive

    def cleanup(self, info: ExtractedInfo):
        if info.main_archive is None:
            return

        self.logger.debug('Closing "{}"'.format(info.main_archive.filename))
        info.main_archive.close()

    @staticmethod
    def is_compression_type_supported(method: int) -> bool:
        return (method == zipfile.ZIP_STORED or method == zipfile.ZIP_LZMA or
                method == zipfile.ZIP_DEFLATED or method == zipfile.ZIP_BZIP2)

    @staticmethod
    def get_data_main_fileinfo(file_basename: str, archive: ZipFile) -> Optional[ZipInfo]:
        for extension in ArchiveExtractor.EXTENSIONS:

            # Look for a file with the same basename as the ZIP.
            wad_filename = '{}.{}'.format(file_basename, extension).lower()
            for info in archive.infolist():
                if basename(info.filename).lower() == wad_filename:
                    return info

            # Return the first available file with the largest file size matching the extension.
            largest: Optional[ZipInfo] = None
            for info in archive.infolist():
                if not info.filename.lower().endswith(extension):
                    continue
                if largest is None or info.file_size > largest.file_size:
                    largest = info
            if largest:
                return largest

        return None

    def load_main_file(self, file: IO[bytes], path: str, archive_path: str) -> Optional[ArchiveBase]:
        magic_bytes = file.read(4)

        archive = None
        if magic_bytes[0:2] == b'PK':
            archive = ZIPArchive(path, file, self.logger)

        elif magic_bytes[0:4] == b'PWAD' or magic_bytes[0:4] == b'IWAD':
            archive = WADArchive(path, file, self.logger)

        elif magic_bytes[0:2] == b'7z':
            archive = None
            self.logger.error('7zip is not yet supported for main archive.')
            self.logger.stream('7zip_unsupported', '{} in {}'.format(path, archive_path))

        else:
            self.logger.error('Cannot determine type of archive.')
            self.logger.stream('archive_type_unknown', '{} in {}'.format(path, archive_path))

        return archive
