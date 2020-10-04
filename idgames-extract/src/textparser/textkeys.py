from enum import Enum

from idgames.game import Game


class KeyType(Enum):
    TEXT = 'text'
    BOOL = 'bool'
    GAME = 'game'
    DIFFICULTY = 'difficulty'
    MAP_NUMBER = 'map_number'
    DATETIME = 'datetime'
    INTEGER = 'integer'
    GAME_STYLE = 'game_style'
    ENGINE = 'engine'


TEXT_KEYS = {
    'author': {
        'type': KeyType.TEXT,
        'keys': {'author', 'authors', 'author(s)'},
    },
    'title': {
        'type': KeyType.TEXT,
        'keys': {'title', 'level name', 'name'},
    },
    'game_style_singleplayer': {
        'type': KeyType.BOOL,
        'keys': {'single player'},
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
        'keys': {'deathmatch 2-4 player', 'deathmatch'},
    },
    'tools_used': {
        'type': KeyType.TEXT,
        'keys': {'editor(s) used', 'editors used', 'editor used'},
    },
    'email': {
        'type': KeyType.TEXT,
        'keys': {'email address', 'e-mail', 'e-mail address', 'email'},
    },
    'known_bugs': {
        'type': KeyType.TEXT,
        'keys': {'known bugs', 'bugs'},
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
        'keys': {'additional credits to', 'additional credits', 'credits'},
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
        'keys': {'game', 'required game', 'game version required', 'iwad needed'},
    },
    'content_graphics': {
        'type': KeyType.BOOL,
        'keys': {'new graphics', 'graphics', 'graphic addon only'},
    },
    'content_sounds': {
        'type': KeyType.BOOL,
        'keys': {'new sounds', 'sounds', 'sound pwad only'},
    },
    'music': {
        'type': KeyType.TEXT,
        'keys': {'music'},
    },
    'map_number': {
        'type': KeyType.MAP_NUMBER,
        'keys': {'map #', 'episode and level #', 'level #', 'level', 'map', 'map number'},
    },
    'content_demos': {
        'type': KeyType.BOOL,
        'keys': {'demos replaced', 'demos', '.lmp only'},
    },
    'content_music': {
        'type': KeyType.BOOL,
        'keys': {'new music', 'music pwad only'},
    },
    'date_released': {
        'type': KeyType.DATETIME,
        'keys': {'release date', 'date'},
    },
    'count_levels': {
        'type': KeyType.INTEGER,
        'keys': {'new levels'},
    },
    'content_dehacked': {
        'type': KeyType.BOOL,
        'keys': {'dehacked/bex patch'},
    },
    'game_style_primary': {
        'type': KeyType.GAME_STYLE,
        'keys': {'primary purpose'},
    },
    'game_style_other': {
        'type': KeyType.GAME_STYLE,
        'keys': {'other game styles'},
    },
    'other_files_required': {
        'type': KeyType.TEXT,
        'keys': {'other files required', 'required to have in dir'},
    },
    'author_other_files': {
        'type': KeyType.TEXT,
        'keys': {'other files by author'},
    },
    'engine': {
        'type': KeyType.ENGINE,
        'keys': {'advanced engine needed'},
    },
    'links': {
        'type': KeyType.TEXT,
        'keys': {'the usual', 'ftp sites', 'web sites', 'bbs numbers'},
    },
    'do_not_run_with': {
        'type': KeyType.TEXT,
        'keys': {'may not run with...', 'may not run with'},
    },
    'archive_maintainer': {
        'type': KeyType.TEXT,
        'keys': {'archive maintainer'},
    },
    'tested_with': {
        'type': KeyType.TEXT,
        'keys': {'tested with'},
    },
    'update_to': {
        'type': KeyType.TEXT,
        'keys': {'update to'},
    },
    'where_to_get': {
        'type': KeyType.TEXT,
        'keys': {'where to get this wad'},
    },
    'date_completed': {
        'type': KeyType.DATETIME,
        'keys': {'date finished'},
    },
    'notes': {
        'type': KeyType.TEXT,
        'keys': {'note', 'notes'},
    },
    'content_levels': {
        'type': KeyType.BOOL,
        'keys': {'new level wad'},
    },
    'review': {
        'type': KeyType.TEXT,
        'keys': {'review'},
    },
    'story': {
        'type': KeyType.TEXT,
        'keys': {'story'},
    },
    'theme': {
        'type': KeyType.TEXT,
        'keys': {'theme'},
    },
    'inspiration': {
        'type': KeyType.TEXT,
        'keys': {'inspiration'},
    },
    'comments': {
        'type': KeyType.TEXT,
        'keys': {'comments'},
    },
}

TEXT_GAMES = {
    Game.DOOM2: {
        'keys': {'doom 2', 'doom2', 'doom ii', 'doomii', 'doom 2 ver 1.9', 'doom ][', 'doom / doom2', 'doom/doom2', 'any doom', '- doom2', '2', 'any', 'both', 'freedoom',' freedm', 'ii', 'any iwad', 'freedoom: phase 2'},
        're': ['any doom.*', '^doom 2.*', '.*doom2.*', r'.*doom2\.wad.*', '.*doom ii.*', r'\bdoom\b', '^any game'],
    },
    Game.DOOM: {
        'keys': {'doom', 'doom1', '(ultimate) doom', 'the ultimate doom', 'freedoom1'},
        're': ['^doom.*', '.* ultimate .*', r'doom\.wad', '.*ultimate doom.*', '.*doom i.*'],
    },
    Game.PLUTONIA: {
        'keys': {'final doom', 'the plutonia experiment'},
        're': ['^final doom.*', '^plutonia.*'],
    },
    Game.TNT: {
        're': ['^tnt.*'],
    },
    Game.HERETIC: {
        're': ['.*heretic.*'],
    },
    Game.HEXEN: {
        're': ['.*hexen.*'],
    },
    Game.STRIFE: {
        're': ['.*strife.*'],
    },
    Game.CHEX: {
        'keys': {'chex', 'chex quest', 'chex quest 3'},
    },
    Game.HACX: {
        're': ['^hacx.*'],
    }
}

TEXT_DIFFICULTY = {
    'true': {
        'keys': {'yes', 'y', 'supported', 'possibly', 'of course', 'any', 'all', 'full implementation', 'fully functional.', 'fully supported', 'some'},
        're': ['^yes', '^yup', '^yep', '^yeah', r'^skills\s', r'^skill\s', '^only', '^implemented', r'^all\b', '^definitely', '^fully implemented', '^some\b']
    },
    'false': {
        'keys': {'not implented', 'no', 'not implimented', '-', 'n\\a', 'unknown', 'n', 'not really', 'na', 'not applicable'},
        're': ['^not implemented', '^nope', '^none', r'^no\b', '^nah', '^n/a', r'^not\b']
    },
}

TEXT_MAP_NUMBER = {
    # Game.DOOM2: {
    #     'keys': {'doom 2'},
    #     're': ['any doom.*', '^doom 2.*', '.*doom2.*', '.*doom2\.wad.*', '.*doom ii.*'],
    # },
}

TEXT_BOOLEAN = {
    'true': {
        'keys': {'designed for', 'all', 'any', 'yeah!', 'yeah', 'affirmative', 'certainly', 'some', 'y', '- yes',
                 'one'},
        're': ['^yea.*', '^oh yeah.*', '^oh yes.*', '^supported.*', '^starts.*', '^yes.*', '^sure.*', '^probably.*',
               '^absolutely.*', '^definitely.*', '^some .*', '^duh.*', '^hell (yes|yeah).*', '^i guess.*',
               '^implemented.*', '^of course.*', '^yep.*', '^yup.*', '^a .*', '^all .*', '^designed.*', '^fully .*',
               '^full .*', '^you bet.*', '.*\(yes\).*', '^aye.*'],
    },
    'false': {
        'keys': {'n/a', '-', '- no', '0'},
        're': ['^no.*'],
    },
}
