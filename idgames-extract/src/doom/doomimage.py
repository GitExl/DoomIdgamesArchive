from struct import Struct
from typing import Optional

from PIL import Image

from doom.palette import Palette


class DoomImage(object):
    S_HEADER: Struct = Struct('<HHhh')

    def __init__(self, width: int, height: int, left: int, top: int):
        self.width: int = width
        self.height: int = height
        self.left: int = left
        self.top: int = top

        self.pixels: Optional[bytes] = None

    @classmethod
    def from_data(cls, data: bytes, palette: Palette):
        """
        Creates a DoomImage with doom graphics data rendered to an internal buffer.

        :param data:
        :param palette:
        :return:
        """
        width, height, left, top = DoomImage.S_HEADER.unpack_from(data)

        # Attempt to detect invalid data.
        if width > 2048 or height > 2048 or top > 2048 or left > 2048:
            return None
        if width <= 0 or height <= 0:
            return None

        image = cls(width, height, left, top)

        # Initialize an empty bitmap.
        pixels = bytearray([0, 0, 0] * width * height)

        # Read column offsets.
        offset_struct = Struct('<' + ('I' * width))
        offsets = offset_struct.unpack_from(data[8:8 + (width * 4)])

        # Read columns.
        column_index = 0
        while column_index < width:
            offset = offsets[column_index]

            # Attempt to detect invalid data.
            if offset >= len(data):
                return None

            prev_delta = 0
            while True:
                column_top = data[offset]

                # Column end.
                if column_top == 255:
                    break

                # Tall columns are extended.
                if column_top <= prev_delta:
                    column_top += prev_delta
                prev_delta = column_top

                pixel_count = data[offset + 1]
                offset += 3

                pixel_index = 0
                while pixel_index < pixel_count:
                    if offset + pixel_index >= len(data):
                        break

                    pixel = data[offset + pixel_index]
                    destination = ((pixel_index + column_top) * width + column_index) * 3

                    if destination + 2 < len(pixels):
                        pixels[destination + 0] = palette.colors[pixel].r
                        pixels[destination + 1] = palette.colors[pixel].g
                        pixels[destination + 2] = palette.colors[pixel].b

                    pixel_index += 1

                offset += pixel_count + 1
                if offset >= len(data):
                    break

            column_index += 1

        image.pixels = bytes(pixels)

        return image

    @staticmethod
    def is_valid(data: bytes) -> bool:
        """
        Determine if some data is likely to be a valid Doom type image.

        :param data:
        :return:
        """
        if len(data) < 16:
            return False

        # Verify if the header values are sane.
        width, height, left, top = DoomImage.S_HEADER.unpack_from(data)
        if width > 2048 or height > 2048 or top > 2048 or left > 2048:
            return False
        if width <= 0 or height <= 0:
            return False

        # Verify that offsets are in range of the data.
        offset_struct = Struct('<' + ('I' * width))
        offsets = offset_struct.unpack_from(data[8:8 + (width * 4)])
        for offset in offsets:
            if offset >= len(data):
                return False

        return True

    def get_pillow_image(self) -> Image:
        """
        Returns a Pillow image from this graphic's image data.

        :return:
        """
        return Image.frombytes('RGB', (self.width, self.height), self.pixels)
