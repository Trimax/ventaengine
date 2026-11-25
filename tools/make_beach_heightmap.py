#!/usr/bin/env python3
"""
Makes the heightmap of the beach demo: a wide sandy beach that goes down into the sea, with a curved shoreline, dunes behind the
beach and a headland on the left. The terrain is 2000 by 2000 meters, the sea is to -z, and the heights go from -40 to 40 meters
around the level of the sea (the range of the heights, 80, is the "factor" of the terrain). Needs Pillow only.

    make_beach_heightmap.py output.png [size]
"""
import array
import math
import random
import sys

from PIL import Image

WORLD = 2000.0
FACTOR = 80.0
SEED = 20261125


def smoothstep(low, high, value):
    t = min(max((value - low) / (high - low), 0.0), 1.0)
    return t * t * (3.0 - 2.0 * t)


class Noise:
    """Value noise: random values at the points of a grid, smoothly mixed between them"""

    def __init__(self, size, seed):
        generator = random.Random(seed)
        self.size = size
        self.values = [[generator.random() for _ in range(size)] for _ in range(size)]

    def get(self, x, z):
        x0 = math.floor(x)
        z0 = math.floor(z)
        fx = x - x0
        fz = z - z0
        fx = fx * fx * (3.0 - 2.0 * fx)
        fz = fz * fz * (3.0 - 2.0 * fz)
        a = self.values[z0 % self.size][x0 % self.size]
        b = self.values[z0 % self.size][(x0 + 1) % self.size]
        c = self.values[(z0 + 1) % self.size][x0 % self.size]
        d = self.values[(z0 + 1) % self.size][(x0 + 1) % self.size]
        return (a + (b - a) * fx) * (1.0 - fz) + (c + (d - c) * fx) * fz


LARGE = Noise(64, SEED)
MEDIUM = Noise(64, SEED + 1)
SMALL = Noise(64, SEED + 2)


def shoreline(x):
    """Where the shore is along z at x: the line is not straight"""
    return 14.0 * math.sin(x / 210.0 + 0.6) + 7.0 * math.sin(x / 83.0 + 2.0) + 3.0 * math.sin(x / 37.0)


def ground(x, z):
    """The height of the ground at a point of the world, in meters over the sea"""
    d = z - shoreline(x)

    if d < 0.0:
        # The sea floor goes down gently near the beach, and then steeper, down to the deep
        return -min(0.03 * -d + 0.00012 * d * d, 28.0)

    height = 0.055 * d

    # The dunes behind the beach: ridges that grow with the distance from the water
    dunes = LARGE.get(x / 160.0, z / 160.0) * 0.65 + MEDIUM.get(x / 70.0, z / 70.0) * 0.28 + SMALL.get(x / 28.0, z / 28.0) * 0.07
    height += 6.0 * smoothstep(60.0, 220.0, d) * dunes

    # A headland on the left, over the water and the land
    headland = math.exp(-(((x + 520.0) ** 2 + (z - shoreline(-520.0) - 40.0) ** 2) / (2.0 * 130.0 ** 2)))
    height += 24.0 * headland * (0.8 + 0.2 * MEDIUM.get(x / 40.0, z / 40.0))

    return height


def main():
    size = int(sys.argv[2]) if len(sys.argv) > 2 else 2048
    values = array.array('H', [0]) * (size * size)
    index = 0
    for row in range(size):
        z = ((row + 0.5) / size - 0.5) * WORLD
        for column in range(size):
            x = ((column + 0.5) / size - 0.5) * WORLD
            values[index] = int(min(max(ground(x, z) / FACTOR + 0.5, 0.0), 1.0) * 65535.0 + 0.5)
            index += 1

    Image.frombytes('I;16', (size, size), values.tobytes()).save(sys.argv[1])


if __name__ == '__main__':
    main()
