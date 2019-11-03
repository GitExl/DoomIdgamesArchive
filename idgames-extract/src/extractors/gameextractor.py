from archives.archivebase import ArchiveBase
from extractors.extractorbase import ExtractorBase
from textparser.textparser import Game


class GameExtractor(ExtractorBase):

    def extract(self, info: dict) -> dict:
        game = Game.UNKNOWN

        # Only try to detect the game if one is not set already.
        if 'game' in info and info['game'] != Game.UNKNOWN:
            game = info['game']

        # Detect from idgames path.
        if game == Game.UNKNOWN and 'path_idgames' in info:
            game = self.detect_from_path(info['path_idgames'])

        # Detect from certain lump names.
        if game == Game.UNKNOWN and 'archive' in info:
            game = self.detect_from_archive(info['archive'])

        # Last ditch effort, just use the entire text file.
        if game == Game.UNKNOWN and 'text_file' in info:
            game = self.detect_from_text(info['text_file'])

        if game == Game.UNKNOWN:
            self.logger.warn('Cannot determine game.')
            self.logger.stream('game_unknown', info['path_idgames'])

        return {
            'game': game
        }

    @staticmethod
    def detect_from_path(path: str) -> Game:
        if 'doom2' in path:
            return Game.DOOM2
        elif 'doom' in path:
            return Game.DOOM
        elif 'heretic' in path:
            return Game.HERETIC
        elif 'hexen' in path:
            return Game.HEXEN
        elif 'strife' in path:
            return Game.HEXEN
        elif 'hacx' in path:
            return Game.HACX

        return Game.UNKNOWN

    @staticmethod
    def detect_from_text(text_file: str) -> Game:
        text_data = text_file.lower()

        if 'heretic' in text_data:
            return Game.HERETIC
        elif 'hexen' in text_data:
            return Game.HEXEN
        elif 'strife' in text_data:
            return Game.STRIFE
        elif 'hacx' in text_data:
            return Game.HACX
        elif 'doom2' in text_data or 'doom 2' in text_data:
            return Game.DOOM2
        elif 'doom' in text_data:
            return Game.DOOM

        return Game.UNKNOWN

    @staticmethod
    def detect_from_archive(archive: ArchiveBase) -> Game:
        if archive.file_find_basename('bossback') or archive.file_find_regexp('VILE') or archive.file_find_regexp('CPOS') or archive.file_find_basename('help'):
            return Game.DOOM2

        elif archive.file_find_basename('rsky1') or archive.file_find_basename('rsky2') or archive.file_find_basename('rsky3') or archive.file_find_basename('rsky4'):
            return Game.DOOM2

        elif archive.file_find_basename('m_doom') or archive.file_find_regexp('^CWILV.*'):
            return Game.DOOM2

        elif archive.file_find_basename('m_htic') or archive.file_find_basename('title'):
            return Game.HERETIC

        elif archive.file_find_basename('help1') and not archive.file_find_basename('help2') or archive.file_find_regexp('^WILV.*'):
            return Game.DOOM

        return Game.UNKNOWN
