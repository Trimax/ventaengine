package io.github.trimax.packer.model;

import java.util.Optional;

import io.github.trimax.packer.enums.TextureChannel;
import io.github.trimax.packer.util.ImageUtil;

public record Source(Image image, TextureChannel channel, Byte channelValue) {
    private Source(final Image image, final TextureChannel channel) {
        this(image, channel, null);
    }

    private Source(final Image image, final byte channelValue) {
        this(image, null, channelValue);
    }

    private static Source ofChannel(final String channel, final Image image) {
        return Optional.ofNullable(channel)
                .map(TextureChannel::of)
                .map(c -> new Source(image, c))
                .orElseThrow(() -> new IllegalArgumentException("Invalid texture channel: " + channel));
    }

    private static Source ofValue(final String channelValue, final Image image) {
        final var value = Integer.parseInt(channelValue);
        if (value < 0 || value > 255)
            throw new IllegalArgumentException("Channel value out of range 0-255: " + value);

        return new Source(image, (byte) value);
    }

    public static Source of(final String argument) {
        final var parts = argument.split(":");
        if (parts.length != 2)
            throw new IllegalArgumentException("Invalid source format: " + argument);

        try {
            return ofValue(parts[1].trim(), ImageUtil.read(parts[0]));
        } catch (final NumberFormatException ignored) {
            return ofChannel(parts[1].trim(), ImageUtil.read(parts[0]));
        }
    }

    public byte getValue(final int x, final int y) {
        if (channelValue != null)
            return channelValue;

        final var pixelIndex = (x + y * image.width()) * image.channels();
        final var channelIndex = channel.ordinal();

        if (channelIndex >= image.channels())
            return (byte) (channelIndex == 3 ? 255 : 0);

        return image.data()[pixelIndex + channelIndex];
    }
}
