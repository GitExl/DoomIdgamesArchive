import pprint
from sys import argv, exit

from utils.config import Config
from utils.logger import Logger
from textparser.textparser import TextParser

if len(argv) < 2:
    print('Insufficient arguments.')
    exit(-1)

config = Config()
logger = Logger(config.get('paths.logs'), Logger.VERBOSITY_DEBUG)

filename = argv[1]
with open(filename, 'r') as f:
    parser = TextParser(logger)
    parser.parse(f)

pp = pprint.PrettyPrinter(indent=4)

if len(argv) == 3:
    pp.pprint(parser.info[argv[2]])
else:
    pp.pprint(parser.info)
