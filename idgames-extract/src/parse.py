from sys import argv, exit

from utils.logger import Logger
from textparser.textparser import TextParser2

if len(argv) < 2:
    print('Insufficient arguments.')
    exit(-1)

logger = Logger()

filename = argv[1]
with open(filename, 'r') as f:
    parser = TextParser2(logger)
    parser.parse(f)
