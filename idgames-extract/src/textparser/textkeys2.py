from enum import Enum

from game import Game


class KeyType(Enum):
    TEXT = 'text'
    BOOL = 'bool'
    GAME = 'game'
    DIFFICULTY = 'difficulty'
    MAP_NUMBER = 'map_number'


TEXT_KEYS = {
    'author': {
        'type': KeyType.TEXT,
        'keys': {'author'},
    },
    'title': {
        'type': KeyType.TEXT,
        'keys': {'title'},
    },
    'game_style_singleplayer': {
        'type': KeyType.BOOL,
        'keys': {'singleplayer'},
    },
    'based_on': {
        'type': KeyType.TEXT,
        'keys': {'base'},
    },
    'description': {
        'type': KeyType.TEXT,
        'keys': {'description'},
    },
    'difficulty_levels': {
        'type': KeyType.DIFFICULTY,
        'keys': {'difficulty settings'},
    },
    'game_style_cooperative': {
        'type': KeyType.BOOL,
        'keys': {'cooperative 2-4 player'},
    },
    'game_style_deathmatch': {
        'type': KeyType.BOOL,
        'keys': {'deathmatch 2-4 player'},
    },
    'tools_used': {
        'type': KeyType.TEXT,
        'keys': {'editor(s) used'},
    },
    'email': {
        'type': KeyType.TEXT,
        'keys': {'email address'},
    },
    'known_bugs': {
        'type': KeyType.TEXT,
        'keys': {'known bugs'},
    },
    'copyright': {
        'type': KeyType.TEXT,
        'keys': {'copyright / permissions'},
    },
    'filename': {
        'type': KeyType.TEXT,
        'keys': {'filename'},
    },
    'credits': {
        'type': KeyType.TEXT,
        'keys': {'additional credits to'},
    },
    'build_time': {
        'type': KeyType.TEXT,
        'keys': {'build time'},
    },
    'author_info': {
        'type': KeyType.TEXT,
        'keys': {'misc. author info'},
    },
    'other': {
        'type': KeyType.TEXT,
        'keys': {'other'},
    },
    'game': {
        'type': KeyType.GAME,
        'keys': {'game'},
    },
    'content_graphics': {
        'type': KeyType.BOOL,
        'keys': {'new graphics'},
    },
    'content_sounds': {
        'type': KeyType.BOOL,
        'keys': {'new sounds'},
    },
    'music': {
        'type': KeyType.TEXT,
        'keys': {'music'},
    },
    'map_number': {
        'type': KeyType.MAP_NUMBER,
        'keys': {'map #'},
    },
    'content_demos': {
        'type': KeyType.BOOL,
        'keys': {'demos replaced'},
    },
    'content_music': {
        'type': KeyType.BOOL,
        'keys': {'new music'},
    },
}

TEXT_GAMES = {
    # Game.DOOM2: {
    #     'keys': {'doom 2'},
    #     're': ['any doom.*', '^doom 2.*', '.*doom2.*', '.*doom2\.wad.*', '.*doom ii.*'],
    # },
}

TEXT_DIFFICULTY = {
    # Game.DOOM2: {
    #     'keys': {'doom 2'},
    #     're': ['any doom.*', '^doom 2.*', '.*doom2.*', '.*doom2\.wad.*', '.*doom ii.*'],
    # },
}

TEXT_MAP_NUMBER = {
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
