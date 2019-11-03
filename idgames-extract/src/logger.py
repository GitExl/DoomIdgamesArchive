import codecs
import sys
import colorama


class Logger:

    def __init__(self):
        colorama.init()

        self.streams: dict = {}

    def error(self, text: str):
        self.stream('error', text)
        sys.stderr.write('\033[1;31m  [error]\033[1;0m {}\n'.format(text))

    def info(self, text: str):
        self.stream('info', text)
        sys.stdout.write('          {}\n'.format(text))

    def warn(self, text: str):
        self.stream('warn', text)
        sys.stdout.write('\033[1;33m[warning]\033[1;0m {}\n'.format(text))

    def stream(self, stream: str, text: str):
        if stream not in self.streams:
            stream_file = codecs.open('logs/{}.txt'.format(stream), 'w', encoding='utf8')
            self.streams[stream] = stream_file
        else:
            stream_file = self.streams[stream]

        stream_file.write(text)
        stream_file.write('\n')
