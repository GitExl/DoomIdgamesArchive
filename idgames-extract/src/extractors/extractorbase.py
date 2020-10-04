from utils.logger import Logger


class ExtractorBase:

    def __init__(self, logger: Logger, config: dict):
        self.logger: Logger = logger
        self.config: dict = config

    def extract(self, info: dict) -> dict:
        pass
