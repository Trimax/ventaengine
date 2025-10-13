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
        for (int i = 0; i < 256; i++) {
            P[i] = P[i + 256] = PERMUTATION[i];
        }
    }
    
    public static float[][] generateHeightmap(final int width, final int height,
                                            final int octaves, final double amplitude,
                                            final double persistence, final double weight,
                                            final int minValue, final int maxValue) {
        final long seed = System.currentTimeMillis();
        
        final float[][] heightmap = new float[width][height];
        final java.util.Random random = new java.util.Random(seed);
        final double offsetX = random.nextDouble() * 1000;
        final double offsetY = random.nextDouble() * 1000;
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double noiseValue = 0.0;
                double currentAmplitude = amplitude;
                double frequency = 1.0 / (width * 0.1);
                
                for (int octave = 0; octave < octaves; octave++) {
                    final double sampleX = (x + offsetX) * frequency;
                    final double sampleY = (y + offsetY) * frequency;
                    final double perlinValue = noise(sampleX, sampleY);
                    
                    noiseValue += perlinValue * currentAmplitude;
                    currentAmplitude *= persistence;
                    frequency *= 2.0;
                }
                
                noiseValue = (noiseValue + 1.0) * 0.5;
                noiseValue = Math.max(0.0, Math.min(1.0, noiseValue));
                noiseValue = Math.pow(noiseValue, 1.0 / weight);
                
                final float scaledValue = (float) (minValue + noiseValue * (maxValue - minValue));
                heightmap[x][y] = scaledValue;
            }
        }

        return heightmap;
    }
    
    public static double noise(final double x, final double y) {
        final int X = (int) Math.floor(x) & 255;
        final int Y = (int) Math.floor(y) & 255;
        final double xf = x - Math.floor(x);
        final double yf = y - Math.floor(y);
        final double u = fade(xf);
        final double v = fade(yf);
        
        final int aa = P[P[X] + Y];
        final int ab = P[P[X] + Y + 1];
        final int ba = P[P[X + 1] + Y];
        final int bb = P[P[X + 1] + Y + 1];

        return lerp(v,
            lerp(u, grad(aa, xf, yf), grad(ba, xf - 1, yf)),
            lerp(u, grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1))
        );
    }
    
    private static double fade(final double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
    
    private static double lerp(final double t, final double a, final double b) {
        return a + t * (b - a);
    }
    
    private static double grad(final int hash, final double x, final double y) {
        final int h = hash & 3;
        final double u = h < 2 ? x : y;
        final double v = h < 2 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}
