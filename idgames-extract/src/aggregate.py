import codecs
from sys import argv, exit


if len(argv) < 3:
    print('Insufficient arguments.')
    exit(-1)

filename_in = argv[1]
filename_out = argv[2]
lines = {}

with codecs.open(filename_in, 'r', 'latin1') as f:
    for line in f.readlines():
        line = line.strip()
        if line in lines:
            lines[line] += 1
        else:
            lines[line] = 1

sorted_lines = sorted(lines.items(), key=lambda item: item[1], reverse=True)

with codecs.open(filename_out, 'w', 'latin1') as f:
    for line, count in sorted_lines:
        f.write('{}: {}\n'.format(line, count))
