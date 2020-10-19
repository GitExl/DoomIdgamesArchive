import codecs
import sys
from time import localtime, strftime

import colorama


class Logger:

    def __init__(self, path: str):
        colorama.init()

        self.streams: dict = {}
        self.path: str = path

    def error(self, text: str):
        dt = strftime('%Y-%m-%d %H:%M:%S', localtime())
        self.stream('error', text)
        sys.stderr.write('\033[1;90m[{}]   \033[1;31m[error]\033[1;0m {}\n'.format(dt, text))

    def info(self, text: str):
        dt = strftime('%Y-%m-%d %H:%M:%S', localtime())
        self.stream('info', text)
        sys.stdout.write('\033[1;90m[{}] \033[1;0m          {}\n'.format(dt, text))

    def warn(self, text: str):
        dt = strftime('%Y-%m-%d %H:%M:%S', localtime())
        self.stream('warn', text)
        sys.stdout.write('\033[1;90m[{}] \033[1;33m[warning]\033[1;0m {}\n'.format(dt, text))

    def stream(self, stream: str, text: str):
        if stream not in self.streams:
            stream_file = codecs.open('{}/{}.txt'.format(self.path, stream), 'w', encoding='utf8')
            self.streams[stream] = stream_file
        else:
            stream_file = self.streams[stream]

        stream_file.write(text)
        stream_file.write('\n')
