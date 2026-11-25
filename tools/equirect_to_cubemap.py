#!/usr/bin/env python3
"""
Makes the six faces of a cubemap (the faces of the skyboxes of the engine) from an equirectangular panorama, and finds where the
sun is in it. Needs Pillow only.

    equirect_to_cubemap.py panorama.jpg output-directory [face-size] [panorama.hdr]

The panorama is read as the engine reads a skybox: the center of the picture looks along -z and the right side of it goes to +x.
The faces are written as px, nx, py, ny, pz and nz (jpg) in the standard orientation of the faces of a cubemap of OpenGL. If a
radiance file (.hdr) of the same panorama is given, the direction of the sun (the brightest part of it) is printed.
"""
import math
import os
import struct
import sys

from PIL import Image


def read_radiance(path):
    """The pixels of a radiance file as lists of (r, g, b) per row, with the run length encoding of the new kind"""
    with open(path, 'rb') as stream:
        data = stream.read()

    position = data.index(b'\n\n') + 2
    end = data.index(b'\n', position)
    header = data[position:end].decode().split()
    height, width = int(header[1]), int(header[3])
    position = end + 1

    rows = []
    for _ in range(height):
        assert data[position] == 2 and data[position + 1] == 2, 'only the run length encoded files are read'
        position += 4
        channels = []
        for _channel in range(4):
            values = []
            while len(values) < width:
                count = data[position]
                position += 1
                if count > 128:
                    values.extend([data[position]] * (count - 128))
                    position += 1
                else:
                    values.extend(data[position:position + count])
                    position += count
            channels.append(values)

        rows.append([(math.ldexp(channels[0][x] / 256.0, channels[3][x] - 128) if channels[3][x] else 0.0,
                      math.ldexp(channels[1][x] / 256.0, channels[3][x] - 128) if channels[3][x] else 0.0,
                      math.ldexp(channels[2][x] / 256.0, channels[3][x] - 128) if channels[3][x] else 0.0) for x in range(width)])

    return width, height, rows


def direction(u, v):
    """The direction of a point of the panorama (from 0 to 1 along and down it): the center looks along -z"""
    longitude = (u - 0.5) * 2.0 * math.pi
    latitude = (0.5 - v) * math.pi
    return math.cos(latitude) * math.sin(longitude), math.sin(latitude), -math.cos(latitude) * math.cos(longitude)


def find_sun(path):
    width, height, rows = read_radiance(path)
    best = 0.0
    for row in rows:
        for pixel in row:
            best = max(best, 0.2126 * pixel[0] + 0.7152 * pixel[1] + 0.0722 * pixel[2])

    x_sum = y_sum = z_sum = 0.0
    for y, row in enumerate(rows):
        for x, pixel in enumerate(row):
            if 0.2126 * pixel[0] + 0.7152 * pixel[1] + 0.0722 * pixel[2] >= best * 0.8:
                dx, dy, dz = direction((x + 0.5) / width, (y + 0.5) / height)
                x_sum += dx
                y_sum += dy
                z_sum += dz

    length = math.sqrt(x_sum * x_sum + y_sum * y_sum + z_sum * z_sum)
    return best, (x_sum / length, y_sum / length, z_sum / length)


# The faces of a cubemap of OpenGL: the direction of the pixel (s, t) of a face, s and t from -1 to 1, t going down the picture
FACES = {
    'px': lambda s, t: (1.0, -t, -s),
    'nx': lambda s, t: (-1.0, -t, s),
    'py': lambda s, t: (s, 1.0, t),
    'ny': lambda s, t: (s, -1.0, -t),
    'pz': lambda s, t: (s, -t, 1.0),
    'nz': lambda s, t: (-s, -t, -1.0),
}


def make_face(name, source, width, height, size):
    pixels = source.tobytes()
    face = bytearray(size * size * 3)
    function = FACES[name]
    index = 0
    for row in range(size):
        t = (row + 0.5) / size * 2.0 - 1.0
        for column in range(size):
            s = (column + 0.5) / size * 2.0 - 1.0
            x, y, z = function(s, t)
            length = math.sqrt(x * x + y * y + z * z)
            u = (math.atan2(x, -z) / (2.0 * math.pi) + 0.5) * width - 0.5
            v = (0.5 - math.asin(y / length) / math.pi) * height - 0.5

            x0 = math.floor(u)
            y0 = math.floor(v)
            fx = u - x0
            fy = v - y0
            y1 = min(max(y0 + 1, 0), height - 1)
            y0 = min(max(y0, 0), height - 1)
            x1 = (x0 + 1) % width
            x0 = x0 % width

            for channel in range(3):
                top = pixels[(y0 * width + x0) * 3 + channel] * (1.0 - fx) + pixels[(y0 * width + x1) * 3 + channel] * fx
                bottom = pixels[(y1 * width + x0) * 3 + channel] * (1.0 - fx) + pixels[(y1 * width + x1) * 3 + channel] * fx
                face[index] = int(top * (1.0 - fy) + bottom * fy + 0.5)
                index += 1

    return Image.frombytes('RGB', (size, size), bytes(face))


def main():
    source = Image.open(sys.argv[1]).convert('RGB')
    output = sys.argv[2]
    size = int(sys.argv[3]) if len(sys.argv) > 3 else 1024
    os.makedirs(output, exist_ok=True)

    if len(sys.argv) > 4:
        brightness, sun = find_sun(sys.argv[4])
        print('sun: brightness %.1f, direction (%.4f, %.4f, %.4f)' % ((brightness,) + sun))

    for name in FACES:
        make_face(name, source, source.width, source.height, size).save(os.path.join(output, name + '.jpg'), quality=92)
        print('written', name)


if __name__ == '__main__':
    main()
