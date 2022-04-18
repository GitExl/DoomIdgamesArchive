from pathlib import Path
from typing import Optional, List

from mysql.connector import MySQLConnection, connection

from doom.level import Level, LEVEL_FORMAT_TO_INT
from idgames.entry import Entry
from utils.config import Config


class DBStorage:

    def __init__(self, config: Config):
        self.config: Config = config

        self.db: MySQLConnection = connection.MySQLConnection(
            user=config.get('db.user'),
            password=config.get('db.password'),
            host=config.get('db.host'),
            database=config.get('db.database')
        )
        self.cursor = self.db.cursor()

    def close(self):
        self.cursor.close()
        self.db.close()

    def get_entry_by_path(self, path: Path) -> Optional[Entry]:
        self.cursor.execute('SELECT * FROM entry WHERE path=%s LIMIT 1', (path.as_posix(),))

        row = self.cursor.fetchone()
        if row is None:
            return None

        return Entry.from_row(dict(zip(self.cursor.column_names, row)))

    def save_entry(self, entry: Entry) -> int:
        row = entry.to_row()

        args = list(row.values())
        if entry.id is not None:
            args.append(entry.id)
            set_stmt = ['{}=%s'.format(key) for key in row.keys()]
            query = 'UPDATE entry SET {} WHERE id=%s'.format(','.join(set_stmt))
        else:
            col_names = row.keys()
            value_subs = ['%s'] * len(row)
            query = 'INSERT INTO entry ({}) VALUES ({})'.format(','.join(col_names), ','.join(value_subs))
        self.cursor.execute(query, args)

        if entry.id is None:
            entry.id = self.cursor.lastrowid

        return entry.id

    def save_entry_authors(self, entry: Entry, authors: List[str]):

        # Update (remove + re-add) authors.
        self.cursor.execute('DELETE FROM entry_authors WHERE entry_id=%s', (entry.id,))

        known_author_ids = set()
        for author_name in authors:
            self.cursor.execute('SELECT id FROM author WHERE name=%s LIMIT 1', (author_name,))
            author_row = self.cursor.fetchone()
            if author_row is not None:
                author_id = author_row[0]

                # In some cases the same author can appear multiple times.
                if author_id in known_author_ids:
                    continue

            else:
                self.cursor.execute('INSERT INTO author (name) VALUES (%s)', (author_name,))
                author_id = self.cursor.lastrowid

            self.cursor.execute('INSERT INTO entry_authors VALUES (%s, %s)', (entry.id, author_id))
            known_author_ids.add(author_id)

    def save_entry_levels(self, entry: Entry, levels: List[Level]):
        self.cursor.execute('DELETE FROM entry_levels WHERE entry_id=%s', (entry.id,))

        for level in levels:
            self.cursor.execute(
                'INSERT INTO entry_levels (entry_id, name, format, line_count, side_count, thing_count, sector_count) VALUES (%s, %s, %s, %s, %s, %s, %s)',
                (entry.id, level.name, LEVEL_FORMAT_TO_INT.get(level.format), len(level.lines), len(level.sides), len(level.things), len(level.sectors))
            )

    def remove_orphan_authors(self):
        self.cursor.execute('DELETE FROM author WHERE id NOT IN (SELECT author_id FROM entry_authors)')

    def remove_orphan_levels(self):
        self.cursor.execute('DELETE FROM entry_levels WHERE entry_id NOT IN (SELECT id FROM entry)')

    def remove_dead_entries(self, existing_paths: List[Path]):
        local_paths = set()
        for path_local in existing_paths:
            local_paths.add(path_local.relative_to(self.config.get('paths.idgames')).as_posix())

        self.cursor.execute('SELECT id, path FROM entry')
        path_rows = self.cursor.fetchall()
        db_paths = dict((path, id) for (id, path) in path_rows)

        for db_path, db_id in db_paths.items():
            if db_path in local_paths:
                continue

            self.cursor.execute('DELETE FROM entry WHERE id=%s', (db_id,))

    def commit(self):
        self.db.commit()