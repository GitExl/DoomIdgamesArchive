from logger import Logger


class WriterBase:

    def __init__(self, logger: Logger):
        self.logger: Logger = logger

    def write(self, info: dict):
        pass
