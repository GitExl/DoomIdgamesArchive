from logger import Logger


class ExtractorBase:

    def __init__(self, logger: Logger):
        self.logger: Logger = logger

    def extract(self, info: dict) -> dict:
        pass
