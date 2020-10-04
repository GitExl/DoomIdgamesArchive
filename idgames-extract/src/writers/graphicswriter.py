from os.path import dirname
from pathlib import Path

from writers.writerbase import WriterBase


class GraphicsWriter(WriterBase):

    def write(self, info: dict):
        base_path = self.config['writers']['graphics']['output_path']

        if 'graphics' not in info:
            return

        for key, graphic in info['graphics'].items():
            if graphic is None:
                continue

            path_dir = dirname('{}/{}'.format(base_path, info['path_idgames']))
            path_file = '{}/{}_{}.webp'.format(base_path, info['path_idgames_base'], key)
            Path(path_dir).mkdir(parents=True, exist_ok=True)

            pixel_count = graphic.size[0] * graphic.size[1]
            if pixel_count >= 1024 * 768:
                graphic.save(path_file, method=6, quality=75)
            elif pixel_count >= 800 * 600:
                graphic.save(path_file, method=6, quality=85)
            elif pixel_count >= 640 * 480:
                graphic.save(path_file, method=6, quality=95)
            else:
                graphic.save(path_file, method=6, lossless=True)

            return
