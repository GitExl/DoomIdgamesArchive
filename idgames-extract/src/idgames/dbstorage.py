from pathlib import Path
from typing import Optional

from mysql.connector import MySQLConnection, connection

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

        # Update (remove + re-add) authors.
        self.cursor.execute('DELETE FROM entry_authors WHERE entry_id=%s', (entry.id,))
        known_author_ids = set()
        for author_name in entry.authors:
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

        self.db.commit()

        return entry.id

    def remove_orphan_authors(self):
        self.cursor.execute('DELETE FROM author WHERE id NOT IN (SELECT author_id FROM entry_authors)')
        self.db.commit()
