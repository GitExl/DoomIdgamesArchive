# /idgames extractor
This can read, analyze and extract information from an /idgames archive directory.

## Usage
Run ``src/extract.py``. This will read /idgames archive files from ``/idgames`` and output main images as WEBP into
``/graphics``, an SQLite database with basic file and image information as ``idgames.db`` and some diagnostic logs in
``/logs``.

## Requirements
This requires Python 3.8 or newer, as well as Pillow and Colorama, both of which are available via PyPI.

The following IWADs are required to be present in /iwads as these are used for their palette:

* ``DOOM.WAD``: (Ultimate) Doom
* ``DOOM2.WAD``: Doom 2
* ``HACX.WAD``: Hacx
* ``HERETIC.WAD``: Heretic
* ``HEXEN.WAD``: Hexen
* ``PLUTONIA.WAD``: Plutonia
* ``STRIFE0.WAD``: Strife (Shareware)
* ``TNT.WAD``: TNT: Evilution

## Limitations

* In some cases the wrong game type is detected. As a result images with the wrong palette will be output.
* The Python standard library Zip functionality does not support the implode compression type used by some older
files present in the /idgames archive. This will be skipped.
