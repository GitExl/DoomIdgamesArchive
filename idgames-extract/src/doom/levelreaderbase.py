from doom.level import Level
from doom.levelfinder import LevelData


class LevelReaderBase:

    def read(self, level_data: LevelData) -> Level:
        pass
