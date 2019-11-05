from datetime import date
from enum import Enum
from typing import Optional, Set, List, Dict

from PIL import Image

from game import Game


class GameStyles(Enum):
    SINGLE_PLAYER = 'single_player'
    DEATHMATCH = 'deathmatch'
    COOPERATIVE = 'cooperative'
    OTHER = 'other'


class ContentTypes(Enum):
    LEVELS = 'levels'
    MUSIC = 'music'
    GRAPHICS = 'graphics'
    TEXTURES = 'textures'
    SOUNDS = 'sounds'
    DEMOS = 'demos'
    DEHACKED = 'dehacked'
    DECORATE = 'decorate'
    PALETTE = 'palette'


class ReviewType(Enum):
    TEXT = 'text'
    VIDEO = 'video'


class GraphicType(Enum):
    SCREEN = 'screen'
    SCREENSHOT = 'screenshot'


class VideoType(Enum):
    YOUTUBE = 'youtube'
    TWITCH = 'twitch'


class Level:

    def __init__(self, lump_name: str):
        self.lump_name: str = lump_name

        self.title: Optional[str] = None
        self.authors: List[str] = []
        self.has_difficulty_levels: bool = False


class Graphic:

    def __init__(self, title: str, graphic_type: GraphicType, image: Image):
        self.title: str = title
        self.graphic_type: GraphicType = graphic_type
        self.image: Image = image

        self.source: Optional[str] = None
        self.attribution: Optional[str] = None


class Video:

    def __init__(self, title: str, video_type: VideoType, source: str):
        self.title: str = title
        self.video_type: VideoType = video_type
        self.source: str = source

        self.attribution: Optional[str] = None


class Review:

    def __init__(self, review_type: ReviewType):
        self.review_type: ReviewType = review_type

        self.text: Optional[str] = None
        self.author: Optional[str] = None
        self.source: Optional[str] = None
        self.attribution: Optional[str] = None


class Entry:

    def __init__(self, path: str, text: str):
        self.path: str = path
        self.text: str = text

        self.game_styles: Set[GameStyles] = set()
        self.content: Set[ContentTypes] = set()
        self.authors: List[str] = []
        self.links: List[str] = []
        self.levels: Dict[Level] = {}
        self.reviews: List[Review] = []
        self.graphics: Dict[Graphic] = {}
        self.videos: List[Video] = []

        self.score_aggregated: float = 0

        self.game: Game = Game.UNKNOWN
        self.title: Optional[str] = None
        self.description: Optional[str] = None
        self.based_on: Optional[str] = None
        self.story: Optional[str] = None
        self.notes: Optional[str] = None
        self.theme: Optional[str] = None
        self.known_bugs: Optional[str] = None
        self.tools_used: Optional[str] = None
        self.credits: Optional[str] = None
        self.tested_with: Optional[str] = None
        self.do_not_run_with: Optional[str] = None
        self.build_time: Optional[str] = None
        self.source_port: Optional[str] = None
        self.other_files_required: Optional[str] = None

        self.date_file: Optional[date] = None
        self.date_completed: Optional[date] = None
        self.date_released: Optional[date] = None
