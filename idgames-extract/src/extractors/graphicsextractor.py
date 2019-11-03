from io import BytesIO
from math import ceil
from typing import Optional

from PIL import Image

from archives.archivefilebase import ArchiveFileBase
from extractors.extractorbase import ExtractorBase
from doomimage import DoomImage
from palette import Palette


class GraphicsExtractor(ExtractorBase):

    TITLE_GRAPHICS = ['titlepic', 'title', 'interpic', 'bossback', 'endpic', 'pfub1', 'victory2', 'pfub2', 'wimap0', 'wimap1', 'wimap2']

    def extract(self, info: dict) -> dict:
        archive_list = info.get('archive_list', None)
        if not archive_list:
            self.logger.warn('Cannot extract graphics without an archive list.')
            return {}

        playpal = archive_list.file_find_basename('playpal')
        if not playpal:
            self.logger.error('No PLAYPAL lump in archive list.')
            return {}
        palette = Palette.from_playpal_data(playpal.get_data())

        for filename in GraphicsExtractor.TITLE_GRAPHICS:
            file = archive_list.file_find_basename(filename, include_main=False)
            if not file:
                continue

            graphic = self.read_graphic(file, palette)
            if graphic:
                return {
                    'graphics': {
                        'main': graphic,
                    }
                }

        return {}

    def read_graphic(self, file: ArchiveFileBase, palette: Palette) -> Optional[Image.Image]:
        image = None

        # Attempt to identify the file looking for PNG or partial JPEG magic bytes.
        data = file.get_data()
        if data[0:8] == b'\x89\x50\x4E\x47\x0D\x0A\x1A\x0A' or data[0:3] == b'\xFF\xD8\xFF':
            try:
                image = Image.open(BytesIO(data))
            except IOError:
                image = None

        elif DoomImage.is_valid(data):
            doom_image = DoomImage.from_data(data, palette)
            image = doom_image.get_pillow_image()

        # File sizes for planar 320x200 and 640x480 match Heretic and Hexen screens.
        elif file.size == 320 * 200:
            image = self.read_raw_graphic(320, 200, data, palette)
        elif file.size == 640 * 480:
            image = self.read_raw_graphic(640, 480, data, palette)

        # Apply 4:3 aspect ratio correction, to 1.6 aspect ratio screens. Increase height by 20% and double the size
        # with nearest interpolation to retain a crispy look without too many scaling artifacts for nearest filtering.
        # TODO: Let the app do the scaling when rendering, saving on bandwidth and memory.
        # if image:
        #     width, height = image.width, image.height
        #     aspect_ratio = width / height
        #     if aspect_ratio == 1.6:
        #         width *= 2
        #         height = ceil(height * 2.4)
        #         image = image.resize((width, height), Image.NEAREST)

        if not image:
            self.logger.stream('unknown_graphics_format', 'Cannot identify or read {} in {}'.format(file.name, file.owner.file.name))
            self.logger.warn('Graphics data is of unknown type.')

        return image

    @staticmethod
    def read_raw_graphic(width: int, height: int, data: bytes, palette: Palette) -> Image.Image:
        image = Image.frombytes('P', (width, height), data)
        image.putpalette(palette.raw)
        return image.convert('RGB')
