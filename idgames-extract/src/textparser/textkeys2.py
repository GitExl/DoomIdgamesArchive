from enum import Enum

from game import Game


class KeyType(Enum):
    TEXT = 'text'
    BOOL = 'bool'
    GAME = 'game'


TEXT_KEYS = {
    # 'title': {
    #     'type': KeyType.TEXT,
    #     'keys': {'title'},
    #     're': ['^dehack.*'],
    #     'array': True,
    # },
}


TEXT_GAMES = {
    # Game.DOOM2: {
    #     'keys': {'doom 2'},
    #     're': ['any doom.*', '^doom 2.*', '.*doom2.*', '.*doom2\.wad.*', '.*doom ii.*'],
    # },
 }

TEXT_BOOLEAN = {
    'true': {
        'keys': {},
        're': [],
    },
    'false': {
        'keys': {},
        're': [],
    },
}