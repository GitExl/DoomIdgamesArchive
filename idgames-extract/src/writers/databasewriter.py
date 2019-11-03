import sqlite3

from sqlite3 import Connection, Cursor

from logger import Logger
from writers.writerbase import WriterBase


class DatabaseWriter(WriterBase):

    def __init__(self, logger: Logger):
        super().__init__(logger)

        self.db: Connection = sqlite3.connect('idgames.db')
        self.cursor: Cursor = self.db.cursor()
        self.create_tables()

    def write(self, info: dict):
        path = info['path_idgames']

        self.cursor.execute('INSERT INTO `files` (`path`) VALUES (?)', (path,))
        file_id = self.cursor.lastrowid

        if 'graphics' in info:
            for key, image in info['graphics'].items():
                if not image:
                    continue

                path_image = '{}_{}.webp'.format(info['path_idgames_base'], key)
                self.cursor.execute('INSERT INTO `images` (`file_id`, `path`, `width`, `height`) VALUES (?, ?, ?, ?)', (file_id, path_image, image.width, image.height))

        self.db.commit()

    def create_tables(self):
        self.cursor.execute('DROP TABLE IF EXISTS `files`')
        self.cursor.execute('DROP TABLE IF EXISTS `images`')
        self.db.commit()

        # Taken from the schema file generated by Android's Room library.
        self.cursor.execute('CREATE TABLE IF NOT EXISTS `files` (`id` INTEGER NOT NULL, `path` TEXT, PRIMARY KEY(`id`))')
        self.cursor.execute('CREATE TABLE IF NOT EXISTS `images` (`id` INTEGER NOT NULL, `file_id` INTEGER NOT NULL, `path` TEXT, `width` INTEGER NOT NULL, `height` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`file_id`) REFERENCES `files`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )')
        self.db.commit()
