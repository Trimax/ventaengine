#!/usr/bin/env python3
"""
Writes a wav file as ogg vorbis, with the channels it has (a mono file stays mono: the engine plays only mono sounds in 3D).
Needs the soundfile package.

    wav_to_vorbis.py input.wav output.ogg
"""
import sys

import soundfile


def main():
    data, rate = soundfile.read(sys.argv[1])
    soundfile.write(sys.argv[2], data, rate, format='OGG', subtype='VORBIS')


if __name__ == '__main__':
    main()
