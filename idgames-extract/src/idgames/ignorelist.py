import re

# Most of these are ignored to exclude their images, in order to lower the Google Play content rating.
IGNORE = {
    r'levels/doom/a-c/bedlam2\.zip',  # X-rated \ risky
    r'levels/doom2/a-c/cassie\.zip',  # X-rated \ risky
    r'levels/doom2/g-i/goatdoom\.zip',  # Shock image
    r'levels/doom2/m-o/noname\.zip',  # X-rated \ risky
    r'levels/doom2/s-u/ultra\.zip',  # Not a valid WAD file, is actually a Wolf3D map
    r'levels/doom2/s-u/udsm1\.zip',  # Not a valid WAD file, is actually a Wolf3D map
    r'levels/doom2/deathmatch/a-c/cesspool\.zip',  # Has weird 128-byte header before actual WAD header
    r'levels/doom2/deathmatch/a-c/arcarena\.zip',  # X-rated \ risky
    r'levels/doom2/deathmatch/g-i/gem02\.zip',  # X-rated \ risky
    r'levels/doom2/deathmatch/p-r/rage\.zip',  # X-rated \ risky
    r'levels/doom2/deathmatch/Ports/s-u/sleeper\.zip',  # X-rated \ risky
    r'levels/doom2/deathmatch/Ports/s-u/sottc\.zip',  # X-rated \ risky
    r'levels/doom2/deathmatch/Ports/v-z/yrstick\.zip',  # X-rated \ risky
    r'levels/doom2/Ports/m-o/mastmaga\.zip',  # X-rated \ risky
    r'levels/doom2/Ports/m-o/nina2\.zip',  # X-rated \ risky
    r'levels/doom2/Ports/m-o/ninadbrv\.zip',  # X-rated \ risky
    r'levels/doom2/Ports/d-f/female\.zip',  # X-rated \ risky
    r'levels/doom2/Ports/j-l/lplant\.zip',  # X-rated \ risky
    r'themes/terrywads/sreality\.zip',  # X-rated \ risky
    r'themes/wolf3d/.*',  # Nazi imagery. Affects Google Play content rating.
    r'themes/x-rated/.*',  # X-rated category
}


def must_ignore(path: str) -> bool:
    """
    Returns if an idgames path should be ignored.

    :param path:
    :return:
    """

    for ignore in IGNORE:
        if re.match(ignore, path):
            return True

    return False
