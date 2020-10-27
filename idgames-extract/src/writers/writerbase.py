from extractors.extractedinfo import ExtractedInfo
from utils.config import Config
from utils.logger import Logger


class WriterBase:

    def __init__(self, logger: Logger, config: Config):
        self.logger: Logger = logger
        self.config: Config = config

    def write(self, info: ExtractedInfo):
        pass

    def close(self):
        pass
