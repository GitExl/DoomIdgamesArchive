import codecs
import re

from os import path

from extractors.extractorbase import ExtractorBase
from textparser.textparser2 import TextParser2


class TextExtractor(ExtractorBase):

    def extract(self, info: dict) -> dict:
        text_file = None
        text_path = '{}.txt'.format(info['path_base'])

        # Parse from directory text file.
        if path.isfile(text_path):
            text_file = codecs.open(text_path, 'r', 'latin_1')

        # If no text file exists in the idgames directory, examine the archive file itself.
        if 'main_archive' in info and info['main_archive']:
            main_archive = info['main_archive']
            namelist = main_archive.namelist()

            # Case-insensitive filename search.
            regexp = re.compile('{}\.txt'.format(info['path_base']), re.IGNORECASE)
            for name in namelist:
                if regexp.match(name):
                    print('found {}'.format(name))
                    text_file = main_archive.open(name, 'r')

        if not text_file:
            return {}

        text_parser = TextParser2(self.logger)
        text_parser.parse(text_file)
        text_file.close()
        return text_parser.info
