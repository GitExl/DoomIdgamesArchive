from utils.logger import Logger


class WriterBase:

    def __init__(self, logger: Logger, config: dict):
        self.logger: Logger = logger
        self.config: dict = config

    def write(self, info: dict):
        pass
