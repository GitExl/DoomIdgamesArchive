from enum import Enum

from game import Game


class KeyType(Enum):
    TEXT = 'text'
    BOOL = 'bool'
    GAME = 'game'


TEXT_KEYS = {
    'filename': {
        'type': KeyType.TEXT,
        'keys': {'filename'},
    },
    'title': {
        'type': KeyType.TEXT,
        'keys': {'title', 'name', 'file title', 'title (once again)', 'wad name'},
    },
    'map_titles': {
        'type': KeyType.TEXT,
        'keys': {'level name'},
        'array': True,
    },
    'authors': {
        'type': KeyType.TEXT,
        'keys': {'author', 'authors', 'author(s)', 'autor', 'author & email', 'co-authors', 'creator', 'developer', 'designer'},
        'array': True,
    },
    'author_info': {
        'type': KeyType.TEXT,
        'keys': {'misc. author info', 'misc author info', 'author info', 'misc. developer info'},
    },
    'emails': {
        'type': KeyType.TEXT,
        're': ['^email.*', '^e-mail.*'],
    },
    'description': {
        'type': KeyType.TEXT,
        'keys': {'description', 'brief description', 'summary', 'wad description'},
    },
    'base': {
        'type': KeyType.TEXT,
        'keys': {'base'},
    },
    'style_single_player': {
        'type': KeyType.BOOL,
        'keys': {'single player', 'singleplayer'},
    },
    'style_deathmatch': {
        'type': KeyType.BOOL,
        're': ['^deathmatch.*', '.* deathmatch', 'deatmatch.*', 'death match.*'],
    },
    'style_coop': {
        'type': KeyType.BOOL,
        're': ['^cooperative.*', '^co-op.*', '^coop.*', '^co-operative.*'],
    },
    'has_difficulties': {
        'type': KeyType.BOOL,
        'keys': {'skill levels', 'skill settings'},
        're': ['^difficulty.*'],
    },
    'story': {
        'type': KeyType.TEXT,
        'keys': {'story'},
    },
    'note': {
        'type': KeyType.TEXT,
        'keys': {'note'},
    },
    'theme': {
        'type': KeyType.TEXT,
        'keys': {'theme', 'themes'},
    },
    'date': {
        'type': KeyType.TEXT,
        'keys': {'date'},
    },
    'date_completed': {
        'type': KeyType.TEXT,
        'keys': {'date finished', 'date completed', 'date of completion', 'finished date'},
    },
    'date_release': {
        'type': KeyType.TEXT,
        'keys': {'date of release', 'date released', 'release date'},
    },
    'bugs': {
        'type': KeyType.TEXT,
        're': ['^bugs.*', '.*bugs$'],
    },
    'style_other': {
        'type': KeyType.TEXT,
        'keys': {'other game styles'},
    },
    'tools': {
        'type': KeyType.TEXT,
        'keys': {'editor(s) used', 'editors used', 'editor used', 'editors', 'tools used', 'editor', 'utilities used', 'main editor(s) used', 'tools(s) used', 'other editor(s) used', 'tool(s) used'},
        'array': True,
    },
    'credits': {
        'type': KeyType.TEXT,
        'keys': {'additional credits to', 'additional credits', 'credits', 'special thanks to', 'credits to', 'beta testers', 'big thanks to', 'playtesters', 'thanks to', 'additional credit to', 'additional thanks', 'many thanks to', 'major credits to'},
    },
    'tested_with': {
        'type': KeyType.TEXT,
        'keys': {'tested with'},
    },
    'do_not_run_with': {
        'type': KeyType.TEXT,
        'keys': {'may not run with...', 'may not run with', 'will not run with...'},
    },
    'build_time': {
        'type': KeyType.TEXT,
        'keys': {'build time', 'construction time', 'time', 'building time', 'design time', 'time spent', 'time taken', 'time to build'},
    },
    'has_levels': {
        'type': KeyType.BOOL,
        'keys': {'new levels', 'new level wad'},
    },
    'has_graphics': {
        'type': KeyType.BOOL,
        'keys': {'new graphics', 'graphics', 'graphic addon only', 'sprites', 'new sprites', 'new textures', 'new graphic'},
    },
    'has_sounds': {
        'type': KeyType.BOOL,
        'keys': {'new sounds', 'sounds', 'sound pwad only'},
    },
    'has_music': {
        'type': KeyType.BOOL,
        'keys': {'new music', 'music', 'music pwad only', 'midi'},
    },
    'has_demos': {
        'type': KeyType.BOOL,
        'keys': {'demos replaced', 'demos', '.lmp only', 'new demos', 'demos included'},
    },
    'has_dehacked': {
        'type': KeyType.BOOL,
        're': ['^dehack.*'],
    },
    'game': {
        'type': KeyType.GAME,
        'keys': {'game', 'game name', 'game type', 'doom version', 'doom game', 'for game', 'required game', 'iwad', 'iwad needed', 'doom-version', 'wadfile to be used with', 'Game Version Required'},
    },
    'levels': {
        'type': KeyType.TEXT,
        'keys': {'map #', 'episode and level #', 'level #', 'map number', 'map', 'level', 'map#', 'levels', 'levels replaced'},
    },
    'purpose': {
        'type': KeyType.TEXT,
        'keys': {'primary purpose'},
    },
    'source_port': {
        'type': KeyType.TEXT,
        'keys': {'advanced engine needed', 'source port', 'engine'},
    },
    'files_required': {
        'type': KeyType.TEXT,
        'keys': {'other files required'},
    },
    'other_files': {
        'type': KeyType.TEXT,
        'keys': {'other files by author', 'other levels by author', 'other releases', 'other wads', 'other wads by author', 'files by author', 'other works'},
        'array': True,
    },
    'other': {
        'type': KeyType.TEXT,
        'keys': {'other'},
    },
    'archive_maintainer': {
        'type': KeyType.TEXT,
        'keys': {'archive maintainer'},
    },
    'update_to': {
        'type': KeyType.TEXT,
        'keys': {'update to'},
    },
    'uploaded_by': {
        'type': KeyType.TEXT,
        'keys': {'uploaded by'},
    },
    'url': {
        'type': KeyType.TEXT,
        'keys': {'web', 'home page', 'homepage', 'internet', 'internet address', 'internet home page'},
        're': ['^web .*', '^website.*'],
        'array': True,
    },
    'bbs': {
        'type': KeyType.TEXT,
        're': ['^bbs .*'],
        'array': True,
    },
    'ftp': {
        'type': KeyType.TEXT,
        're': ['^ftp .*'],
        'array': True,
    },
}


TEXT_GAMES = {
    Game.DOOM2: {
        'keys': {'doom 2', 'doom2', 'doom ii', 'doomii', 'doom 2 ver 1.9', 'doom ][', 'doom / doom2', 'doom/doom2', 'any doom', '- doom2', '2', 'any', 'both', 'freedoom',' freedm', 'ii'},
        're': ['any doom.*', '^doom 2.*', '.*doom2.*', '.*doom2\.wad.*', '.*doom ii.*'],
    },
    Game.DOOM: {
        'keys': {'doom', 'doom1', '(ultimate) doom', 'the ultimate doom'},
        're': ['^doom.*', '.* ultimate .*', 'doom\.wad', '.*ultimate doom.*', '.*doom i.*'],
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

TEXT_BOOLEAN = {
    'true': {
        'keys': {'designed for', 'all', 'any', 'yeah!', 'yeah', 'affirmative', 'certainly', 'some', 'y', '- yes', 'one'},
        're': ['^yea.*', '^oh yeah.*', '^oh yes.*', '^supported.*', '^starts.*', '^yes.*', '^sure.*', '^probably.*', '^absolutely.*', '^definitely.*', '^some .*', '^duh.*', '^hell (yes|yeah).*', '^i guess.*', '^implemented.*', '^of course.*', '^yep.*', '^yup.*', '^a .*', '^all .*', '^designed.*', '^fully .*', '^full .*', '^you bet.*', '.*\(yes\).*', '^aye.*'],
    },
    'false': {
        'keys': {'n/a', '-', '- no', '0'},
        're': ['^no.*'],
    },
}