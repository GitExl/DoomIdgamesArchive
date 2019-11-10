from sys import argv, exit

from logger import Logger
from textparser.textparser2 import TextParser2

if len(argv) < 2:
    print('Insufficient arguments.')
    exit(-1)

logger = Logger()

filename = argv[1]
with open(filename, 'r') as f:
    parser = TextParser2(logger)
    parser.parse(f)
