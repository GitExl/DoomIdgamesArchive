import re
from re import Pattern

from extractors.extractedinfo import ExtractedInfo
from extractors.extractorbase import ExtractorBase


RE_AUTHOR_SPLIT: Pattern = re.compile(r'[;:,&]|(\sand\s)|(\s\|\s)')
RE_WHITESPACE_COLLAPSE: Pattern = re.compile(r'\s\s+')


class PropertyExtractor(ExtractorBase):

    def extract(self, info: ExtractedInfo):
        info.title = info.text_keys.get('title', None)
        if info.title is not None and len(info.title) > 255:
            info.title = '{}...'.format(info.title[:252])

        parsed_authors = info.text_keys.get('authors', [])
        parsed_authors = list(set(parsed_authors))

        for author_list in parsed_authors:
            for author in RE_AUTHOR_SPLIT.split(author_list):
                if author is None:
                    continue
                author = author.strip()
                if len(author):
                    info.authors.append(author)
