package io.github.trimax.packer.model;

public record Image(int width, int height, int channels, byte[] data) {
    public boolean isValid() {
        return (data != null) && (data.length > 0) && (data.length == (width * height * channels));
    }
}
