package io.github.trimax.packer.model;

import io.github.trimax.packer.enums.TextureChannel;
import io.github.trimax.packer.util.ImageUtil;

public record Source(Image image, TextureChannel channel) {
    public static Source of(String argument) {
        final var parts = argument.split(":");
        if (parts.length != 2)
            throw new IllegalArgumentException("Invalid source format: " + argument);

        final var channel = TextureChannel.of(parts[1]);
        if (channel == null)
            throw new IllegalArgumentException("Invalid texture channel: " + parts[1]);

        return new Source(ImageUtil.read(parts[0]), channel);
    }

    public byte getValue(final int x, final int y) {
        final var pixelIndex = (x + y * image.width()) * image.channels();
        final var channelIndex = channel.ordinal();

        if (channelIndex >= image.channels())
            return (byte) (channelIndex == 3 ? 255 : 0);

        return image.data()[pixelIndex + channelIndex];
    }
}
