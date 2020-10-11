from enum import Enum

from idgames.engine import Engine
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
        'keys': {'author', 'authors', 'author(s)', 'autor', 'original author'},
    },
    'title': {
        'type': KeyType.TEXT,
        'keys': {'title', 'level name', 'name'},
    },
    'game_style_singleplayer': {
        'type': KeyType.BOOL,
        'keys': {'single player', 'singleplayer'},
    },
    'based_on': {
        'type': KeyType.TEXT,
        'keys': {'base', 'idea base'},
    },
    'description': {
        'type': KeyType.TEXT,
        'keys': {'description', 'instructions', 'play information', 'wad description'},
    },
    'difficulty_levels': {
        'type': KeyType.DIFFICULTY,
        'keys': {'difficulty settings', 'difficulty'},
    },
    'game_style_cooperative': {
        'type': KeyType.BOOL,
        'keys': {'cooperative 2-4 player', 'cooperative', 'cooperative 2-8 player', 'co-op', 'coop 2-4 player', 'coop'},
    },
    'game_style_deathmatch': {
        'type': KeyType.BOOL,
        'keys': {'deathmatch 2-4 player', 'deathmatch', 'deathmatch 2-8 player'},
    },
    'tools_used': {
        'type': KeyType.TEXT,
        'keys': {
            'editor(s) used', 'editors used', 'editor used', 'tools used', 'editors', 'utilities used', 'editor',
            'main editor(s) used', 'tools(s) used',
        },
    },
    'email': {
        'type': KeyType.TEXT,
        'keys': {'email address', 'e-mail', 'e-mail address', 'email'},
    },
    'known_bugs': {
        'type': KeyType.TEXT,
        'keys': {'known bugs', 'bugs', 'unknown bugs'},
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
        'keys': {
            'additional credits to', 'additional credits', 'credits', 'credits to', 'special thanks to', 'thanks to',
            'big thanks to', 'special thanks', 'additional credit to',
        },
    },
    'build_time': {
        'type': KeyType.TEXT,
        'keys': {'build time', 'time taken', 'construction time', 'time', 'building time'},
    },
    'author_info': {
        'type': KeyType.TEXT,
        'keys': {'misc. author info', 'misc author info', 'author info', 'misc. developer info'},
    },
    'other': {
        'type': KeyType.TEXT,
        'keys': {'other'},
    },
    'game': {
        'type': KeyType.GAME,
        'keys': {'game', 'required game', 'game version required', 'iwad needed', 'doom version'},
    },
    'content_graphics': {
        'type': KeyType.BOOL,
        'keys': {
            'new graphics', 'graphics', 'graphic addon only', 'sprites', 'textures', 'sprite edit', 'new sprites',
            'new textures',
        },
    },
    'content_sounds': {
        'type': KeyType.BOOL,
        'keys': {'new sounds', 'sounds', 'sound pwad only'},
    },
    'music': {
        'type': KeyType.TEXT,
        'keys': {'music', 'music track'},
    },
    'map_number': {
        'type': KeyType.MAP_NUMBER,
        'keys': {'map #', 'episode and level #', 'level #', 'level', 'map', 'map number'},
    },
    'content_demos': {
        'type': KeyType.BOOL,
        'keys': {'demos replaced', 'demos', '.lmp only', 'new demos'},
    },
    'content_music': {
        'type': KeyType.BOOL,
        'keys': {'new music', 'music pwad only', 'midi', 'new musics'},
    },
    'date_released': {
        'type': KeyType.DATETIME,
        'keys': {'release date', 'date', 'date released', 'date of release'},
    },
    'content_dehacked': {
        'type': KeyType.BOOL,
        'keys': {'dehacked/bex patch', 'dehack patch only'},
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
        'keys': {'advanced engine needed', 'source port', 'engine', 'port'},
    },
    'links': {
        'type': KeyType.TEXT,
        'keys': {
            'the usual', 'ftp sites', 'web sites', 'bbs numbers', 'homepage', 'web page', 'ftp', 'website',
            'web site', 'home page', 'www', 'bbs', 'internet', 'web', 'www sites'
        },
    },
    'do_not_run_with': {
        'type': KeyType.TEXT,
        'keys': {'may not run with...', 'may not run with', 'will not run with...'},
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
        'keys': {'date finished', 'date completed'},
    },
    'content_levels': {
        'type': KeyType.BOOL,
        'keys': {'new level wad', 'new levels', 'levels', 'levels replaced'},
    },
    'review': {
        'type': KeyType.TEXT,
        'keys': {'review'},
    },
    'story': {
        'type': KeyType.TEXT,
        'keys': {'story', 'the story'},
    },
    'theme': {
        'type': KeyType.TEXT,
        'keys': {'theme', 'themes'},
    },
    'inspiration': {
        'type': KeyType.TEXT,
        'keys': {'inspiration'},
    },
    'comments': {
        'type': KeyType.TEXT,
        'keys': {
            'comments', 'author\'s comment', 'info', 'author\'s comments', 'note', 'notes', 'additional notes',
            'uploader\'s note', 'important notes', 'play notes', 'misc. info', 'things to look out for',
            'additional info', 'comment', 'misc game info', 'misc notes', 'author comments',
        },
    },
    'hints': {
        'type': KeyType.TEXT,
        'keys': {'hints', 'tips', 'hint'},
    },
    'content_decorate': {
        'type': KeyType.TEXT,
        'keys': {'decorate'},
    },
    'content_weapons': {
        'type': KeyType.TEXT,
        'keys': {'weapons', 'new weapons'},
    },
}

TEXT_GAMES = {
    Game.DOOM2: {
        'keys': {
            'doom 2', 'doom2', 'doom ii', 'doomii', 'doom 2 ver 1.9', 'doom ][', 'doom / doom2', 'doom/doom2',
            'any doom', '- doom2', '2', 'any', 'both', 'freedoom',' freedm', 'ii', 'any iwad', 'freedoom: phase 2',
        },
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
        'keys': {'n/a', '-', '- no', '0', 'nah'},
        're': ['^no.*'],
    },
}

TEXT_ENGINE = {
    Engine.ZDOOM: {
        're': [r'zdoom'],
    },
    Engine.VANILLA: {
        'keys': {
            'none', 'no', 'none.', 'doom2', 'vanilla', '-', 'doom 2', 'nope', 'any', 'n/a', 'vanilla compatible',
            'doom2.exe', 'vanilla-compatible', 'vanilla doom', 'none required',
        },
    },
    Engine.BOOM: {
        'keys': {},
        're': [r'boom'],
    },
    Engine.GZDOOM: {
        'keys': {},
        're': [r'gzdoom'],
    },
    Engine.SKULLTAG: {
        'keys': {'skulltag'},
    },
    Engine.NOLIMITS: {
        'keys': {'limit removing', 'limit-removing', 'limit removing port', 'yes', 'limit-removing port'},
    },
    Engine.PRBOOM: {
        'keys': {'prboom+', 'prboom-plus -complevel 2'},
    },
    Engine.LEGACY: {
        'keys': {'legacy', 'doom legacy'},
    },
    Engine.ZANDRONUM: {
        'keys': {'zandronum'},
    },
    Engine.ZDAEMON: {
        'keys': {'zdaemon'},
    },
}
