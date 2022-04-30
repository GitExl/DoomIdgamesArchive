from pathlib import Path

from extractors.extractedinfo import ExtractedInfo
from writers.writerbase import WriterBase


class GraphicsWriter(WriterBase):

    def write(self, info: ExtractedInfo):
        base_path = Path(self.config.get('writers.graphics.output_path'))

        for key, graphic in info.graphics.items():
            if graphic is None:
                continue

            path_dir = (base_path / info.path_idgames).parents[0]
            path_file = path_dir / '{}_{}.webp'.format(info.filename_base, key)
            path_dir.mkdir(parents=True, exist_ok=True)

            pixel_count = graphic.size[0] * graphic.size[1]
            if pixel_count >= 1024 * 768:
                graphic.save(path_file, method=6, quality=75)
            elif pixel_count >= 800 * 600:
                graphic.save(path_file, method=6, quality=85)
            elif pixel_count >= 640 * 480:
                graphic.save(path_file, method=6, quality=95)
            else:
                graphic.save(path_file, method=6, lossless=True)
