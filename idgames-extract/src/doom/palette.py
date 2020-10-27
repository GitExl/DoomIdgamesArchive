from dataclasses import dataclass
from typing import List


@dataclass(frozen=True)
class Color:
    __slots__ = ['r', 'g', 'b']

    r: int
    g: int
    b: int


class Palette:

    def __init__(self):
        self.colors: List[Color] = []
        self.raw: List[int] = []

    @classmethod
    def from_playpal_data(cls, data: bytes):
        if len(data) < 768:
            raise Exception('Not enough data for a 256 RGB color palette.')

        palette = cls()
        palette.raw.extend(data[0:768])

        offset = 0
        while offset < 768:
            palette.colors.append(Color(data[offset], data[offset + 1], data[offset + 2]))
            offset += 3

        return palette
