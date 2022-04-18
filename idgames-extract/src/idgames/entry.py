from typing import Dict, Optional

from idgames.engine import Engine
from idgames.game import Game


INT_TO_GAME: Dict[int, Game] = {
    0: Game.DOOM,
    1: Game.DOOM2,
    2: Game.TNT,
    3: Game.PLUTONIA,
    4: Game.HERETIC,
    5: Game.HEXEN,
    6: Game.STRIFE,
    7: Game.CHEX,
    8: Game.HACX,
}

GAME_TO_INT: Dict[Game, int] = {
    Game.DOOM: 0,
    Game.DOOM2: 1,
    Game.TNT: 2,
    Game.PLUTONIA: 3,
    Game.HERETIC: 4,
    Game.HEXEN: 5,
    Game.STRIFE: 6,
    Game.CHEX: 7,
    Game.HACX: 8,
}

INT_TO_ENGINE: Dict[int, Engine] = {
    0: Engine.UNKNOWN,
    1: Engine.DOOM,
    2: Engine.HERETIC,
    3: Engine.HEXEN,
    4: Engine.STRIFE,
    5: Engine.NOLIMITS,
    6: Engine.BOOM,
    7: Engine.MBF,
    8: Engine.ZDOOM,
    9: Engine.GZDOOM,
    10: Engine.LEGACY,
    11: Engine.SKULLTAG,
    12: Engine.ZDAEMON,
    13: Engine.DOOMSDAY,
    14: Engine.EDGE,
    15: Engine.ETERNITY,
    16: Engine.DOOMRETRO,
    17: Engine.ZANDRONUM,
}

ENGINE_TO_INT: Dict[Engine, int] = {
    Engine.UNKNOWN: 0,
    Engine.DOOM: 1,
    Engine.HERETIC: 2,
    Engine.HEXEN: 3,
    Engine.STRIFE: 4,
    Engine.NOLIMITS: 5,
    Engine.BOOM: 6,
    Engine.MBF: 7,
    Engine.ZDOOM: 8,
    Engine.GZDOOM: 9,
    Engine.LEGACY: 10,
    Engine.SKULLTAG: 11,
    Engine.ZDAEMON: 12,
    Engine.DOOMSDAY: 13,
    Engine.EDGE: 14,
    Engine.ETERNITY: 15,
    Engine.DOOMRETRO: 16,
    Engine.ZANDRONUM: 17,
}


class Entry:

    def __init__(self, path: str, file_modified: int, entry_updated: int):
        self.path: str = path
        self.file_modified: int = file_modified
        self.entry_updated: int = entry_updated

        self.id: Optional[int] = None

        self.title: Optional[str] = None
        self.game: Optional[Game] = None
        self.engine: Optional[Engine] = None

    def __repr__(self):
        return '{}, {}: {}'.format(self.id, self.path, self.title)

    def to_row(self) -> Dict[str, any]:
        game = None
        if self.game in GAME_TO_INT:
            game = GAME_TO_INT.get(self.game)

        engine = None
        if self.engine in ENGINE_TO_INT:
            engine = ENGINE_TO_INT.get(self.engine)

        return {
            'path': self.path,
            'file_modified': self.file_modified,
            'entry_updated': self.entry_updated,
            'title': self.title,
            'game': game,
            'engine': engine,
        }

    @staticmethod
    def from_row(row: Dict):
        game: Game = Game.UNKNOWN
        if row['game'] in INT_TO_GAME:
            game = INT_TO_GAME.get(row['game'])

        engine: Engine = Engine.UNKNOWN
        if row['engine'] in INT_TO_ENGINE:
            engine = INT_TO_ENGINE.get(row['engine'])

        entry = Entry(
            row['path'],
            row['file_modified'],
            row['entry_updated']
        )
        entry.id = row['id']
        entry.title = row['title']
        entry.game = game
        entry.engine = engine

        return entry
