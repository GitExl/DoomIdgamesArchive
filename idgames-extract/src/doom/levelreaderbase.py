from doom.level import Level
from doom.levelfinder import LevelData
from utils.logger import Logger


class LevelReaderBase:

    def __init__(self, logger: Logger):
        self.logger: Logger = logger

    def read(self, level_data: LevelData) -> Level:
        pass
