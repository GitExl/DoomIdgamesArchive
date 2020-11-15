import codecs
import re
from io import StringIO

from os import path

from extractors.extractedinfo import ExtractedInfo
from extractors.extractorbase import ExtractorBase
from textparser.textparser import TextParser


class TextExtractor(ExtractorBase):

    def extract(self, info: ExtractedInfo):
        text_path = info.path_local_base.with_suffix('.txt')
        contents = None

        # Parse from directory text file.
        if path.isfile(text_path):
            text_file = codecs.open(text_path.as_posix(), 'r', 'latin_1')
            contents = text_file.read()
            text_file.close()

            self.logger.decision('Using text file outside archive.')

        # If no text file exists in the idgames directory, examine the archive file itself.
        if contents is None and info.main_archive is not None:
            main_archive = info.main_archive

            # Case-insensitive filename search.
            regexp = re.compile(r'{}\.txt'.format(info.filename_base), re.IGNORECASE)
            for file_info in main_archive.infolist():
                if regexp.match(file_info.filename):
                    text_file = main_archive.open(file_info.filename)
                    contents = text_file.read().decode('latin_1')
                    break

            if contents:
                self.logger.decision('Using text file from inside the archive.')

        if contents is None:
            self.logger.warn('No text file found.')
            return

        text_parser = TextParser(self.logger)
        text_parser.parse(StringIO(contents))

        self.logger.debug('Extracted {} keys from text file.'.format(len(text_parser.info)))

        info.text_keys = text_parser.info
        info.text_contents = contents
