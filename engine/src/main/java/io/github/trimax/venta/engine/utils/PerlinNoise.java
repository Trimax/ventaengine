package io.github.trimax.venta.engine.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class PerlinNoise {
    
    private static final int[] PERMUTATION = {
        151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225,
        140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21, 10, 23, 190, 6, 148,
        247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32,
        57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175,
        74, 165, 71, 134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122,
        60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244, 102, 143, 54,
        65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169,
        200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64,
        52, 217, 226, 250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212,
        207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42, 223, 183, 170, 213,
        119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9,
        129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104,
        218, 246, 97, 228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241,
        81, 51, 145, 235, 249, 14, 239, 107, 49, 192, 214, 31, 181, 199, 106, 157,
        184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254, 138, 236, 205, 93,
        222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180
    };
    
    private static final int[] P = new int[512];
    
    static {
        for (int i = 0; i < 256; i++)
            P[i] = P[i + 256] = PERMUTATION[i];
    }

    public float[][] generate(final int width, final int height,
                              final long seed, final double cellSize,
                              final int levels, final double attenuation,
                              final double groovy) {
        final var heightmap = new float[width][height];
        final var random = new java.util.Random(seed);
        final var offsetX = random.nextDouble() * 1000;
        final var offsetY = random.nextDouble() * 1000;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                var noiseValue = 0.0;
                var frequency = 1.0 / cellSize;
                var amplitude = 1.0;
                var maxValue = 0.0;

                for (int level = 0; level < levels; level++) {
                    final var sampleX = (x + offsetX) * frequency;
                    final var sampleY = (y + offsetY) * frequency;
                    
                    var perlinValue = noise(sampleX, sampleY);

                    if (groovy > 0.0) {
                        final var turbulenceX = sampleX * groovy;
                        final var turbulenceY = sampleY * groovy;
                        final var turbulence = Math.abs(noise(turbulenceX, turbulenceY));
                        perlinValue = Math.abs(perlinValue) * (1.0 + turbulence * groovy);
                    }
                    
                    noiseValue += perlinValue * amplitude;
                    maxValue += amplitude;
                    amplitude *= attenuation;
                    frequency *= 2.0;
                }

                noiseValue /= maxValue;
                noiseValue = (noiseValue + 1.0) * 0.5;
                noiseValue = Math.max(0.0, Math.min(1.0, noiseValue));

                heightmap[x][y] = (float) (noiseValue * 255.0);
            }
        }

        return heightmap;
    }

    private double noise(final double x, final double y) {
        final var X = (int) Math.floor(x) & 255;
        final var Y = (int) Math.floor(y) & 255;
        
        final var xf = x - Math.floor(x);
        final var yf = y - Math.floor(y);
        
        final var topRight = dotGridGradient(X + 1, Y, xf - 1, yf);
        final var topLeft = dotGridGradient(X, Y, xf, yf);
        final var bottomRight = dotGridGradient(X + 1, Y + 1, xf - 1, yf - 1);
        final var bottomLeft = dotGridGradient(X, Y + 1, xf, yf - 1);
        
        final var xt = interpolate(topLeft, topRight, xf);
        final var xb = interpolate(bottomLeft, bottomRight, xf);
        
        return interpolate(xt, xb, yf);
    }

    private double dotGridGradient(final int ix, final int iy, final double x, final double y) {
        final var gradient = randomGradient(ix, iy);
        return gradient[0] * x + gradient[1] * y;
    }

    private double[] randomGradient(final int ix, final int iy) {
        final var random = P[(ix + P[iy & 255]) & 255];
        final var theta = random * 2.0 * Math.PI / 256.0;
        return new double[]{Math.cos(theta), Math.sin(theta)};
    }

    private double interpolate(final double a0, final double a1, final double w) {
        final var weight = smootherstep(w);
        return (a1 - a0) * weight + a0;
    }

    private double smootherstep(final double x) {
        return x * x * x * (x * (x * 6 - 15) + 10);
    }
}
