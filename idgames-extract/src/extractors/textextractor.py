import codecs
import re
from io import StringIO

from os import path

from extractors.extractorbase import ExtractorBase
from textparser.textparser import TextParser


class TextExtractor(ExtractorBase):

    def extract(self, info: dict) -> dict:
        text_file = None
        text_path = '{}.txt'.format(info['path_base'])

        # Parse from directory text file.
        if path.isfile(text_path):
            text_file = codecs.open(text_path, 'r', 'latin_1')
            self.logger.decision('Using text file outside archive.')

        # If no text file exists in the idgames directory, examine the archive file itself.
        if text_file is None and 'main_archive' in info and info['main_archive']:
            main_archive = info['main_archive']

            # Case-insensitive filename search.
            regexp = re.compile(r'{}\.txt'.format(info['filename_base']), re.IGNORECASE)
            for file_info in main_archive.infolist():
                if regexp.match(file_info.filename):
                    text_file = main_archive.open(file_info.filename)
                    text_data = text_file.read().decode('latin_1')
                    text_file = StringIO(text_data)

            if text_file:
                self.logger.decision('Using text file from inside the archive.')

        if not text_file:
            self.logger.warn('No text file found.')
            return {}

        text_parser = TextParser(self.logger)
        text_parser.parse(text_file)
        text_file.close()

        self.logger.debug('Extracted {} keys from text file.'.format(len(text_parser.info)))

        return text_parser.info
