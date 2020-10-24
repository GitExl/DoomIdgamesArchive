from struct import Struct
from typing import Optional, Tuple

from doom.level import Level, Line, Sector, Side, Thing, Vertex
from doom.levelfinder import LevelData, LevelFormat
from doom.levelreaderbase import LevelReaderBase


STRUCT_VERTEX: Struct = Struct('<hh')

STRUCT_LINE_DOOM: Struct = Struct('<HHHHHHH')
STRUCT_LINE_HEXEN: Struct = Struct('<HHHBBBBBBHH')

STRUCT_SIDE: Struct = Struct('<hh8s8s8sh')

STRUCT_SECTOR: Struct = Struct('<hh8s8shhh')

STRUCT_THING_DOOM: Struct = Struct('<hhHHH')
STRUCT_THING_HEXEN: Struct = Struct('<HhhhHHHBBBBBB')


def unpack_vertex(values: Tuple):
    return Vertex(float(values[0]), float(values[1]))


def unpack_line_doom(values: Tuple):
    return Line(values[0], values[1], values[5], values[6], values[2], values[3], values[4], (0, 0, 0, 0, 0))


def unpack_line_hexen(values: Tuple):
    return Line(
        values[0], values[1], values[9], values[10], values[2], values[3],
        0, (values[4], values[5], values[6], values[7], values[8]),
    )


def unpack_side(values: Tuple):
    return Side(values[5], values[2], values[4], values[3], values[0], values[1])


def unpack_sector(values: Tuple):
    return Sector(values[0], values[1], values[2], values[3], values[6], values[5], values[4])


def unpack_thing_doom(values: Tuple):
    return Thing(float(values[0]), float(values[1]), values[2], values[3], values[4], 0, 0, 0, (0, 0, 0, 0, 0))


def unpack_thing_hexen(values: Tuple):
    return Thing(
        float(values[1]), float(values[2]), values[4], values[5], values[6], float(values[3]), values[0], values[7],
        (values[8], values[9], values[10], values[11], values[12]),
    )


class BinaryLevelReader(LevelReaderBase):

    def read(self, level_data: LevelData) -> Optional[Level]:
        level_name: str = level_data.header_file.name

        vertices = BinaryLevelReader._read_binary_data(level_data, 'VERTEXES', unpack_vertex, STRUCT_VERTEX)
        sides = BinaryLevelReader._read_binary_data(level_data, 'SIDEDEFS', unpack_side, STRUCT_SIDE)
        sectors = BinaryLevelReader._read_binary_data(level_data, 'SECTORS', unpack_sector, STRUCT_SECTOR)

        if level_data.format == LevelFormat.DOOM:
            lines = BinaryLevelReader._read_binary_data(level_data, 'LINEDEFS', unpack_line_doom, STRUCT_LINE_DOOM)
            things = BinaryLevelReader._read_binary_data(level_data, 'THINGS', unpack_thing_doom, STRUCT_THING_DOOM)
        else:
            lines = BinaryLevelReader._read_binary_data(level_data, 'LINEDEFS', unpack_line_hexen, STRUCT_LINE_HEXEN)
            things = BinaryLevelReader._read_binary_data(level_data, 'THINGS', unpack_thing_hexen, STRUCT_THING_HEXEN)

        return Level(level_name, vertices, lines, sides, sectors, things)

    @staticmethod
    def _read_binary_data(level_data: LevelData, file_name: str, unpack_func, data_struct: Struct):
        file = level_data.files.get(file_name)
        if file is None:
            return []
        data = file.get_data()

        # Trim any extraneous data, iter_unpack will not accept it.
        if len(data) % data_struct.size != 0:
            aligned = data_struct.size * int(len(data) / data_struct.size)
            data = data[0:aligned]

        items = []
        for unpacked in data_struct.iter_unpack(data):
            items.append(unpack_func(unpacked))

        return items
