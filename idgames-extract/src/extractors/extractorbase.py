from utils.config import Config
from utils.logger import Logger


class ExtractorBase:

    def __init__(self, logger: Logger, config: Config):
        self.logger: Logger = logger
        self.config: Config = config

    def extract(self, info: dict) -> dict:
        pass
