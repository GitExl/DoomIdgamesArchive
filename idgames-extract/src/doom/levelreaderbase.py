from doom.level import Level
from doom.levelfinder import LevelData
from idgames.game import Game
from utils.logger import Logger


class LevelReaderBase:

    def __init__(self, game: Game, logger: Logger):
        self.game: Game = game
        self.logger: Logger = logger

    def read(self, level_data: LevelData) -> Level:
        pass
