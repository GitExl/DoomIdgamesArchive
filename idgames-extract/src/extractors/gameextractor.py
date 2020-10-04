import json
from typing import Dict

from archives.archivebase import ArchiveBase
from extractors.extractorbase import ExtractorBase
from idgames.game import Game
from textparser.textkeys import TEXT_GAMES
from textparser.textparser import TextParser2
from utils.logger import Logger

LumpScores = Dict[str, Dict[Game, float]]


class GameExtractor(ExtractorBase):

    def __init__(self, logger: Logger, config: dict):
        super().__init__(logger, config)

        self.lump_scores: LumpScores = self._load_lump_scores()

    def _load_lump_scores(self) -> LumpScores:
        lump_scores: LumpScores = {}

        with open(self.config['extractors']['game']['lump_score_table'], 'r') as f:
            scores = json.load(f)

            for file_name, scores in scores.items():
                lump_scores[file_name] = {}

                for game, score in scores.items():
                    lump_scores[file_name][Game(game)] = score

        return lump_scores

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
            if game != Game.UNKNOWN:
                self.logger.stream('game_detect_archive', info['path_idgames'])

        # Last ditch effort, just use the entire text file.
        if game == Game.UNKNOWN and 'text_file' in info:
            game = self.detect_from_text(info['text_file'])
            if game != Game.UNKNOWN:
                self.logger.stream('game_detect_text', info['path_idgames'])

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
        key, data = TextParser2.match_key(text_data, TEXT_GAMES)
        if key:
            return Game(key)

        return Game.UNKNOWN

    def detect_from_archive(self, archive: ArchiveBase) -> Game:
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

        # Attempt detection through lump scores.
        scores = {}
        for file in archive.files:
            if file.name in self.lump_scores:
                for game, score in self.lump_scores[file.name].items():
                    if game in scores:
                        scores[game] += score
                    else:
                        scores[game] = score

        if len(scores):
            game = max(scores.keys(), key=(lambda k: scores[k]))
            if scores[game] >= 5:
                return game

        return Game.UNKNOWN
