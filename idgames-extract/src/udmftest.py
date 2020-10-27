import cProfile

from doom.udmfparser import UDMFParser


def test():
    with open('c:/Users/Dennis/Desktop/TEXTMAP.txt', 'r') as f:
        parser = UDMFParser()
        parser.parse(f.read())

        print(len(parser.vertices), len(parser.lines), len(parser.sides), len(parser.sectors), len(parser.things))


cProfile.run('test()', sort='tottime')
#test()