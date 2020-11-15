from typing import Dict, List, Optional

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


class Entry:

    def __init__(self, path: str, file_modified: int, entry_updated: int):
        self.path: str = path
        self.file_modified: int = file_modified
        self.entry_updated: int = entry_updated

        self.id: Optional[int] = None

        self.title: Optional[str] = None
        self.game: Optional[Game] = None
        self.authors: List[str] = []

    def __repr__(self):
        return '{}, {}: {}'.format(self.id, self.path, self.title)

    def to_row(self) -> Dict[str, any]:
        game = None
        if self.game in GAME_TO_INT:
            game = GAME_TO_INT.get(self.game)

        return {
            'path': self.path,
            'file_modified': self.file_modified,
            'entry_updated': self.entry_updated,
            'title': self.title,
            'game': game
        }

    @staticmethod
    def from_row(row: Dict):
        game: Game = Game.UNKNOWN
        if row['game'] in INT_TO_GAME:
            game = INT_TO_GAME.get(row['game'])

        entry = Entry(
            row['path'],
            row['file_modified'],
            row['entry_updated']
        )
        entry.id = row['id']
        entry.title = row['title']
        entry.game = game

        return entry
