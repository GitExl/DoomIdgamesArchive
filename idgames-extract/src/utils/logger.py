import codecs
import sys
from time import localtime, strftime

import colorama


class Logger:

    VERBOSITY_ERROR: int = 0
    VERBOSITY_WARNING: int = 1
    VERBOSITY_INFO: int = 2
    VERBOSITY_DECISION: int = 3
    VERBOSITY_DEBUG: int = 4

    def __init__(self, path: str, verbosity: int = VERBOSITY_INFO):
        colorama.init(autoreset=True)

        self.streams: dict = {}
        self.path: str = path
        self.verbosity: int = verbosity

    def debug(self, text: str):
        if self.verbosity < Logger.VERBOSITY_DEBUG:
            return

        dt = strftime('%Y-%m-%d %H:%M:%S', localtime())
        sys.stdout.write('{} \033[1;36m[debug]    {}\n'.format(dt, text))

    def decision(self, text: str):
        if self.verbosity < Logger.VERBOSITY_DECISION:
            return

        dt = strftime('%Y-%m-%d %H:%M:%S', localtime())
        sys.stdout.write('{} \033[1;32m[decision] {}\n'.format(dt, text))

    def error(self, text: str):
        if self.verbosity < Logger.VERBOSITY_ERROR:
            return

        dt = strftime('%Y-%m-%d %H:%M:%S', localtime())
        sys.stderr.write('{} \033[1;31m[error]    {}\n'.format(dt, text))

    def info(self, text: str):
        if self.verbosity < Logger.VERBOSITY_INFO:
            return

        dt = strftime('%Y-%m-%d %H:%M:%S', localtime())
        sys.stdout.write('{}            {}\n'.format(dt, text))

    def warn(self, text: str):
        if self.verbosity < Logger.VERBOSITY_WARNING:
            return

        dt = strftime('%Y-%m-%d %H:%M:%S', localtime())
        sys.stdout.write('{} \033[1;33m[warning]  {}\n'.format(dt, text))

    def stream(self, stream: str, text: str):
        if stream not in self.streams:
            stream_file = codecs.open('{}/{}.txt'.format(self.path, stream), 'w', encoding='utf8')
            self.streams[stream] = stream_file
        else:
            stream_file = self.streams[stream]

        stream_file.write(text)
        stream_file.write('\n')
