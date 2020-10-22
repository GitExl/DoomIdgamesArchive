from typing import Optional


class ArchiveFileBase:

    def __init__(self, owner, name: str, size: int):
        self.name: str = name
        self.size: int = size
        self.owner = owner

        self.data: Optional[bytes] = None

    def get_data(self) -> bytes:
        if self.data is None:
            self.owner.logger.debug('Reading "{}" from "{}"'.format(self.name, self.owner.name))
            self.data = self.owner.get_file_data(self)

        return self.data
